package com.foreach.imageserver.dto;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class ImageResolutionDto
{
	private long id;
	private boolean configurable, pregenerateVariants;
	private String name;
	private int width;
	private int height;
	private Set<ImageTypeDto> allowedOutputTypes = EnumSet.noneOf( ImageTypeDto.class );
	private Set<String> tags = new HashSet<String>();

	public ImageResolutionDto() {
	}

	public ImageResolutionDto( ImageResolutionDto original ) {
		width = original.width;
		height = original.height;
	}

	public ImageResolutionDto( int width, int height ) {
		this.width = width;
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth( int width ) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight( int height ) {
		this.height = height;
	}

	public boolean isConfigurable() {
		return configurable;
	}

	public void setConfigurable( boolean configurable ) {
		this.configurable = configurable;
	}

	public boolean isPregenerateVariants() {
		return pregenerateVariants;
	}

	public void setPregenerateVariants( boolean pregenerateVariants ) {
		this.pregenerateVariants = pregenerateVariants;
	}

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
	}

	public Set<String> getTags() {
		return tags;
	}

	public void setTags( Set<String> tags ) {
		this.tags = tags;
	}

	public long getId() {
		return id;
	}

	public void setId( long id ) {
		this.id = id;
	}

	public Set<ImageTypeDto> getAllowedOutputTypes() {
		return allowedOutputTypes;
	}

	public void setAllowedOutputTypes( Set<ImageTypeDto> allowedOutputTypes ) {
		this.allowedOutputTypes = allowedOutputTypes;
	}

	/**
	 * @return dimensions of the resolution
	 */
	public DimensionsDto getDimensions() {
		return new DimensionsDto( width, height );
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( !( o instanceof ImageResolutionDto ) ) {
			return false;
		}

		ImageResolutionDto that = (ImageResolutionDto) o;

		if ( height != that.height ) {
			return false;
		}
		if ( width != that.width ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = width;
		result = 31 * result + height;
		return result;
	}
}
