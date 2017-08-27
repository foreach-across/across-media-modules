package com.foreach.imageserver.core.business;

import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class Crop
{
	private int x, y, width, height;

	public Crop( int x, int y, int width, int height ) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public Crop() {
	}

	public int getX() {
		return x;
	}

	public void setX( int x ) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY( int y ) {
		this.y = y;
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

	public boolean hasEmptyHeightOrWidth() {
		return height <= 0 || width <= 0;
	}

	public Dimensions getDimensions() {
		return new Dimensions( width, height );
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		Crop crop = (Crop) o;

		if ( height != crop.height ) {
			return false;
		}
		if ( width != crop.width ) {
			return false;
		}
		if ( x != crop.x ) {
			return false;
		}
		if ( y != crop.y ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash( x, y, width, height );
	}
}
