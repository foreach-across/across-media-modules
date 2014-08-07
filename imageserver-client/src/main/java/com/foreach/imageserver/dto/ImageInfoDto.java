package com.foreach.imageserver.dto;

import org.apache.commons.io.FileUtils;

import java.util.Date;
import java.util.Objects;

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

	public boolean isExisting() {
		return existing;
	}

	public void setExisting( boolean existing ) {
		this.existing = existing;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId( String externalId ) {
		this.externalId = externalId;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated( Date created ) {
		this.created = created;
	}

	public ImageTypeDto getImageType() {
		return imageType;
	}

	public void setImageType( ImageTypeDto imageType ) {
		this.imageType = imageType;
	}

	public DimensionsDto getDimensionsDto() {
		return dimensionsDto;
	}

	public void setDimensionsDto( DimensionsDto dimensionsDto ) {
		this.dimensionsDto = dimensionsDto;
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

	public void setImageFileSize( long imageFileSize ) {
		this.imageFileSize = imageFileSize;
	}

	public long getImageFileSize() {
		return imageFileSize;
	}

	public String getReadableFileSize() {
		return FileUtils.byteCountToDisplaySize( imageFileSize );
	}
}
