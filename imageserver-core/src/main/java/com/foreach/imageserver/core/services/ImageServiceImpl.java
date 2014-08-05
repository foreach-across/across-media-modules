package com.foreach.imageserver.core.services;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.logging.LogHelper;
import com.foreach.imageserver.core.managers.ContextManager;
import com.foreach.imageserver.core.managers.ImageManager;
import com.foreach.imageserver.core.managers.ImageModificationManager;
import com.foreach.imageserver.core.managers.ImageResolutionManager;
import com.foreach.imageserver.core.transformers.ImageAttributes;
import com.foreach.imageserver.core.transformers.InMemoryImageSource;
import com.foreach.imageserver.core.transformers.StreamImageSource;
import com.foreach.imageserver.dto.CropDto;
import com.foreach.imageserver.dto.DimensionsDto;
import com.foreach.imageserver.dto.ImageModificationDto;
import com.foreach.imageserver.dto.ImageResolutionDto;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.util.*;

@Service
@Exposed
public class ImageServiceImpl implements ImageService
{

	private static Logger LOG = LoggerFactory.getLogger( ImageServiceImpl.class );

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
	private ImageResolutionManager imageResolutionManager;

	@Autowired
	private ContextManager contextManager;

	@Override
	public Image getById( long imageId ) {
		return imageManager.getById( imageId );
	}

	@Override
	public Image getByExternalId( String externalId ) {
		return imageManager.getByExternalId( externalId );
	}

	// Used concurrently from multiple threads.
	private Map<VariantImageRequest, FutureVariantImage> futureVariantImages = new HashMap<>();

	// TODO I'm not taking care of errors right now, make sure to tackle this later on!
	@Override
	@Transactional
	public Dimensions saveImage( String externalId, byte[] imageBytes, Date imageDate ) throws ImageStoreException {
		if ( StringUtils.isBlank( externalId ) || imageBytes == null || imageDate == null ) {
			LOG.warn( "Null parameters not allowed - ImageServiceImpl#saveImage: image={}, context={}, imageDate={}",
			          LogHelper.flatten( externalId, imageBytes, imageDate ) );
		}

		ImageAttributes imageAttributes = imageTransformService.getAttributes( new ByteArrayInputStream( imageBytes ) );

		Image image = new Image();
		image.setExternalId( externalId );
		image.setDateCreated( imageDate );
		image.setDimensions( imageAttributes.getDimensions() );
		image.setImageType( imageAttributes.getType() );
		imageManager.insert( image );

		imageStoreService.storeOriginalImage( image, imageBytes );

		return image.getDimensions();
	}

	/**
	 * DO NOT MAKE THIS METHOD TRANSACTIONAL! If we are updating an existing modification, we need to make sure that
	 * the changes are committed to the database *before* we clean up the filesystem. Otherwise a different instance
	 * might recreate variants on disk using the old values.
	 */
	@Override
	public void saveImageModification( ImageModification modification ) {
		Image image = getById( modification.getImageId() );
		saveImageModification( modification, image );
	}

	@Override
	public void saveImageModification( ImageModification modification, Image image ) {
		if ( modification == null ) {
			LOG.warn( "Null parameters not allowed - ImageServiceImpl#saveImageModification: modification={}",
			          LogHelper.flatten( modification ) );
		}

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

		imageStoreService.removeVariants( modification.getImageId() );
	}

	@Override
	public StreamImageSource getVariantImage( Image image,
	                                          Context context,
	                                          ImageResolution imageResolution,
	                                          ImageVariant imageVariant ) {
		if ( image == null || context == null || imageResolution == null || imageVariant == null ) {
			LOG.warn(
					"Null parameters not allowed - ImageServiceImpl#getVariantImage: image={}, context={}, imageResolution={}, imageVariant={}",
					LogHelper.flatten( image, context, imageResolution, imageVariant ) );
		}

		ImageModificationDto modification = cropGenerator.buildModificationDto( image, context, imageResolution );
		StreamImageSource imageSource = imageStoreService.getVariantImage( image, context, modification, imageVariant );
		if ( imageSource == null ) {
			imageSource = generateVariantImage( image, context, modification, imageVariant, true );

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
				imageStoreService.removeVariantImage( image, context, modification, imageVariant );
			}

		}
		return imageSource;
	}

	@Override
	public StreamImageSource generateModification( Image image,
	                                               ImageModificationDto modificationDto,
	                                               ImageVariant imageVariant ) {
		CropGeneratorUtil.normalizeModificationDto( image, modificationDto );
		return generateVariantImage( image, null, modificationDto, imageVariant, false );
	}

	/**
	 * We allow just one thread to generate a specific variant. Other threads that require this variant simultaneously
	 * will block and re-use the same result.
	 */
	private StreamImageSource generateVariantImage( Image image,
	                                                Context context,
	                                                ImageModificationDto imageModification,
	                                                ImageVariant imageVariant,
	                                                boolean storeImage ) {
		if ( image == null || context == null || imageModification == null || imageVariant == null ) {
			LOG.warn(
					"Null parameters not allowed - ImageServiceImpl#generateVariantImage: image={}, context={}, imageModification={}, imageVariant={}, storeImage={}",
					LogHelper.flatten( image, context, imageModification, imageVariant, storeImage ) );
		}

		VariantImageRequest request =
				new VariantImageRequest( image.getId(), context != null ? context.getId() : 0, imageModification,
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
						generateVariantImageInCurrentThread( image, context, imageModification, imageVariant,
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
					removeImageVariantFile( image, context, imageModification, imageVariant );
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
	                                     Context context,
	                                     ImageModificationDto modification,
	                                     ImageVariant imageVariant ) {
		try {
			imageStoreService.removeVariantImage( image, context, modification, imageVariant );
		}
		catch ( Exception e ) {
			LOG.error(
					"Encountered error while trying to remove an image variant file due to errors during its creation - ImageServiceImpl#generateVariantImage: image={}, context={}, modification={}, imageVariant={}, storeImage={}",
					LogHelper.flatten( image ), LogHelper.flatten( context ), LogHelper.flatten( modification ),
					LogHelper.flatten( imageVariant ), e );
		}
	}

	private InMemoryImageSource generateVariantImageInCurrentThread( Image image,
	                                                                 Context context,
	                                                                 ImageModificationDto modificationDto,
	                                                                 ImageVariant imageVariant,
	                                                                 boolean storeImage ) {
		if ( image == null || context == null || modificationDto == null || imageVariant == null ) {
			LOG.warn(
					"Null parameters not allowed - ImageServiceImpl#generateVariantImageInCurrentThread: image={}, context={}, modificationDto={}, imageVariant={}, storeImage={}",
					LogHelper.flatten( image, context, modificationDto, imageVariant, storeImage ) );
		}

		ImageResolution imageResolution = new ImageResolution();
		imageResolution.setWidth( modificationDto.getResolution().getWidth() );
		imageResolution.setHeight( modificationDto.getResolution().getHeight() );

		ImageResolutionDto outputDimensions = modificationDto.getResolution();
		CropDto crop = modificationDto.getCrop();
		DimensionsDto density = modificationDto.getDensity();

		StreamImageSource originalImageSource = imageStoreService.getOriginalImage( image );
		if ( originalImageSource == null ) {
			throw new ImageCouldNotBeRetrievedException( "The original image is not available on disk." );
		}

		InMemoryImageSource variantImageSource =
				imageTransformService.modify( originalImageSource, outputDimensions.getWidth(),
				                              outputDimensions.getHeight(), crop.getX(), crop.getY(), crop.getWidth(),
				                              crop.getHeight(), density.getWidth(), density.getHeight(),
				                              imageVariant.getOutputType() );

		// TODO We might opt to catch exceptions here and not fail on the write. We can return the variant in memory regardless.
		if ( storeImage ) {
			imageStoreService.storeVariantImage( image, context, modificationDto, imageVariant,
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
		return new ArrayList<>( imageModificationManager.getModifications( imageId, contextId ) );
	}

	@Override
	public List<ImageResolution> getAllResolutions() {
		return imageResolutionManager.getAllResolutions();
	}

	@Override
	@Transactional
	public void saveImageResolution( ImageResolution resolution, Collection<Context> contexts ) {
		if ( resolution == null || contexts == null ) {
			LOG.warn( "Null parameters not allowed - ImageServiceImpl#saveImageResolution: resolution={}, contexts={}}",
			          LogHelper.flatten( resolution, contexts ) );
		}

		imageResolutionManager.saveResolution( resolution );
		contextManager.updateContextsForResolution( resolution.getId(), contexts );
	}

	private static class VariantImageRequest
	{
		private final long imageId;
		private final long contextId;
		private final ImageModificationDto modification;
		private final ImageVariant variant;

		public VariantImageRequest( long imageId,
		                            long contextId,
		                            ImageModificationDto modification,
		                            ImageVariant variant ) {
			this.imageId = imageId;
			this.contextId = contextId;
			this.modification = modification;
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
			if ( !variant.equals( that.variant ) ) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			return Objects.hash( imageId, contextId, modification, variant );
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
