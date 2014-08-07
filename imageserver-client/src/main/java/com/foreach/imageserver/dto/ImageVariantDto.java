package com.foreach.imageserver.dto;

public class ImageVariantDto
{
	private ImageTypeDto imageType;

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
}
