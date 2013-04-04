package com.foreach.imageserver.services;

import com.foreach.imageserver.business.geometry.Point;
import com.foreach.imageserver.business.geometry.Rect;
import com.foreach.imageserver.business.geometry.Size;
import com.foreach.imageserver.business.image.Crop;
import com.foreach.imageserver.business.image.ServableImageData;
import com.foreach.imageserver.dao.CropDao;
import com.foreach.imageserver.dao.ImageDao;
import com.foreach.imageserver.dao.selectors.CropSelector;
import com.foreach.imageserver.dao.selectors.ImageSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ImageServiceImpl implements ImageService {

	@Autowired
    private ImageDao imageDao;

	@Autowired
	private CropDao cropDao;

    public final ServableImageData getImageById(long id) {
        return imageDao.getImageById(id);
    }

    public final ServableImageData getImageByPath(ImageSelector selector) {
        return imageDao.getImageByPath(selector);
    }

    public final List<ServableImageData> getAllImages() {
        return imageDao.getAllImages();
    }

	@Transactional
	public final long saveImage( ServableImageData image )
	{
		return saveImage( image, false );
	}

	@Transactional
    public final long saveImage( ServableImageData image, boolean deleteCrops )
    {
		if ( image.getId() > 0 ) {
			imageDao.updateImage( image );
			if ( deleteCrops ) {
				cullCrops( image.getId(), image.getSize() );
			}
		} else {
			imageDao.insertImage( image );
		}
		return image.getId();
    }

	private void cullCrops( long imageId, Size imageSize )
	{
		Rect boundingRect = new Rect( new Point(0, 0), imageSize );
		List<Crop> crops = cropDao.getCrops( CropSelector.onImageId( imageId ) );
		for( Crop crop : crops ) {
			if( ! crop.withinRect( boundingRect ) ) {
				cropDao.deleteCrop( crop.getId() );
			}
		}
	}

    public final List<ServableImageData> getImages(ImageSelector selector) {
        return imageDao.getImages(selector);
    }

    public final int getImageCount(ImageSelector selector) {
        return imageDao.getImageCount(selector);
    }
}
