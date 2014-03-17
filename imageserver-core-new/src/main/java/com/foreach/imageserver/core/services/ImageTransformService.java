package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.transformers.InMemoryImageSource;
import com.foreach.imageserver.core.transformers.StreamImageSource;

public interface ImageTransformService {
    Dimensions computeDimensions(ImageType imageType, byte[] imageBytes);

    InMemoryImageSource modify(StreamImageSource imageSource,
                               int outputWidth,
                               int outputHeight,
                               int cropX,
                               int cropY,
                               int cropWidth,
                               int cropHeight,
                               int densityWidth,
                               int densityHeight,
                               ImageType outputType);
}
