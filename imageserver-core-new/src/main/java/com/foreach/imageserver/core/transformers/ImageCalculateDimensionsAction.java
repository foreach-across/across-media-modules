package com.foreach.imageserver.core.transformers;

public class ImageCalculateDimensionsAction {
    private final StreamImageSource imageSource;

    public ImageCalculateDimensionsAction(StreamImageSource imageSource) {
        this.imageSource = imageSource;
    }

    public StreamImageSource getImageSource() {
        return imageSource;
    }
}
