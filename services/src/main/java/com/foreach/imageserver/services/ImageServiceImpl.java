package com.foreach.imageserver.services;

import com.foreach.imageserver.business.Image;
import com.foreach.imageserver.business.ImageFile;
import com.foreach.imageserver.business.geometry.Point;
import com.foreach.imageserver.business.geometry.Rect;
import com.foreach.imageserver.business.geometry.Size;
import com.foreach.imageserver.business.image.Crop;
import com.foreach.imageserver.business.image.ServableImageData;
import com.foreach.imageserver.dao.CropDao;
import com.foreach.imageserver.dao.ImageDao;
import com.foreach.imageserver.dao.selectors.CropSelector;
import com.foreach.imageserver.dao.selectors.ImageSelector;
import com.foreach.imageserver.services.repositories.RepositoryLookupResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ImageServiceImpl implements ImageService
{
	@Autowired
	private ImageDao imageDao;

	@Autowired
	private ImageStoreService imageStoreService;

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

		long savedFileSize = imageStoreService.saveImage( image, lookupResult.getContent() );

		image.setFileSize( savedFileSize );

		imageDao.updateImage( image );
		if ( !isInsert ) {
			imageStoreService.deleteVariants( image );
		}
	}

	private boolean isNewImage( Image image ) {
		return image.getId() <= 0;
	}

	@Override
	public ImageFile fetchImageFile( Image image ) {
		return imageStoreService.getImageFile( image );
	}

	@Transactional
	@Override
	public void delete( Image image ) {
		// First delete the database entry - this avoids requests coming in
		imageDao.deleteImage( image.getId() );

		// Delete the actual physical phyles
		imageStoreService.delete( image );
	}

	@Deprecated
	public final ServableImageData getImageById( long id ) {
		return imageDao.getImageById( id );
	}

	@Deprecated
	public final ServableImageData getImageByPath( ImageSelector selector ) {
		return imageDao.getImageByPath( selector );
	}

	@Deprecated
	public final List<ServableImageData> getAllImages() {
		return imageDao.getAllImages();
	}

	@Deprecated
	@Transactional
	public final long saveImage( ServableImageData image ) {
		return saveImage( image, false );
	}

	@Deprecated
	@Transactional
	public final long saveImage( ServableImageData image, boolean deleteCrops ) {
		if ( image.getId() > 0 ) {
			imageDao.updateImage( image );
			if ( deleteCrops ) {
				cullCrops( image.getId(), image.getSize() );
			}
		}
		else {
			imageDao.insertImage( image );
		}
		return image.getId();
	}

	private void cullCrops( long imageId, Size imageSize ) {
		Rect boundingRect = new Rect( new Point( 0, 0 ), imageSize );
		List<Crop> crops = cropDao.getCrops( CropSelector.onImageId( imageId ) );
		for ( Crop crop : crops ) {
			if ( !crop.withinRect( boundingRect ) ) {
				cropDao.deleteCrop( crop.getId() );
			}
		}
	}

	@Deprecated
	public final List<ServableImageData> getImages( ImageSelector selector ) {
		return imageDao.getImages( selector );
	}

	@Deprecated
	public final int getImageCount( ImageSelector selector ) {
		return imageDao.getImageCount( selector );
	}
}
