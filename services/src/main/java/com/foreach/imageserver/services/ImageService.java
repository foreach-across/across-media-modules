package com.foreach.imageserver.services;

import com.foreach.imageserver.business.image.ServableImageData;
import com.foreach.imageserver.dao.selectors.ImageSelector;

import java.util.List;

public interface ImageService {
    ServableImageData getImageById(long id);

    ServableImageData getImageByPath(ImageSelector selector);

    List<ServableImageData> getAllImages();

    int getImageCount(ImageSelector selector);

    long saveImage(ServableImageData image);

	long saveImage(ServableImageData image, boolean deleteCrops);

    List<ServableImageData> getImages(ImageSelector selector);
}
