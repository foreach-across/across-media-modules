package com.foreach.imageserver.dao;

import com.foreach.imageserver.business.image.ServableImageData;
import com.foreach.imageserver.dao.selectors.ImageSelector;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageDao
{
    ServableImageData getImageById( long id );

    ServableImageData getImageByPath( ImageSelector selector);

    int getImageCount(ImageSelector selector);

    List<ServableImageData> getAllImages();

    void insertImage( ServableImageData image );

    void updateImage( ServableImageData image );

    void deleteImage( long imageId );

    List<ServableImageData> getImages(ImageSelector selector);
}
