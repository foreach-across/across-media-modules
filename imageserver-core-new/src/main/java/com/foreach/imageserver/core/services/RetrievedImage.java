package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.ImageParameters;

public class RetrievedImage {
    private final ImageParameters imageParameters;
    private final byte[] imageBytes;

    public RetrievedImage(ImageParameters imageParameters, byte[] imageBytes) {
        this.imageParameters = imageParameters;
        this.imageBytes = imageBytes;
    }

    public ImageParameters getImageParameters() {
        return imageParameters;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }
}
