package com.foreach.imageserver.math;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestAspectRatio
{
	private AspectRatio two;

	@Before
	public void setup() {
		two = new AspectRatio( 2, 1 );
	}

	@Test
	public void calculateWidthForHeight() {
		AspectRatio aspectRatio = new AspectRatio( 4, 3 );
		assertEquals( 800, aspectRatio.calculateWidthForHeight( 600 ) );
		assertEquals( 1024, aspectRatio.calculateWidthForHeight( 768 ) );
		assertEquals( 1600, aspectRatio.calculateWidthForHeight( 1200 ) );
		assertEquals( 4, aspectRatio.calculateWidthForHeight( 3 ) );

		aspectRatio = new AspectRatio( 16, 9 );
		assertEquals( 16, aspectRatio.calculateWidthForHeight( 9 ) );

		aspectRatio = new AspectRatio( 9, 16 );
		assertEquals( 9, aspectRatio.calculateWidthForHeight( 16 ) );

		aspectRatio = new AspectRatio( 1280, 1024 );
		assertEquals( 1280, aspectRatio.calculateWidthForHeight( 1024 ) );

		aspectRatio = new AspectRatio( 700, 467 );
		assertEquals( 628, aspectRatio.calculateWidthForHeight( 419 ) );
	}

	@Test
	public void calculateHeightForWidth() {
		AspectRatio aspectRatio = new AspectRatio( 4, 3 );
		assertEquals( 600, aspectRatio.calculateHeightForWidth( 800 ) );
		assertEquals( 768, aspectRatio.calculateHeightForWidth( 1024 ) );
		assertEquals( 1200, aspectRatio.calculateHeightForWidth( 1600 ) );
		assertEquals( 3, aspectRatio.calculateHeightForWidth( 4 ) );

		aspectRatio = new AspectRatio( 16, 9 );
		assertEquals( 9, aspectRatio.calculateHeightForWidth( 16 ) );

		aspectRatio = new AspectRatio( 9, 16 );
		assertEquals( 16, aspectRatio.calculateHeightForWidth( 9 ) );

		aspectRatio = new AspectRatio( 1280, 1024 );
		assertEquals( 1024, aspectRatio.calculateHeightForWidth( 1280 ) );

		aspectRatio = new AspectRatio( 700, 467 );
		assertEquals( 419, aspectRatio.calculateHeightForWidth( 628 ) );
	}

	@Test
	public void isLargerOnSide() {
		AspectRatio aspectRatio = new AspectRatio( 4, 3 );
		assertTrue( aspectRatio.isLargerOnWidth() );
		assertFalse( aspectRatio.isLargerOnHeight() );

		aspectRatio = new AspectRatio( 9, 16 );
		assertTrue( aspectRatio.isLargerOnHeight() );
		assertFalse( aspectRatio.isLargerOnWidth() );

		aspectRatio = new AspectRatio( 4, 4 );
		assertFalse( aspectRatio.isLargerOnWidth() );
		assertFalse( aspectRatio.isLargerOnHeight() );
	}

	@Test
	public void normalize() {
		AspectRatio oneHalve = new AspectRatio( 1, 2 );
		AspectRatio same = new AspectRatio( -1, -2 );

		assertEquals( true, ( oneHalve.equals( same ) ) );
		assertEquals( true, ( oneHalve.equals( oneHalve ) ) );
	}

	@Test
	public void normalize2() {
		AspectRatio a = new AspectRatio( 11, 17 );
		AspectRatio b = new AspectRatio( -11000, -17000 );

		assertEquals( true, ( a.equals( b ) ) );
	}

	@Test
	public void multiply() {
		AspectRatio tv = new AspectRatio( 4, 3 );
		AspectRatio inverseTv = new AspectRatio( 3, 4 );
		AspectRatio widescreen = new AspectRatio( 16, 9 );

		testMultiplication( AspectRatio.ONE, AspectRatio.ONE, AspectRatio.ONE );
		testMultiplication( AspectRatio.ONE, tv, tv );
		testMultiplication( tv, AspectRatio.ONE, tv );
		testMultiplication( tv, tv, widescreen );
		testMultiplication( tv, inverseTv, AspectRatio.ONE );
		testMultiplication( inverseTv, tv, AspectRatio.ONE );
	}

	private void testMultiplication( AspectRatio a, AspectRatio b, AspectRatio ab ) {
		assertEquals( ab, a.multiplyWith( b ) );
	}

	@Test
	public void divide() {
		AspectRatio tv = new AspectRatio( 4, 3 );
		AspectRatio inverseTv = new AspectRatio( 3, 4 );

		assertEquals( true, inverseTv.equals( AspectRatio.ONE.divideBy( tv ) ) );
		assertEquals( true, tv.equals( AspectRatio.ONE.divideBy( inverseTv ) ) );
		assertEquals( true, AspectRatio.ONE.equals( tv.divideBy( tv ) ) );
		assertEquals( true, AspectRatio.ONE.equals( inverseTv.divideBy( inverseTv ) ) );
	}

	@Test(expected = ArithmeticException.class)
	public void underflow() {
		// the smallest nonzero postive number we can express
		AspectRatio smallestPositiveAspectRatio = new AspectRatio( 1, Integer.MAX_VALUE );

		AspectRatio evenSmaller = smallestPositiveAspectRatio.divideBy( two );
	}

	@Test(expected = ArithmeticException.class)
	public void underflow2() {
		// the biggest nonzero negative number we can express
		AspectRatio biggestNegativeAspectRatio = new AspectRatio( -1, Integer.MAX_VALUE );

		AspectRatio evenBigger = biggestNegativeAspectRatio.divideBy( two );
	}

	@Test(expected = ArithmeticException.class)
	public void overflow() {
		// the biggest nonzero postive number we can express
		AspectRatio biggestPositiveAspectRatio = new AspectRatio( Integer.MAX_VALUE, 1 );

		AspectRatio evenBigger = biggestPositiveAspectRatio.multiplyWith( two );
	}

	@Test(expected = ArithmeticException.class)
	public void overflow2() {
		// the smallest nonzero negative number we can express
		AspectRatio smallestNegativeAspectRatio = new AspectRatio( Integer.MIN_VALUE, 1 );

		AspectRatio evenBigger = smallestNegativeAspectRatio.multiplyWith( two );
	}

	@Test
	public void equalityAndHash() {
		int scale = 2 * 3 * 7 * 11;
		AspectRatio tv = new AspectRatio( 4, 3 );
		AspectRatio bigTv = new AspectRatio( scale * 4, scale * 3 );

		assertEquals( true, tv.equals( bigTv ) );
		assertEquals( true, bigTv.equals( tv ) );

		assertEquals( false, tv.equals( AspectRatio.ONE ) );

		assertEquals( tv.hashCode(), bigTv.hashCode() );
	}

	@Test
	public void gcd() {
		AspectRatio f = new AspectRatio( 120, -40 );

		assertEquals( -3, f.getNumerator() );
		assertEquals( 1, f.getDenominator() );

		f = new AspectRatio( 11, 77 );

		assertEquals( 1, f.getNumerator() );
		assertEquals( 7, f.getDenominator() );

		f = new AspectRatio( -111, -111 );

		assertEquals( 1, f.getNumerator() );
		assertEquals( 1, f.getDenominator() );

		AspectRatio zero = new AspectRatio( 0, 1 );

		assertEquals( 0, zero.getNumerator() );
		assertEquals( 1, zero.getDenominator() );
	}
}
