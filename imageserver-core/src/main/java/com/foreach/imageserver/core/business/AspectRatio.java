package com.foreach.imageserver.core.business;

public class AspectRatio
{
	private final int p;
	private final int q;

	public static final AspectRatio ONE = new AspectRatio( 1, 1 );
	public static final AspectRatio UNDEFINED = new AspectRatio( 0, 0 );

	public AspectRatio( int p, int q ) {
		int gcd = gcd( q, p );

		// if q == 0, we normalize to 0/0 instead of 1/0

		this.p = ( q == 0 ) ? 0 : ( ( q > 0 ) ? p : -p ) / gcd;
		this.q = ( q == 0 ) ? 0 : ( ( q > 0 ) ? q : -q ) / gcd;
	}

	public final int getNumerator() {
		return p;
	}

	public final int getDenominator() {
		return q;
	}

	/**
	 * Checks if this is a valid aspect ratio (1/1 or more.
	 *
	 * @return True if any of the sides is in fact 0.
	 */
	public final boolean isUndefined() {
		return p == 0 || q == 0;
	}

	public int calculateWidthForHeight( int height ) {
		return Math.round( ( ( (float) p ) * height ) / q );
	}

	public int calculateHeightForWidth( int width ) {
		return Math.round( ( ( (float) q ) * width ) / p );
	}

	public boolean isLargerOnWidth() {
		return p > q;
	}

	public boolean isLargerOnHeight() {
		return p < q;
	}

	public final boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		AspectRatio other = (AspectRatio) o;

		return ( p == other.p ) && ( q == other.q );
	}

	private static boolean validInteger( long l ) {
		return ( ( l <= Integer.MAX_VALUE ) && ( l >= Integer.MIN_VALUE ) );
	}

	private static AspectRatio boundsCheck( long p, long q ) {
		if ( !validInteger( p ) || !validInteger( q ) ) {
			throw new ArithmeticException(
					"The result of the operation can not be accurately represented as a fraction" );
		}
		return new AspectRatio( (int) p, (int) q );
	}

	public final AspectRatio multiplyWith( AspectRatio multiplicator ) {
		long numerator = (long) p * multiplicator.p;
		long denominator = (long) q * multiplicator.q;
		return boundsCheck( numerator, denominator );
	}

	public final AspectRatio divideBy( AspectRatio dividor ) {
		long numerator = (long) p * dividor.q;
		long denominator = (long) q * dividor.p;
		return boundsCheck( numerator, denominator );
	}

	public final AspectRatio addInteger( int addend ) {
		long numerator = (long) q * addend + p;
		long denominator = (long) q;
		return boundsCheck( numerator, denominator );
	}

	public final boolean isNegative() {
		return ( p < 0 );
	}

	@Override
	public final String toString() {
		if ( this.equals( AspectRatio.UNDEFINED ) ) {
			return "undefined";
		}

		return p + "/" + q;
	}

	private int gcd( int a, int b ) {
		int na = ( a < 0 ) ? -a : a;
		int nb = ( b < 0 ) ? -b : b;

		// If one number is zero, we return the other

		if ( na == 0 ) {
			return nb;
		}
		if ( nb == 0 ) {
			return na;
		}

		while ( nb != 0 ) {
			int temp = nb;
			nb = na % nb;
			na = temp;
		}
		return na;
	}

	@Override
	public final int hashCode() {
		return 10007 * p + q;
	}
}
