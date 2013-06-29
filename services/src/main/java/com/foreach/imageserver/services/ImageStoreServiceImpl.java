package com.foreach.imageserver.services;

import com.foreach.imageserver.business.image.ServableImageData;
import com.foreach.imageserver.services.paths.ImagePathBuilder;
import com.foreach.imageserver.services.paths.ImageSpecifier;
import com.foreach.imageserver.services.paths.ImageVersion;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;

@Service
public class ImageStoreServiceImpl implements ImageStoreService
{
	private static final Logger LOG = Logger.getLogger( ImageStoreServiceImpl.class );

	@Autowired
	private ImagePathBuilder pathBuilder;

	public final void saveImage( ServableImageData image, MultipartFile imageData )
	{
		ImageSpecifier imageSpecifier = new ImageSpecifier();
		imageSpecifier.setImageId( image.getId() );
		imageSpecifier.setFileType( image.getExtension() );

		int applicationId = image.getApplicationId();
		int groupId = image.getGroupId();

		String physicalPath = pathBuilder.createManualImagePath( ImageVersion.ORIGINAL, applicationId, groupId, new Date(),
		                                                         imageSpecifier );
		saveImageOnDisk( physicalPath, imageData );
	}

	public final void replaceImage( ServableImageData image, MultipartFile imageData, String oldExtension )
	{
		String newExtension = image.getExtension();

		if( !newExtension.equals( oldExtension ) ) {
			image.setExtension( oldExtension );
			deleteImage( image );
			image.setExtension( newExtension );
		}

		String physicalPath = pathBuilder.generateOriginalImagePath( image );
		saveImageOnDisk( physicalPath, imageData );
	}

	public final void deleteImage( ServableImageData image )
	{
		String physicalPath = pathBuilder.generateOriginalImagePath( image );
		deleteImageOnDisk( physicalPath );
	}

	private void saveImageOnDisk( String fileName, MultipartFile imageData )
	{
		try {
			File f = new File( fileName );
			FileUtils.writeByteArrayToFile( f, imageData.getBytes() );
		}
		catch ( IOException e ) {
			LOG.error( "Problem writing file to disk.", e );
		}
	}

	private void deleteImageOnDisk( String fileName )
	{
		File f = new File( fileName );
		if ( !f.delete() ) {
			LOG.error( "Problem erasing file from disk: " + fileName );
		}
	}

}
