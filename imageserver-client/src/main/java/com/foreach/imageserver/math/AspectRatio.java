package com.foreach.imageserver.math;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AspectRatio
{

	private static final Pattern RATIO_PATTERN = Pattern.compile( "(\\d+)/(\\d+)" );

	private final Ratio ratio;

	public static final AspectRatio ONE = new AspectRatio( 1, 1 );
	public static final AspectRatio UNDEFINED = new AspectRatio( 0, 0 );

	public AspectRatio( String ratio ) {
		Matcher matcher = RATIO_PATTERN.matcher( ratio );
		if ( !matcher.matches() ) {
			throw new IllegalArgumentException( "Ratio should be of pattern: \"p/q\"" );
		}
		int p = NumberUtils.toInt( matcher.group( 1 ) );
		int q = NumberUtils.toInt( matcher.group( 2 ) );
		this.ratio = new Ratio( p, q );
	}

	public AspectRatio( int p, int q ) {
		this.ratio = new Ratio( p, q );
	}

	public final int getNumerator() {
		return ratio.getP();
	}

	public final int getDenominator() {
		return ratio.getQ();
	}

	/**
	 * Checks if this is a valid aspect ratio (1/1 or more.
	 *
	 * @return True if any of the sides is in fact 0.
	 */
	public final boolean isUndefined() {
		return getNumerator() == 0 || getDenominator() == 0;
	}

	public int calculateWidthForHeight( int height ) {
		return Math.round( ( ( (float) getNumerator() ) * height ) / getDenominator() );
	}

	public int calculateHeightForWidth( int width ) {
		return Math.round( ( ( (float) getDenominator() ) * width ) / getNumerator() );
	}

	public boolean isLargerOnWidth() {
		return getNumerator() > getDenominator();
	}

	public boolean isLargerOnHeight() {
		return !isLargerOnWidth();
	}

	public final boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		AspectRatio other = (AspectRatio) o;

		return getDenominator() == other.getDenominator() && getNumerator() == other.getNumerator();
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
		long numerator = (long) getNumerator() * multiplicator.getNumerator();
		long denominator = (long) getDenominator() * multiplicator.getDenominator();
		return boundsCheck( numerator, denominator );
	}

	public final AspectRatio divideBy( AspectRatio dividor ) {
		long numerator = (long) getNumerator() * dividor.getDenominator();
		long denominator = (long) getDenominator() * dividor.getNumerator();
		return boundsCheck( numerator, denominator );
	}

	public final AspectRatio addInteger( int addend ) {
		long numerator = (long) getDenominator() * addend + getNumerator();
		long denominator = (long) getDenominator();
		return boundsCheck( numerator, denominator );
	}

	public final boolean isNegative() {
		return ( getNumerator() < 0 );
	}

	@Override
	public final String toString() {
		if ( this.equals( AspectRatio.UNDEFINED ) ) {
			return "undefined";
		}

		return getNumerator() + "/" + getDenominator();
	}

	@Override
	public final int hashCode() {
		return ratio.hashCode();
	}

	private final class Ratio
	{
		private final int p;
		private final int q;

		public Ratio( int p, int q ) {
			int gcd = gcd( q, p );

			// if q == 0, we normalize to 0/0 instead of 1/0

			this.p = ( q == 0 ) ? 0 : ( ( q > 0 ) ? p : -p ) / gcd;
			this.q = ( q == 0 ) ? 0 : ( ( q > 0 ) ? q : -q ) / gcd;
		}

		public int getP() {
			return p;
		}

		public int getQ() {
			return q;
		}

		public final int hashCode() {
			return 10007 * p + q;
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
	}
}
