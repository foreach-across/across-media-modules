package com.foreach.imageserver.core.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageContext;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.business.ImageVariant;
import com.foreach.imageserver.logging.LogHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import static com.foreach.imageserver.core.config.ServicesConfiguration.ORIGINALS_REPOSITORY;
import static com.foreach.imageserver.core.config.ServicesConfiguration.TEMP_REPOSITORY;
import static com.foreach.imageserver.core.config.ServicesConfiguration.VARIANTS_REPOSITORY;

@Slf4j
public class DefaultImageFileDescriptorFactory implements ImageFileDescriptorFactory
{
	@Override
	public FileDescriptor createForOriginal( Image image ) {
		String fileName = constructFileName( image );
		String targetPath = getFolderName( image );
		FileDescriptor fileDescriptor;
		String defaultTargetRepository = ORIGINALS_REPOSITORY;

		if ( image.isTemporaryImage() ) {
			defaultTargetRepository = TEMP_REPOSITORY;
		}

		return composeFileDescriptor( image, fileName, targetPath, defaultTargetRepository );
	}

	@Override
	public FileDescriptor createForVariant( Image image, ImageContext context, ImageResolution imageResolution, ImageVariant imageVariant ) {
		String fileName = constructFileName( image, imageResolution, imageVariant );
		String targetPath = getFolderName( image, context );

		return composeFileDescriptor( image, fileName, targetPath, VARIANTS_REPOSITORY );
	}

	private FileDescriptor composeFileDescriptor( Image image, String fileName, String targetPath, String defaultTargetRepository ) {
		FileDescriptor fileDescriptor;
		String[] targetPathParts = targetPath.split( ":" );
		boolean isFileDescriptor = targetPathParts.length > 1;


		// When we are not dealing with a fileDescriptor (Legacy) --> Create one
		if ( isFileDescriptor ) {
			String fileRepository = targetPathParts[0];
			String fileDescriptorPath = targetPathParts[1];

			fileDescriptor = FileDescriptor.of( fileRepository, fileDescriptorPath, fileName );
		}
		else {
			fileDescriptor = FileDescriptor.of( defaultTargetRepository, targetPath, fileName );
		}

		return fileDescriptor;
	}

	private String constructFileName( Image image, ImageResolution imageResolution, ImageVariant imageVariant ) {
		if ( image == null || imageResolution == null || imageVariant == null ) {
			LOG.warn(
					"Null parameters not allowed - ImageStoreServiceImpl#constructFileName: image={}, modification={}, imageVariant={}",
					LogHelper.flatten( image, imageResolution, imageVariant ) );
		}

		StringBuilder fileNameBuilder = new StringBuilder();
		fileNameBuilder.append( variantFileNamePrefix( image.getId() ) );
		//if (imageResolution.getWidth() != null) {
		fileNameBuilder.append( 'w' );
		fileNameBuilder.append( imageResolution.getWidth() );
		fileNameBuilder.append( '-' );
		//}
		//if (imageResolution.getHeight() != null) {
		fileNameBuilder.append( 'h' );
		fileNameBuilder.append( imageResolution.getHeight() );
		//}
		if ( imageVariant.getBoundaries() != null ) {
			fileNameBuilder.append( '-' );
			fileNameBuilder.append( "bw" );
			fileNameBuilder.append( imageVariant.getBoundaries().getWidth() );
			fileNameBuilder.append( '-' );
			fileNameBuilder.append( "bh" );
			fileNameBuilder.append( imageVariant.getBoundaries().getHeight() );
		}

		fileNameBuilder.append( '.' );
		fileNameBuilder.append( imageVariant.getOutputType().getExtension() );

		return fileNameBuilder.toString();
	}

	private String constructFileName( Image image ) {
		String name = image.isTemporaryImage() ? image.getExternalId() : String.valueOf( image.getId() );
		return name + '.' + image.getImageType().getExtension();
	}

	private String getFolderName( Image image ) {
		return image.isTemporaryImage() ? "" : image.getOriginalPath();
	}

	private String getFolderName( Image image,
	                              ImageContext context ) {
		return context.getCode() + "/" + image.getVariantPath();
	}

	private String variantFileNamePrefix( long imageId ) {
		return String.valueOf( imageId ) + '-';
	}

}
