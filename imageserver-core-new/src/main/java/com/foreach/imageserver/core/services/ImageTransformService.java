package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageType;

public interface ImageTransformService {
    Dimensions computeDimensions(ImageType imageType, byte[] imageBytes);
}
