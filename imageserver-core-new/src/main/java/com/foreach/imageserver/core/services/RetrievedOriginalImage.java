package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.OriginalImage;

public class RetrievedOriginalImage {
    private final OriginalImage originalImage;
    private final byte[] imageBytes;

    public RetrievedOriginalImage(OriginalImage originalImage, byte[] imageBytes) {
        this.originalImage = originalImage;
        this.imageBytes = imageBytes;
    }

    public OriginalImage getOriginalImage() {
        return originalImage;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }
}
