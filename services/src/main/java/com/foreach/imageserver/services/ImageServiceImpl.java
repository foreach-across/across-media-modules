package com.foreach.imageserver.services;

import com.foreach.imageserver.business.Image;
import com.foreach.imageserver.business.ImageFile;
import com.foreach.imageserver.business.ImageModifier;
import com.foreach.imageserver.dao.CropDao;
import com.foreach.imageserver.dao.ImageDao;
import com.foreach.imageserver.services.repositories.RepositoryLookupResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ImageServiceImpl implements ImageService
{
	@Autowired
	private ImageDao imageDao;

	@Autowired
	private ImageStoreService imageStoreService;

	@Autowired
	private ImageModificationService imageModificationService;

	@Autowired
	private CropDao cropDao;

	public Image getImageByKey( String key, int applicationId ) {
		return imageDao.getImageByKey( key, applicationId );
	}

	@Transactional
	@Override
	public void save( Image image, RepositoryLookupResult lookupResult ) {
		image.setDimensions( lookupResult.getDimensions() );
		image.setImageType( lookupResult.getImageType() );

		boolean isInsert = isNewImage( image );

		if ( isInsert ) {
			image.setFilePath( imageStoreService.generateRelativeImagePath( image ) );
			imageDao.insertImage( image );
		}

		ImageFile savedFile = imageStoreService.saveImage( image, lookupResult.getContent() );

		image.setFileSize( savedFile.getFileSize() );

		imageDao.updateImage( image );
		if ( !isInsert ) {
			imageStoreService.deleteVariants( image );
		}
	}

	private boolean isNewImage( Image image ) {
		return image.getId() <= 0;
	}

	@Override
	public ImageFile fetchImageFile( Image image, ImageModifier modifier ) {
		ImageFile file = imageStoreService.getImageFile( image, modifier );

		if ( file == null ) {
			ImageFile original = imageStoreService.getImageFile( image );
			ImageFile modified = imageModificationService.apply( original, modifier );

			file = imageStoreService.saveImageFile( image, modifier, modified );
		}

		return file;
	}

	@Transactional
	@Override
	public void delete( Image image ) {
		// First delete the database entry - this avoids requests coming in
		imageDao.deleteImage( image.getId() );

		// Delete the actual physical phyles
		imageStoreService.delete( image );
	}
}
