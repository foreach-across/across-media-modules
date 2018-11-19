package com.foreach.imageserver.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class ImageResolutionDto
{
	private Long id;
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
