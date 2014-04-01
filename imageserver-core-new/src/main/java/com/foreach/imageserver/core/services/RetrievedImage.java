package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageType;

public class RetrievedImage {
    private final Dimensions dimensions;
    private final ImageType imageType;
    private final byte[] imageBytes;

    public RetrievedImage(Dimensions dimensions, ImageType imageType, byte[] imageBytes) {
        this.dimensions = dimensions;
        this.imageType = imageType;
        this.imageBytes = imageBytes;
    }

    public Dimensions getDimensions() {
        return dimensions;
    }

    public ImageType getImageType() {
        return imageType;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }
}
