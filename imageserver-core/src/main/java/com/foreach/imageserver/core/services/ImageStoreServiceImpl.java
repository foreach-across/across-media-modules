package com.foreach.imageserver.core.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import com.foreach.across.modules.filemanager.services.FileManager;
import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.services.exceptions.ImageStoreException;
import com.foreach.imageserver.core.transformers.ImageSource;
import com.foreach.imageserver.core.transformers.SimpleImageSource;
import com.foreach.imageserver.logging.LogHelper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageStoreServiceImpl implements ImageStoreService
{
	private final FileManager fileManager;
	private final DefaultImageFileDescriptorFactory defaultImageFileDescriptorFactory;

	@Override
	public void storeOriginalImage( Image image, byte[] imageBytes ) {
		try (InputStream imageStream = new ByteArrayInputStream( imageBytes )) {
			this.storeOriginalImage( image, imageStream );
		}
		catch ( Exception e ) {
			LOG.error(
					"Encountered failure while storing original image - ImageStoreServiceImpl#storeOriginalImage: image={}",
					LogHelper.flatten( image ), e );
			throw new RuntimeException( e );
		}
	}

	@Override
	public void storeOriginalImage( Image image, InputStream imageStream ) {
		writeSafely( imageStream, getOriginalFileDescriptor( image ) );
	}

	@Override
	public ImageSource getOriginalImage( Image image ) {
		FileDescriptor fileDescriptor = getOriginalFileDescriptor( image );

		if ( LOG.isDebugEnabled() ) {
			LOG.debug( "Original image {} - expected location {}", image.getId(), fileDescriptor );
		}

		return read( fileDescriptor, image.getImageType() );
	}

	@Override
	public void removeOriginal( Image image ) {
		LOG.info( "Deleting original image file for {}", image );
		FileDescriptor descriptor = getOriginalFileDescriptor( image );
		if ( fileManager.exists( descriptor ) ) {
			boolean deleted = fileManager.delete( descriptor );
			LOG.debug( "Original image file for {} was {} deleted", image, deleted ? "successfully" : "not" );
		}
		else {
			LOG.debug( "Original image file for {} does not exist.", image );
		}
	}

	private FileDescriptor getOriginalFileDescriptor( Image image ) {
		return defaultImageFileDescriptorFactory.createForOriginal( image );
	}

	@Override
	@SneakyThrows
	public void storeVariantImage( Image image,
	                               ImageContext context,
	                               ImageResolution imageResolution,
	                               ImageVariant imageVariant,
	                               ImageSource imageSource ) {
		if ( image == null || context == null || imageResolution == null || imageVariant == null || imageSource == null ) {
			LOG.warn(
					"Null parameters not allowed - ImageStoreServiceImpl#storeVariantImage: image={}, context={}, imageResolution={}, imageVariant={}, imageSource={}",
					LogHelper.flatten( image, context, imageResolution, imageVariant, imageSource ) );
		}
		try (InputStream is = imageSource.getImageStream()) {
			writeSafely( is, getVariantsFileDescriptor( image, context, imageResolution, imageVariant ) );
		}
	}

	@Override
	public ImageSource getVariantImage( Image image,
	                                    ImageContext context,
	                                    ImageResolution imageResolution,
	                                    ImageVariant imageVariant ) {
		if ( image == null || context == null || imageResolution == null || imageVariant == null ) {
			LOG.warn(
					"Null parameters not allowed - ImageStoreServiceImpl#getVariantImage: image={}, context={}, imageResolution={}, imageVariant={}",
					LogHelper.flatten( image, context, imageResolution, imageVariant ) );
		}
		FileDescriptor fileDescriptor = getVariantsFileDescriptor( image, context, imageResolution, imageVariant );
		return read( fileDescriptor, imageVariant.getOutputType() );
	}

	@Override
	public void removeVariantImage( Image image,
	                                ImageContext context,
	                                ImageResolution imageResolution,
	                                ImageVariant imageVariant ) {
		if ( image == null || context == null || imageResolution == null || imageVariant == null ) {
			LOG.warn(
					"Null parameters not allowed - ImageStoreServiceImpl#removeVariantImage: image={}, context={}, imageResolution={}, imageVariant={}",
					LogHelper.flatten( image, context, imageResolution, imageVariant ) );
		}

		FileDescriptor descriptor = getVariantsFileDescriptor( image, context, imageResolution, imageVariant );
		FileResource fileResource = fileManager.getFileResource( descriptor );

		if ( fileResource.exists() ) {
			boolean deleted = fileResource.delete();
			LOG.debug( "Original image file for {} was {} deleted", image, deleted ? "successfully" : "not" );
		}
		else {
			LOG.debug( "Original image file for {} does not exist.", image );
		}
	}

	private FileDescriptor getVariantsFileDescriptor( Image image,
	                                                  ImageContext context,
	                                                  ImageResolution imageResolution, ImageVariant imageVariant ) {
		return defaultImageFileDescriptorFactory.createForVariant( image, context, imageResolution, imageVariant );
	}

	//TODO currently not supported as it is not supported by FileManagerModule
	@Override
	public void removeVariants( Long imageId ) {
	}

	private void writeSafely( InputStream inputStream, FileDescriptor target ) {
		try {
			FileResource fileResource = fileManager.getFileResource( target );
			fileResource.copyFrom( inputStream );
		}
		catch ( Exception e ) {
			LOG.error( "Error while creating file resource - ImageStoreServiceImpl#writeSafely: targetPath={}", target,
			           e );
			throw new ImageStoreException( e );
		}
	}

	private ImageSource read( FileDescriptor fileDescriptor, ImageType imageType ) {
		FileResource fileResource = fileManager.getFileResource( fileDescriptor );

		if ( fileResource.exists() ) {
			return new SimpleImageSource( imageType, fileResource );
		}

		return null;
	}
}
