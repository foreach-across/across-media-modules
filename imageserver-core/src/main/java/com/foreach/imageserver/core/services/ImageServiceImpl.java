package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.managers.ImageManager;
import com.foreach.imageserver.core.managers.ImageModificationManager;
import com.foreach.imageserver.core.managers.ImageResolutionManager;
import com.foreach.imageserver.core.services.exceptions.CropOutsideOfImageBoundsException;
import com.foreach.imageserver.core.services.exceptions.ImageCouldNotBeRetrievedException;
import com.foreach.imageserver.core.services.exceptions.ImageStoreException;
import com.foreach.imageserver.core.transformers.ImageAttributes;
import com.foreach.imageserver.core.transformers.InMemoryImageSource;
import com.foreach.imageserver.core.transformers.StreamImageSource;
import com.foreach.imageserver.dto.CropDto;
import com.foreach.imageserver.dto.DimensionsDto;
import com.foreach.imageserver.dto.ImageModificationDto;
import com.foreach.imageserver.dto.ImageResolutionDto;
import com.foreach.imageserver.logging.LogHelper;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ImageServiceImpl implements ImageService
{
	@Autowired
	private ImageManager imageManager;

	@Autowired
	private ImageStoreService imageStoreService;

	@Autowired
	private ImageModificationManager imageModificationManager;

	@Autowired
	private ImageTransformService imageTransformService;

	@Autowired
	private CropGenerator cropGenerator;

	@Autowired
	private CropGeneratorUtil cropGeneratorUtil;

	@Autowired
	private ImageResolutionManager imageResolutionManager;

	@Autowired
	private ImageProfileService imageProfileService;

	// Used concurrently from multiple threads.
	private Map<VariantImageRequest, FutureVariantImage> futureVariantImages = new ConcurrentHashMap<>();

	@Override
	public Image getById( long imageId ) {
		return imageManager.getById( imageId );
	}

	@Override
	public Image getByExternalId( String externalId ) {
		return imageManager.getByExternalId( externalId );
	}

	// TODO I'm not taking care of errors right now, make sure to tackle this later on!
	@Override
	@Transactional
	public Image saveImage( String externalId,
	                        byte[] imageBytes,
	                        Date imageDate,
	                        boolean replaceExisting ) throws ImageStoreException {
		if ( StringUtils.isBlank( externalId ) || imageBytes == null || imageDate == null ) {
			LOG.warn( "Null parameters not allowed - ImageServiceImpl#saveImage: image={}, context={}, imageDate={}",
			          LogHelper.flatten( externalId, imageBytes, imageDate ) );
		}

		Image existing = getByExternalId( externalId );

		if ( existing != null ) {
			if ( !replaceExisting ) {
				throw new ImageStoreException(
						"An image already exists with that external id and no explicit replace was asked." );
			}
			else {
				LOG.debug( "Removing image {} - because it should be replaced", existing );
				deleteImage( existing.getExternalId() );
			}
		}

		Image image = loadImageData( imageBytes );
		image.setExternalId( externalId );
		image.setDateCreated( imageDate );

		imageManager.insert( image );
		imageStoreService.storeOriginalImage( image, imageBytes );

		return image;
	}

	@Override
	@Transactional
	public synchronized boolean deleteImage( String externalId ) {
		Image image = getByExternalId( externalId );

		if ( image != null ) {
			// Delete modifications
			LOG.debug( "Deleting all modifications for image {}", image );
			imageModificationManager.deleteModifications( image.getId() );

			// Delete variants
			imageStoreService.removeVariants( image.getId() );

			// Delete image record
			LOG.debug( "Deleting image record for image {}", image );
			imageManager.delete( image );

			// Delete original
			imageStoreService.removeOriginal( image );

			return true;
		}

		return false;
	}

	@Override
	public Image createImage( byte[] imageBytes ) {
		Image image = loadImageData( imageBytes );
		image.setExternalId( UUID.randomUUID().toString() );
		image.setTemporaryImage( true );
		imageStoreService.storeOriginalImage( image, imageBytes );
		return image;
	}

	private Image loadImageData( @NonNull byte[] imageBytes ) {
		ImageAttributes imageAttributes = imageTransformService.getAttributes( new ByteArrayInputStream( imageBytes ) );

		Image image = new Image();
		image.setImageProfileId( imageProfileService.getDefaultProfile().getId() );
		image.setDimensions( imageAttributes.getDimensions() );
		image.setImageType( imageAttributes.getType() );
		image.setFileSize( imageBytes.length );
		return image;
	}

	@Override
	public void saveImageModification( ImageModification modification, Image image ) {
		if ( modification == null ) {
			LOG.warn( "Null parameters not allowed - ImageServiceImpl#saveImageModification: modification={}",
			          LogHelper.flatten( modification ) );
		}

		saveImageModifications( Arrays.asList( modification ), image );
	}

	/**
	 * DO NOT MAKE THIS METHOD TRANSACTIONAL! If we are updating an existing modification, we need to make sure that
	 * the changes are committed to the database *before* we clean up the filesystem. Otherwise a different instance
	 * might recreate variants on disk using the old values.
	 * <p>
	 * TODO: if needed, storeImageModficication could perhaps be made transactional
	 */
	@Override
	public void saveImageModifications( List<ImageModification> modifications, Image image ) {
		if ( modifications == null ) {
			LOG.warn( "Null parameters not allowed - ImageServiceImpl#saveImageModifications: modifications={}",
			          LogHelper.flatten( modifications ) );
		}
		if ( modifications.size() == 0 ) {
			LOG.warn(
					"An empty list of modifications was provided - ImageServiceImpl#saveImageModifications: modifications={}",
					LogHelper.flatten( modifications ) );
		}

		storeImageModification( modifications, image );

		imageStoreService.removeVariants( image.getId() );
	}

	private void storeImageModification( List<ImageModification> modifications, Image image ) {
		for ( ImageModification modification : modifications ) {
			int imageWidth = image.getDimensions().getWidth();
			int imageHeight = image.getDimensions().getHeight();
			int cropX = modification.getCrop().getX();
			int cropY = modification.getCrop().getY();
			int cropWidth = modification.getCrop().getWidth();
			int cropHeight = modification.getCrop().getHeight();

			if ( cropX < 0 || cropY < 0 || cropWidth + cropX > imageWidth || cropHeight + cropY > imageHeight ) {
				throw new CropOutsideOfImageBoundsException( "The crop fell at least partly outside the image bounds" );
			}

			ImageModification existingModification =
					imageModificationManager.getById( modification.getImageId(), modification.getContextId(),
					                                  modification.getResolutionId() );
			if ( existingModification == null ) {
				imageModificationManager.insert( modification );
			}
			else {
				imageModificationManager.update( modification );
			}
		}
	}

	@Override
	public StreamImageSource getVariantImage( Image image,
	                                          ImageContext context,
	                                          ImageResolution imageResolution,
	                                          ImageVariant imageVariant ) {
		if ( image == null || context == null || imageResolution == null || imageVariant == null ) {
			LOG.warn(
					"Null parameters not allowed - ImageServiceImpl#getVariantImage: image={}, context={}, imageResolution={}, imageVariant={}",
					LogHelper.flatten( image, context, imageResolution, imageVariant ) );
		}

		StreamImageSource imageSource =
				imageStoreService.getVariantImage( image, context, imageResolution, imageVariant );
		if ( imageSource == null ) {
			ImageModificationDto modification = cropGenerator.buildModificationDto( image, context, imageResolution );
			imageSource = generateVariantImage( image, context, modification, imageResolution, imageVariant, true );

			/**
			 * The ImageModification objects we used to determine the Crop may have changed while we were busy
			 * generating it. On the other hand, we expect the chances of this actually happening to be pretty low. To
			 * avoid having to keep database locking in mind whenever we work with ImageModification-s, we employ some
			 * semi-optimistic concurrency control. Specifically: we always write the file without any advance
			 * checking. This may cause us to serve stale variants for a very short while. Then we check that the
			 * ImageModification was not altered behind our back. Should this be the case we delete the variant from
			 * disk; it will then be recreated during the next request.
			 */
			ImageModificationDto reviewModification =
					cropGenerator.buildModificationDto( image, context, imageResolution );
			if ( !modification.equals( reviewModification ) ) {
				imageStoreService.removeVariantImage( image, context, imageResolution, imageVariant );
			}

		}
		return imageSource;
	}

	@Override
	public StreamImageSource generateModification( Image image,
	                                               ImageModificationDto modificationDto,
	                                               ImageVariant imageVariant ) {
		cropGeneratorUtil.normalizeModificationDto( image, modificationDto );
		return generateVariantImage( image, null, modificationDto, null, imageVariant, false );
	}

	/**
	 * We allow just one thread to generate a specific variant. Other threads that require this variant simultaneously
	 * will block and re-use the same result.
	 */
	private StreamImageSource generateVariantImage( Image image,
	                                                ImageContext context,
	                                                ImageModificationDto imageModification,
	                                                ImageResolution requestedResolution,
	                                                ImageVariant imageVariant,
	                                                boolean storeImage ) {
		if ( image == null || context == null || imageModification == null || requestedResolution == null || imageVariant == null ) {
			LOG.warn(
					"Null parameters not allowed - ImageServiceImpl#generateVariantImage: image={}, context={}, imageModification={}, requestedResolution={}, imageVariant={}, storeImage={}",
					LogHelper.flatten( image, context, imageModification, requestedResolution, imageVariant,
					                   storeImage ) );
		}

		if ( storeImage && ( requestedResolution == null ) ) {
			throw new IllegalArgumentException( "Cannot store image without a requested resolution specified." );
		}

		VariantImageRequest request =
				new VariantImageRequest( image.getId(), context != null ? context.getId() : 0, imageModification,
				                         requestedResolution,
				                         imageVariant );

		FutureVariantImage futureVariantImage;
		boolean otherThreadIsCreatingVariant;
		synchronized ( this ) {
			otherThreadIsCreatingVariant = futureVariantImages.containsKey( request );
			if ( otherThreadIsCreatingVariant ) {
				futureVariantImage = futureVariantImages.get( request );
			}
			else {
				futureVariantImage = new FutureVariantImage();
				futureVariantImages.put( request, futureVariantImage );
			}
		}

		if ( otherThreadIsCreatingVariant ) {
			return futureVariantImage.get();
		}
		else {
			try {
				InMemoryImageSource variantImageSource =
						generateVariantImageInCurrentThread( image, context, imageModification, requestedResolution,
						                                     imageVariant,
						                                     storeImage );
				synchronized ( this ) {
					futureVariantImage.setResult( variantImageSource );
					futureVariantImages.remove( request );
				}
				return variantImageSource.stream();
			}
			catch ( RuntimeException e ) {
				LOG.error(
						"Encountered error during image variant file creation - ImageServiceImpl#generateVariantImage: image={}, context={}, imageModification={}, imageVariant={}, storeImage={}",
						LogHelper.flatten( image ), LogHelper.flatten( context ),
						LogHelper.flatten( imageModification ), LogHelper.flatten( imageVariant ), storeImage, e );
				synchronized ( this ) {
					futureVariantImage.setRuntimeException( e );
					futureVariantImages.remove( request );
					if ( storeImage ) {
						removeImageVariantFile( image, context, requestedResolution, imageVariant );
					}
				}
				throw e;
			}
			catch ( Error e ) {
				synchronized ( this ) {
					futureVariantImage.setError( e );
					futureVariantImages.remove( request );
				}
				throw e;
			}
		}
	}

	/**
	 * Tries to remove the image variant file to avoid the possibility that a corrupt file was created and will be persisted
	 */
	private void removeImageVariantFile( Image image,
	                                     ImageContext context,
	                                     ImageResolution imageResolution,
	                                     ImageVariant imageVariant ) {
		try {
			imageStoreService.removeVariantImage( image, context, imageResolution, imageVariant );
		}
		catch ( Exception e ) {
			LOG.error(
					"Encountered error while trying to remove an image variant file due to errors during its creation - ImageServiceImpl#generateVariantImage: image={}, context={}, imageResolution={}, imageVariant={}, storeImage={}",
					LogHelper.flatten( image ), LogHelper.flatten( context ), LogHelper.flatten( imageResolution ),
					LogHelper.flatten( imageVariant ), e );
		}
	}

	private InMemoryImageSource generateVariantImageInCurrentThread( Image image,
	                                                                 ImageContext context,
	                                                                 ImageModificationDto modificationDto,
	                                                                 ImageResolution requestedResolution,
	                                                                 ImageVariant imageVariant,
	                                                                 boolean storeImage ) {
		if ( image == null || context == null || modificationDto == null || requestedResolution == null || imageVariant == null ) {
			LOG.warn(
					"Null parameters not allowed - ImageServiceImpl#generateVariantImageInCurrentThread: image={}, context={}, modificationDto={}, requestedResolution={}, imageVariant={}, storeImage={}",
					LogHelper.flatten( image, context, modificationDto, requestedResolution, imageVariant,
					                   storeImage ) );
		}

		if ( storeImage && ( requestedResolution == null ) ) {
			throw new IllegalArgumentException( "Cannot store image without a requested resolution specified." );
		}

		ImageResolution imageResolution = new ImageResolution();
		imageResolution.setWidth( modificationDto.getResolution().getWidth() );
		imageResolution.setHeight( modificationDto.getResolution().getHeight() );

		ImageResolutionDto outputDimensions = modificationDto.getResolution();
		CropDto crop = modificationDto.getCrop();
		DimensionsDto density = modificationDto.getDensity();

		StreamImageSource originalImageSource = imageStoreService.getOriginalImage( image );
		if ( originalImageSource == null ) {
			String message = String.format(
					"The original image is not available on disk. image=%s, context=%s, modificationDto=%s, requestedResolution=%s, imageVariant=%s,",
					LogHelper.flatten( image ), LogHelper.flatten( context ), LogHelper.flatten( modificationDto ),
					LogHelper.flatten( requestedResolution ), LogHelper.flatten( imageVariant ) );
			LOG.error( message );
			throw new ImageCouldNotBeRetrievedException( message );
		}

		InMemoryImageSource variantImageSource =
				imageTransformService.modify( originalImageSource, outputDimensions.getWidth(),
				                              outputDimensions.getHeight(), crop.getX(), crop.getY(), crop.getWidth(),
				                              crop.getHeight(), density.getWidth(), density.getHeight(),
				                              imageVariant.getOutputType(), imageVariant.getBoundaries() );
		if ( variantImageSource == null ) {
			String message = String.format(
					"Failed to retrieve in-memory variant image source. image=%s, context=%s, modificationDto=%s, requestedResolution=%s, imageVariant=%s,",
					LogHelper.flatten( image ), LogHelper.flatten( context ), LogHelper.flatten( modificationDto ),
					LogHelper.flatten( requestedResolution ), LogHelper.flatten( imageVariant ) );
			LOG.error( message );
			throw new ImageCouldNotBeRetrievedException( message );

		}

		// TODO We might opt to catch exceptions here and not fail on the write. We can return the variant in memory regardless.
		if ( storeImage ) {
			imageStoreService.storeVariantImage( image, context, requestedResolution, imageVariant,
			                                     variantImageSource.byteStream() );
		}

		return variantImageSource;
	}

	@Override
	public boolean hasModification( int imageId ) {
		return imageModificationManager.hasModification( imageId );
	}

	@Override
	public ImageResolution getResolution( long resolutionId ) {
		return imageResolutionManager.getById( resolutionId );
	}

	@Override
	public ImageResolution getResolution( int width, int height ) {
		return imageResolutionManager.getByDimensions( width, height );
	}

	@Override
	public List<ImageModification> getModifications( long imageId, long contextId ) {
		return Collections.unmodifiableList( imageModificationManager.getModifications( imageId, contextId ) );
	}

	@Override
	public List<ImageModification> getAllModifications( long imageId ) {
		return Collections.unmodifiableList( imageModificationManager.getAllModifications( imageId ) );
	}

	@Override
	public Collection<ImageResolution> getAllResolutions() {
		return imageResolutionManager.getAllResolutions();
	}

	@Override
	@Transactional
	public void saveImageResolution( ImageResolution resolution ) {
		if ( resolution == null ) {
			LOG.warn( "Null resolution not not allowed - ImageServiceImpl#saveImageResolution" );
		}

		imageResolutionManager.saveResolution( resolution );
	}

	private static class VariantImageRequest
	{
		private final long imageId;
		private final long contextId;
		private final ImageModificationDto modification;
		private final ImageResolution requestedResolution;
		private final ImageVariant variant;

		public VariantImageRequest( long imageId,
		                            long contextId,
		                            ImageModificationDto modification,
		                            ImageResolution requestedResolution, ImageVariant variant ) {
			this.imageId = imageId;
			this.contextId = contextId;
			this.modification = modification;
			this.requestedResolution = requestedResolution;
			this.variant = variant;
		}

		@Override
		public boolean equals( Object o ) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}

			VariantImageRequest that = (VariantImageRequest) o;

			if ( contextId != that.contextId ) {
				return false;
			}
			if ( imageId != that.imageId ) {
				return false;
			}
			if ( !modification.equals( that.modification ) ) {
				return false;
			}
			if ( requestedResolution != null ? !requestedResolution.equals(
					that.requestedResolution ) : that.requestedResolution != null ) {
				return false;
			}
			if ( !variant.equals( that.variant ) ) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			return Objects.hash( imageId, contextId, modification, requestedResolution, variant );
		}
	}

	private static class FutureVariantImage
	{
		private InMemoryImageSource result;
		private RuntimeException exception;
		private Error error;

		public synchronized StreamImageSource get() {
			try {
				while ( result == null && exception == null && error == null ) {
					wait();
				}

				if ( exception != null ) {
					throw exception;
				}

				if ( error != null ) {
					throw error;
				}

				return result.stream();
			}
			catch ( InterruptedException e ) {
				Thread.currentThread().interrupt();
				throw new VariantGenerationWasInterruptedException();
			}
		}

		public synchronized void setResult( InMemoryImageSource result ) {
			this.result = result;
			notifyAll();
		}

		public synchronized void setRuntimeException( RuntimeException e ) {
			this.exception = e;
			notifyAll();
		}

		public synchronized void setError( Error e ) {
			this.error = e;
			notifyAll();
		}
	}

	private static class VariantGenerationWasInterruptedException extends RuntimeException
	{
	}
}
