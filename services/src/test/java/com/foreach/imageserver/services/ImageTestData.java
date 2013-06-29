package com.foreach.imageserver.services;

import com.foreach.imageserver.business.ImageType;
import com.foreach.imageserver.business.image.Dimensions;

public enum ImageTestData
{
	SUNSET( "/images/sunset.jpg", "image/jpeg", ImageType.JPEG, 1083349L, 1420, 930 ),
	EARTH( "/images/earth_large.jpg", "image/jpeg", ImageType.JPEG, 11803928L, 9104, 6828 );

	private final String resourcePath, contentType;
	private final ImageType imageType;
	private final long fileSize;
	private final Dimensions dimensions;

	ImageTestData( String resourcePath,
	               String contentType,
	               ImageType imageType,
	               long fileSize,
	               int width,
	               int height ) {
		this.resourcePath = resourcePath;
		this.contentType = contentType;
		this.imageType = imageType;
		this.fileSize = fileSize;
		this.dimensions = new Dimensions( width, height );
	}

	public String getContentType() {
		return contentType;
	}

	public ImageType getImageType() {
		return imageType;
	}

	public String getResourcePath() {
		return resourcePath;
	}

	public long getFileSize() {
		return fileSize;
	}

	public Dimensions getDimensions() {
		return dimensions;
	}
}
