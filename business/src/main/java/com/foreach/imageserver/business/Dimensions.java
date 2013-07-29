package com.foreach.imageserver.business;

public class Dimensions implements Comparable<Dimensions>
{
	public static final Dimensions EMPTY = new Dimensions();

	private int width;
	private int height;
	private Fraction ratio = Fraction.UNDEFINED;

	public Dimensions( int width, int height ) {
		this.width = width;
		this.height = height;
		this.ratio = Fraction.UNDEFINED;
	}

	public Dimensions( Fraction f ) {
		this.width = 0;
		this.height = 0;
		this.ratio = f;
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

	public Fraction getRatio() {
		return ratio;
	}

	// clean up after persistence engine...
	public void setRatio( Fraction ratio ) {
		this.ratio = ratio;
	}

	public boolean isAbsolute() {
		return ( ratio.equals( Fraction.UNDEFINED ) );
	}

	public boolean hasAspectRatio() {
		if ( isAbsolute() ) {
			return ( ( width != 0 ) && ( height != 0 ) );
		}
		else {
			return true;
		}
	}

	public Fraction getAspectRatio() {
		if ( isAbsolute() ) {
			return new Fraction( width, height );
		}
		else {
			return ratio;
		}
	}

	public int compareTo( Dimensions other ) {
		if ( isAbsolute() != other.isAbsolute() ) {
			return isAbsolute() ? -1 : 1;
		}
		if ( isAbsolute() ) {
			if ( height < other.height ) {
				return -1;
			}
			if ( height > other.height ) {
				return 1;
			}
			if ( width < other.width ) {
				return -1;
			}
			if ( width > other.width ) {
				return 1;
			}
			return 0;
		}
		else {
			if ( relativeEqual( other ) ) {
				return 0;
			}
			else {
				return ratio.compareTo( other.ratio );
			}
		}
	}

	private boolean absoluteEqual( Dimensions other ) {
		return ( ( width == other.width ) && ( height == other.height ) );
	}

	private boolean relativeEqual( Dimensions other ) {
		return ( ratio.equals( other.ratio ) );
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( !( o instanceof Dimensions ) ) {
			return false;
		}

		Dimensions other = (Dimensions) o;

		if ( isAbsolute() != other.isAbsolute() ) {
			return false;
		}

		if ( isAbsolute() ) {
			return absoluteEqual( other );
		}
		else {

			return relativeEqual( other );
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		if ( isAbsolute() ) {
			if ( width != 0 ) {
				sb.append( width );
			}
			sb.append( 'x' );
			if ( height != 0 ) {
				sb.append( height );
			}
		}
		else {
			sb.append( ratio );
		}

		return sb.toString();
	}

	@Override
	public int hashCode() {
		if ( isAbsolute() ) {
			return 10007 * width + height;
		}
		else {
			return ratio.hashCode();
		}
	}
}
