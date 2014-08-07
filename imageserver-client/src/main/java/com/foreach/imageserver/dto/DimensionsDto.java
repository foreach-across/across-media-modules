package com.foreach.imageserver.dto;

import java.util.Objects;

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
}
