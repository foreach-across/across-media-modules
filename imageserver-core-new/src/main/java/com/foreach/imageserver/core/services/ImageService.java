package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.transformers.StreamImageSource;

import java.util.List;
import java.util.Map;

public interface ImageService {
    Image getById(int imageId);

    ImageSaveResult saveImage(ImageRepository imageRepository, Map<String, String> repositoryParameters) throws ImageStoreException;

    void saveImageModification(ImageModification modification);

    StreamImageSource getVariantImage(Image image, Context context, ImageResolution imageResolution, ImageVariant imageVariant);

    boolean hasModification(int imageId);

    ImageResolution getResolution(int resolutionId);

    List<ImageModification> getModifications(int imageId, int contextId);
}
