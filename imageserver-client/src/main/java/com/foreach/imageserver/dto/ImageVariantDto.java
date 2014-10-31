package com.foreach.imageserver.dto;

public class ImageVariantDto
{
	private ImageTypeDto imageType;

	private DimensionsDto bounderies;

	public ImageVariantDto() {
	}

	public ImageVariantDto( ImageTypeDto imageType ) {
		this.imageType = imageType;
	}

	public ImageVariantDto( DimensionsDto bounderies ) {
		this.bounderies = bounderies;
	}

	public ImageTypeDto getImageType() {
		return imageType;
	}

	public void setImageType( ImageTypeDto imageType ) {
		this.imageType = imageType;
	}

	public DimensionsDto getBounderies() {
		return bounderies;
	}

	public void setBounderies( DimensionsDto bounderies ) {
		this.bounderies = bounderies;
	}
}
