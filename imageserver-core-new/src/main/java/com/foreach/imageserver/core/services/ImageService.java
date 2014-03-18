package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.transformers.StreamImageSource;

import java.util.Map;

public interface ImageService {
    Image getById(int imageId);

    void saveImage(int imageId, ImageRepository imageRepository, Map<String, String> repositoryParameters) throws ImageStoreException;

    void saveImageModification(ImageModification modification);

    StreamImageSource getVariantImage(Image image, Context context, ImageResolution imageResolution, ImageVariant imageVariant);
}
