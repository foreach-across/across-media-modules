package com.foreach.imageserver.core.transformers;

public class ImageCalculateDimensionsAction
{
	private final SimpleImageSource imageSource;

	public ImageCalculateDimensionsAction( SimpleImageSource imageSource ) {
		this.imageSource = imageSource;
	}

	public SimpleImageSource getImageSource() {
		return imageSource;
	}
}
