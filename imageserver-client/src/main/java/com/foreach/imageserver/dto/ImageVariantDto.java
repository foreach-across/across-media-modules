package com.foreach.imageserver.dto;

public class ImageVariantDto
{
	private ImageTypeDto imageType;

	private DimensionsDto boundaries;

	public ImageVariantDto() {
	}

	public ImageVariantDto( ImageTypeDto imageType ) {
		this.imageType = imageType;
	}

	public ImageVariantDto( DimensionsDto boundaries ) {
		this.boundaries = boundaries;
	}

	public ImageVariantDto( ImageTypeDto imageType, DimensionsDto boundaries ) {
		this.imageType = imageType;
		this.boundaries = boundaries;
	}

	public ImageTypeDto getImageType() {
		return imageType;
	}

	public void setImageType( ImageTypeDto imageType ) {
		this.imageType = imageType;
	}

	public DimensionsDto getBoundaries() {
		return boundaries;
	}

	public void setBoundaries( DimensionsDto boundaries ) {
		this.boundaries = boundaries;
	}
}
