package com.foreach.imageserver.core.business;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestFraction
{
	private Fraction two;

	@Before
	public void setup() {
		two = new Fraction( 2, 1 );
	}

	@Test
	public void calculateWidthForHeight() {
		Fraction fraction = new Fraction( 4, 3 );
		assertEquals( 800, fraction.calculateWidthForHeight( 600 ) );
		assertEquals( 1024, fraction.calculateWidthForHeight( 768 ) );
		assertEquals( 1600, fraction.calculateWidthForHeight( 1200 ) );
		assertEquals( 4, fraction.calculateWidthForHeight( 3 ) );

		fraction = new Fraction( 16, 9 );
		assertEquals( 16, fraction.calculateWidthForHeight( 9 ) );

		fraction = new Fraction( 9, 16 );
		assertEquals( 9, fraction.calculateWidthForHeight( 16 ) );

		fraction = new Fraction( 1280, 1024 );
		assertEquals( 1280, fraction.calculateWidthForHeight( 1024 ) );

		fraction = new Fraction( 700, 467 );
		assertEquals( 628, fraction.calculateWidthForHeight( 419 ) );
	}

	@Test
	public void calculateHeightForWidth() {
		Fraction fraction = new Fraction( 4, 3 );
		assertEquals( 600, fraction.calculateHeightForWidth( 800 ) );
		assertEquals( 768, fraction.calculateHeightForWidth( 1024 ) );
		assertEquals( 1200, fraction.calculateHeightForWidth( 1600 ) );
		assertEquals( 3, fraction.calculateHeightForWidth( 4 ) );

		fraction = new Fraction( 16, 9 );
		assertEquals( 9, fraction.calculateHeightForWidth( 16 ) );

		fraction = new Fraction( 9, 16 );
		assertEquals( 16, fraction.calculateHeightForWidth( 9 ) );

		fraction = new Fraction( 1280, 1024 );
		assertEquals( 1024, fraction.calculateHeightForWidth( 1280 ) );

		fraction = new Fraction( 700, 467 );
		assertEquals( 419, fraction.calculateHeightForWidth( 628 ) );
	}

	@Test
	public void isLargerOnSide() {
		Fraction fraction = new Fraction( 4, 3 );
		assertTrue( fraction.isLargerOnWidth() );
		assertFalse( fraction.isLargerOnHeight() );

		fraction = new Fraction( 9, 16 );
		assertTrue( fraction.isLargerOnHeight() );
		assertFalse( fraction.isLargerOnWidth() );

		fraction = new Fraction( 4, 4 );
		assertFalse( fraction.isLargerOnWidth() );
		assertFalse( fraction.isLargerOnHeight() );
	}

	@Test
	public void normalize() {
		Fraction oneHalve = new Fraction( 1, 2 );
		Fraction same = new Fraction( -1, -2 );

		assertEquals( true, ( oneHalve.equals( same ) ) );
		assertEquals( true, ( oneHalve.equals( oneHalve ) ) );
	}

	@Test
	public void normalize2() {
		Fraction a = new Fraction( 11, 17 );
		Fraction b = new Fraction( -11000, -17000 );

		assertEquals( true, ( a.equals( b ) ) );
	}

	@Test
	public void multiply() {
		Fraction tv = new Fraction( 4, 3 );
		Fraction inverseTv = new Fraction( 3, 4 );
		Fraction widescreen = new Fraction( 16, 9 );

		testMultiplication( Fraction.ONE, Fraction.ONE, Fraction.ONE );
		testMultiplication( Fraction.ONE, tv, tv );
		testMultiplication( tv, Fraction.ONE, tv );
		testMultiplication( tv, tv, widescreen );
		testMultiplication( tv, inverseTv, Fraction.ONE );
		testMultiplication( inverseTv, tv, Fraction.ONE );
	}

	private void testMultiplication( Fraction a, Fraction b, Fraction ab ) {
		assertEquals( ab, a.multiplyWith( b ) );
	}

	@Test
	public void divide() {
		Fraction tv = new Fraction( 4, 3 );
		Fraction inverseTv = new Fraction( 3, 4 );

		assertEquals( true, inverseTv.equals( Fraction.ONE.divideBy( tv ) ) );
		assertEquals( true, tv.equals( Fraction.ONE.divideBy( inverseTv ) ) );
		assertEquals( true, Fraction.ONE.equals( tv.divideBy( tv ) ) );
		assertEquals( true, Fraction.ONE.equals( inverseTv.divideBy( inverseTv ) ) );
	}

	@Test(expected = ArithmeticException.class)
	public void underflow() {
		// the smallest nonzero postive number we can express
		Fraction smallestPositiveFraction = new Fraction( 1, Integer.MAX_VALUE );

		Fraction evenSmaller = smallestPositiveFraction.divideBy( two );
	}

	@Test(expected = ArithmeticException.class)
	public void underflow2() {
		// the biggest nonzero negative number we can express
		Fraction biggestNegativeFraction = new Fraction( -1, Integer.MAX_VALUE );

		Fraction evenBigger = biggestNegativeFraction.divideBy( two );
	}

	@Test(expected = ArithmeticException.class)
	public void overflow() {
		// the biggest nonzero postive number we can express
		Fraction biggestPositiveFraction = new Fraction( Integer.MAX_VALUE, 1 );

		Fraction evenBigger = biggestPositiveFraction.multiplyWith( two );
	}

	@Test(expected = ArithmeticException.class)
	public void overflow2() {
		// the smallest nonzero negative number we can express
		Fraction smallestNegativeFraction = new Fraction( Integer.MIN_VALUE, 1 );

		Fraction evenBigger = smallestNegativeFraction.multiplyWith( two );
	}

	@Test
	public void equalityAndHash() {
		int scale = 2 * 3 * 7 * 11;
		Fraction tv = new Fraction( 4, 3 );
		Fraction bigTv = new Fraction( scale * 4, scale * 3 );

		assertEquals( true, tv.equals( bigTv ) );
		assertEquals( true, bigTv.equals( tv ) );

		assertEquals( false, tv.equals( Fraction.ONE ) );

		assertEquals( tv.hashCode(), bigTv.hashCode() );
	}

	@Test
	public void gcd() {
		Fraction f = new Fraction( 120, -40 );

		assertEquals( -3, f.getNumerator() );
		assertEquals( 1, f.getDenominator() );

		f = new Fraction( 11, 77 );

		assertEquals( 1, f.getNumerator() );
		assertEquals( 7, f.getDenominator() );

		f = new Fraction( -111, -111 );

		assertEquals( 1, f.getNumerator() );
		assertEquals( 1, f.getDenominator() );

		Fraction zero = new Fraction( 0, 1 );

		assertEquals( 0, zero.getNumerator() );
		assertEquals( 1, zero.getDenominator() );
	}
}
