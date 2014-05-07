package com.foreach.imageserver.core.transformers;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageType;

public class ImageAttributes {
    private final ImageType type;
    private final Dimensions dimensions;

    public ImageAttributes(ImageType type, Dimensions dimensions) {
        this.type = type;
        this.dimensions = dimensions;
    }

    public ImageType getType() {
        return type;
    }

    public Dimensions getDimensions() {
        return dimensions;
    }
}
