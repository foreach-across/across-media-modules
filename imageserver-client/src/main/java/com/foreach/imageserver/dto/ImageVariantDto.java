package com.foreach.imageserver.dto;

import java.util.Objects;

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

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( !( o instanceof ImageVariantDto ) ) {
			return false;
		}
		ImageVariantDto that = (ImageVariantDto) o;
		return imageType == that.imageType &&
				Objects.equals( boundaries, that.boundaries );
	}

	@Override
	public int hashCode() {
		return Objects.hash( imageType, boundaries );
	}
}
