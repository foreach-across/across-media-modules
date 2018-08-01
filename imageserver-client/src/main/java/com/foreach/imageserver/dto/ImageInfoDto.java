package com.foreach.imageserver.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Objects;

@Getter
@Setter
public class ImageInfoDto
{
	private boolean existing;
	private String externalId;
	private Date created;
	private ImageTypeDto imageType;
	private DimensionsDto dimensionsDto;
	private long imageFileSize;

	public ImageInfoDto() {
	}


	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		ImageInfoDto that = (ImageInfoDto) o;

		return Objects.equals( existing, that.existing )
				&& Objects.equals( created, that.created )
				&& Objects.equals( dimensionsDto, that.dimensionsDto )
				&& Objects.equals( externalId, that.externalId )
				&& Objects.equals( imageType, that.imageType );
	}

	@Override
	public int hashCode() {
		return Objects.hash( existing, externalId, created, imageType, dimensionsDto );
	}
}
