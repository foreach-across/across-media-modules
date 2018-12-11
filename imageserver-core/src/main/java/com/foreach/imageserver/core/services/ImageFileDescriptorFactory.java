package com.foreach.imageserver.core.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageContext;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.business.ImageVariant;
import com.foreach.imageserver.logging.LogHelper;
import lombok.NonNull;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
public interface ImageFileDescriptorFactory
{
	/**
	 * Create a file descriptor for the original file of an image.
	 *
	 * @param image to get the file descriptor for
	 * @return file descriptor
	 */
	FileDescriptor createForOriginal( Image image );

	/**
	 * Create a file descriptor for a variant of an image.
	 *
	 * @param image to get the file descriptor for
	 * @return file descriptor
	 */
	FileDescriptor createForVariant( Image image, ImageContext context, ImageResolution imageResolution, ImageVariant imageVariant );

	/**
	 * Default implementation for generating only the file name aspect of a descriptor, based
	 * on the variant of an image being requested.
	 *
	 * @param image for which the file name should be generated
	 * @param context that is being requested
	 * @param imageResolution resolution being requested
	 * @param imageVariant variant being requested
	 * @return file name
	 */
	default String generateFileName( @NonNull Image image, ImageContext context, ImageResolution imageResolution, ImageVariant imageVariant ) {
//		if ( image == null || imageResolution == null || imageVariant == null ) {
//			LOG.warn(
//					"Null parameters not allowed - ImageStoreServiceImpl#constructFileName: image={}, modification={}, imageVariant={}",
//					LogHelper.flatten( image, imageResolution, imageVariant ) );
//		}

		StringBuilder fileNameBuilder = new StringBuilder();
		fileNameBuilder.append( String.valueOf( image.getId() ) ).append( '-' );
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
}
