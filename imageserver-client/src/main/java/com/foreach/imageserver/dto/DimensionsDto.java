package com.foreach.imageserver.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Builder(toBuilder = true)
public class DimensionsDto
{
	private int width;
	private int height;

	public DimensionsDto() {
	}

	public DimensionsDto( DimensionsDto original ) {
		width = original.width;
		height = original.height;
	}

	public DimensionsDto( int width, int height ) {
		this.width = width;
		this.height = height;
	}

	public boolean isEmpty() {
		return width == 0 && height == 0;
	}

	public boolean hasUnspecifiedDimension() {
		return width == 0 || height == 0;
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( !( o instanceof DimensionsDto ) ) {
			return false;
		}

		DimensionsDto that = (DimensionsDto) o;

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
		return Objects.hash( width, height );
	}

	@Override
	public String toString() {
		return "{" +
				"width=" + width +
				", height=" + height +
				'}';
	}
}
