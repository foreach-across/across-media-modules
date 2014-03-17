package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.business.ImageVariant;
import com.foreach.imageserver.core.business.OriginalImage;
import com.foreach.imageserver.core.transformers.StreamImageSource;

import java.io.InputStream;

public interface ImageStoreService {
    void storeOriginalImage(OriginalImage originalImage, byte[] imageBytes);

    void storeOriginalImage(OriginalImage originalImage, InputStream imageStream);

    StreamImageSource getOriginalImage(OriginalImage originalImage);

    void storeVariantImage(Image image, ImageResolution imageResolution, ImageVariant imageVariant, StreamImageSource imageSource);

    StreamImageSource getVariantImage(Image image, ImageResolution imageResolution, ImageVariant imageVariant);
}
