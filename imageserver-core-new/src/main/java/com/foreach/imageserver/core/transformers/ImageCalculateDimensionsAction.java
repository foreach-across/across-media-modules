package com.foreach.imageserver.core.transformers;

public class ImageCalculateDimensionsAction {
    private final ImageSource imageSource;

    public ImageCalculateDimensionsAction(ImageSource imageSource) {
        this.imageSource = imageSource;
    }

    public ImageSource getImageSource() {
        return imageSource;
    }
}
