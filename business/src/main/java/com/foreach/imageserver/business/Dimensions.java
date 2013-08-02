package com.foreach.imageserver.business;

public class Dimensions
{
	public static final Dimensions EMPTY = new Dimensions();

	private int width;
	private int height;

	public Dimensions( int width, int height ) {
		this.width = width;
		this.height = height;
	}

	public Dimensions() {
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

	/**
	 * Will calculate the unknown dimensions according to the boundaries specified.
	 * Any unknown dimensions will be scaled according to the aspect ratio of the boundaries.
	 */
	public Dimensions normalize( Dimensions boundaries ) {
		Dimensions normalized = new Dimensions( width, height );

		Fraction originalAspectRatio = boundaries.getAspectRatio();

		if ( width == 0 && height == 0 ) {
			normalized.setWidth( boundaries.getWidth() );
			normalized.setHeight( boundaries.getHeight() );
		}
		else if ( height == 0 ) {
			normalized.setHeight( originalAspectRatio.calculateHeightForWidth( width ) );
		}
		else if ( width == 0 ) {
			normalized.setWidth( originalAspectRatio.calculateWidthForHeight( height ) );
		}

		return normalized;
	}

	/**
	 * Will downscale the dimensions to fit in the boundaries if they are larger.
	 */
	public Dimensions scaleToFitIn( Dimensions boundaries ) {
		Dimensions normalized = normalize( boundaries );
		Dimensions scaled = new Dimensions( normalized.getWidth(), normalized.getHeight() );

		Fraction aspectRatio = normalized.getAspectRatio();

		boolean shouldNormalize =
				normalized.getWidth() > boundaries.getWidth() || normalized.getHeight() > boundaries.getHeight();
		boolean extendsOnBoth =
				normalized.getWidth() > boundaries.getWidth() && normalized.getHeight() > boundaries.getHeight();
		boolean scaleOnWidth =
				( extendsOnBoth && normalized.getWidth() > normalized.getHeight() ) || ( !extendsOnBoth && normalized.getWidth() > boundaries.getWidth() );

		if ( shouldNormalize && scaleOnWidth ) {
			scaled.setWidth( boundaries.getWidth() );
			scaled.setHeight( aspectRatio.calculateHeightForWidth( boundaries.getWidth() ) );
		}
		else if ( shouldNormalize ) {
			scaled.setHeight( boundaries.getHeight() );
			scaled.setWidth( aspectRatio.calculateWidthForHeight( boundaries.getHeight() ) );
		}

		return scaled;
	}

	public Fraction getAspectRatio() {
		return new Fraction( width, height );
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		Dimensions that = (Dimensions) o;

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

	@Override
	public String toString() {
		return width + "x" + height;
	}
}
