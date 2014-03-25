package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.transformers.StreamImageSource;

import java.io.InputStream;

public interface ImageStoreService {
    void storeOriginalImage(ImageParameters imageParameters, byte[] imageBytes);

    void storeOriginalImage(ImageParameters imageParameters, InputStream imageStream);

    StreamImageSource getOriginalImage(ImageParameters imageParameters);

    void storeVariantImage(Image image, Context context, ImageResolution imageResolution, ImageVariant imageVariant, InputStream imageStream);

    StreamImageSource getVariantImage(Image image, Context context, ImageResolution imageResolution, ImageVariant imageVariant);
}
