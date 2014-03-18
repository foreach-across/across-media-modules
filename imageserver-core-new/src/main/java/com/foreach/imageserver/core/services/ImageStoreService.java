package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.business.ImageVariant;
import com.foreach.imageserver.core.business.ImageParameters;
import com.foreach.imageserver.core.transformers.StreamImageSource;

import java.io.InputStream;

public interface ImageStoreService {
    void storeOriginalImage(ImageParameters imageParameters, byte[] imageBytes);

    void storeOriginalImage(ImageParameters imageParameters, InputStream imageStream);

    StreamImageSource getOriginalImage(ImageParameters imageParameters);

    void storeVariantImage(Image image, ImageResolution imageResolution, ImageVariant imageVariant, StreamImageSource imageSource);

    StreamImageSource getVariantImage(Image image, ImageResolution imageResolution, ImageVariant imageVariant);
}
