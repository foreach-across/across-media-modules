package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageFile;
import com.foreach.imageserver.core.business.ImageVariant;

public interface ImageStoreService {
    String generateRelativeImagePath(Image image);

    String generateFullImagePath(Image image);

    String generateFullImagePath(Image image, ImageVariant modifier);

    ImageFile saveImage(Image image, ImageFile imageFile);

    ImageFile saveImage(Image image, ImageVariant modifier, ImageFile file);

    void delete(Image image);

    void deleteVariants(Image image);

    ImageFile getImageFile(Image image);

    ImageFile getImageFile(Image image, ImageVariant modifier);
}
