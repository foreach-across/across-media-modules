package com.foreach.imageserver.core.transformers;

import com.foreach.imageserver.core.business.ImageType;

import java.io.InputStream;

public class ImageSource {
    private final ImageType imageType;
    private final InputStream imageStream;

    public ImageSource(ImageType imageType, InputStream imageStream) {
        this.imageType = imageType;
        this.imageStream = imageStream;
    }

    public ImageType getImageType() {
        return imageType;
    }

    public InputStream getImageStream() {
        return imageStream;
    }
}