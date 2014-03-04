package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageFile;
import com.foreach.imageserver.core.business.ImageVariant;

public interface ImageTransformService {
    Dimensions calculateDimensions(ImageFile file);

    ImageFile apply(Image image, ImageVariant modifier);
}
