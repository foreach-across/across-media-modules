package com.foreach.imageserver.business;

public class Crop
{
	private int x, y, width, height;

	public Crop() {
	}

	public Crop( int x, int y, int width, int height ) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
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

	public boolean isEmpty() {
		return height <= 0 || width <= 0;
	}

	public Dimensions getDimensions() {
		return new Dimensions( width, height );
	}

	/**
	 * Snaps a crop to match with the dimensions specified (points exceeding dimensions are snapped to boundaries).
	 */
	public Crop normalize( Dimensions dimensions ) {
		int leftX = this.x;
		int leftY = this.y;
		int rightX = leftX + this.width;
		int rightY = leftY + this.height;

		leftX = snap( leftX, dimensions.getWidth() );
		leftY = snap( leftY, dimensions.getHeight() );
		rightX = snap( rightX, dimensions.getWidth() );
		rightY = snap( rightY, dimensions.getHeight() );

		return new Crop( leftX, leftY, rightX - leftX, rightY - leftY );
	}

	private int snap( int pos, int max ) {
		if ( pos < 0 ) {
			return 0;
		}
		else if ( pos > max ) {
			return max;
		}
		return pos;
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
		int result = x;
		result = 31 * result + y;
		result = 31 * result + width;
		result = 31 * result + height;
		return result;
	}

	@Override
	public String toString() {
		return "Crop{" +
				"x=" + x +
				", y=" + y +
				", width=" + width +
				", height=" + height +
				'}';
	}
}
