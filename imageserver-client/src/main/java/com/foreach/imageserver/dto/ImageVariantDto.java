package com.foreach.imageserver.dto;

public class ImageVariantDto
{
	private ImageTypeDto imageType;

	private DimensionsDto boundingBox;

	public ImageVariantDto() {
	}

	public ImageVariantDto( ImageTypeDto imageType ) {
		this.imageType = imageType;
	}

	public ImageTypeDto getImageType() {
		return imageType;
	}

	public void setImageType( ImageTypeDto imageType ) {
		this.imageType = imageType;
	}

	public DimensionsDto getBoundingBox() {
		return boundingBox;
	}

	public void setBoundingBox( DimensionsDto boundingBox ) {
		this.boundingBox = boundingBox;
	}
}
