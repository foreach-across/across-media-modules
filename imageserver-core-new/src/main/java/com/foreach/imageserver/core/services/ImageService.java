package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageModification;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.business.ImageVariant;
import com.foreach.imageserver.core.transformers.StreamImageSource;

import java.util.Map;

public interface ImageService {
    Image getById(int applicationId, int imageId);

    void saveImage(int applicationId, int imageId, OriginalImageRepository imageRepository, Map<String, String> repositoryParameters);

    void saveImageModification(ImageModification modification);

    StreamImageSource getVariantImage(Image image, ImageResolution imageResolution, ImageVariant imageVariant);
}
