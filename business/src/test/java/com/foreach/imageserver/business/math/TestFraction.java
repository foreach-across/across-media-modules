package com.foreach.imageserver.business.math;


import static org.junit.Assert.assertEquals;

import com.foreach.imageserver.business.Fraction;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class TestFraction {

    private Fraction two;

    @Before
    public void setup()
    {
        two = new Fraction ( 2, 1 );
    }

    @Test
    public void normalize()
    {
        Fraction oneHalve = new Fraction( 1, 2 );
        Fraction same = new Fraction( -1, -2 );

        assertEquals( true, ( oneHalve.compareTo( same ) ) == 0 );
        assertEquals( true, ( oneHalve.equals( same ) ) );
        assertEquals( true, ( oneHalve.equals( oneHalve ) ) );
    }

    @Test
    public void normalize2()
    {
        Fraction a = new Fraction( 11, 17 );
        Fraction b = new Fraction( -11000, -17000 );

        assertEquals( true, ( a.compareTo( b ) ) == 0 );
        assertEquals( true, ( a.equals( b ) ) );
    }


    @Test
    public void compare()
    {
        Fraction oneHalve = new Fraction( 1, 2 );
        Fraction oneThird = new Fraction( 1, 3 );

        assertEquals( true, ( oneHalve.compareTo( oneThird ) ) > 0 );
        assertEquals( true, ( oneThird.compareTo( oneHalve ) ) < 0 );
        assertEquals( true, ( oneThird.compareTo( oneThird ) ) == 0 );

        Fraction minusOneHalve = new Fraction( -1, 2 );
        Fraction minusOneThird = new Fraction( -1, 3 );

        assertEquals( true, ( minusOneHalve.compareTo( minusOneThird ) ) < 0 );
        assertEquals( true, ( minusOneThird.compareTo( minusOneHalve ) ) > 0 );
    }

    @Test(expected=ClassCastException.class)
    public void cantCompare()
    {
        Fraction oneHalve = new Fraction( 1, 2 );
        oneHalve.compareTo( new Float( 1.33f ) );
    }

    @Test
    public void compareWithUndefineds()
    {
        Fraction oneHalve = new Fraction( 1, 2 );

        assertEquals( true, ( oneHalve.compareTo( Fraction.UNDEFINED ) ) > 0 );
        assertEquals( true, ( Fraction.UNDEFINED.compareTo( oneHalve ) ) < 0 );
        assertEquals( true, ( Fraction.UNDEFINED.compareTo( Fraction.UNDEFINED ) ) == 0 );

        Fraction undef = new Fraction( 0, 0 );
        assertEquals( true, ( oneHalve.compareTo( undef ) ) > 0 );
        assertEquals( true, ( undef.compareTo( oneHalve ) ) < 0 );
        assertEquals( true, ( undef.compareTo( undef ) ) == 0 );
        assertEquals( true, ( undef.compareTo( Fraction.UNDEFINED ) ) == 0 );
        assertEquals( true, ( Fraction.UNDEFINED.compareTo( undef ) ) == 0 );
    }

    @Test
    public void multiply()
    {
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

    private void testMultiplication( Fraction a, Fraction b, Fraction ab )
    {
        assertEquals( ab, a.multiplyWith( b ) );
    }

    @Test
    public void divide()
    {
        Fraction tv = new Fraction( 4, 3 );
        Fraction inverseTv = new Fraction( 3, 4 );

        assertEquals( true, inverseTv.equals( Fraction.ONE.divideBy( tv ) )  );
        assertEquals( true, tv.equals( Fraction.ONE.divideBy( inverseTv ) )  );
        assertEquals( true, Fraction.ONE.equals( tv.divideBy( tv ) )  );
        assertEquals( true, Fraction.ONE.equals( inverseTv.divideBy( inverseTv ) )  );
    }

    @Test(expected=ArithmeticException.class)
    public void underflow()
    {
        // the smallest nonzero postive number we can express
        Fraction smallestPositiveFraction = new Fraction( 1, Integer.MAX_VALUE );

        Fraction evenSmaller = smallestPositiveFraction.divideBy( two );
    }

    @Test(expected=ArithmeticException.class)
    public void underflow2()
    {
        // the biggest nonzero negative number we can express
        Fraction biggestNegativeFraction = new Fraction( -1, Integer.MAX_VALUE );

        Fraction evenBigger = biggestNegativeFraction.divideBy(two);
    }

    @Test(expected=ArithmeticException.class)
    public void overflow()
    {
        // the biggest nonzero postive number we can express
        Fraction biggestPositiveFraction = new Fraction( Integer.MAX_VALUE, 1 );

        Fraction evenBigger = biggestPositiveFraction.multiplyWith(two);
    }

    @Test(expected=ArithmeticException.class)
    public void overflow2()
    {
        // the smallest nonzero negative number we can express
        Fraction smallestNegativeFraction = new Fraction( Integer.MIN_VALUE, 1 );

        Fraction evenBigger = smallestNegativeFraction.multiplyWith( two );
    }

    @Test
    public void scale()
    {
        Fraction tv = new Fraction( 4, 3 );

        assertEquals( 4, tv.scale( 3 ) );
        assertEquals( 3, tv.deScale( 4 ) );

        Fraction inverseTv = new Fraction( 3, 4 );

        assertEquals( 3, inverseTv.scale(4) );
        assertEquals( 4, inverseTv.deScale(3) );
    }

    @Test
    // scale must work also when intermediate results must be represented as a long outside int boundaries
    public void scalePrecision()
    {
        Fraction qMax = new Fraction( Integer.MAX_VALUE, 4 );
        assertEquals( Integer.MAX_VALUE, qMax.scale( 4 ) );
    }

    @Test
    public void scalePrecision2()
    {
        Fraction qMax = new Fraction( Integer.MIN_VALUE, 4 );
        assertEquals( Integer.MIN_VALUE, qMax.scale( 4 ) );
    }

    @Test
    public void deScalePrecision()
    {
        Fraction qMax = new Fraction( Integer.MAX_VALUE - 1, Integer.MAX_VALUE );
        assertEquals( Integer.MAX_VALUE, qMax.deScale( Integer.MAX_VALUE - 1 ) );
    }

    @Test
    public void deScalePrecision2()
    {
        Fraction qMax = new Fraction( Integer.MIN_VALUE + 1, Integer.MAX_VALUE );
        assertEquals( Integer.MAX_VALUE, qMax.deScale( Integer.MIN_VALUE + 1 ) );
    }


    @Test
    public void equalityAndHash()
    {
        int scale = 2*3*7*11;
        Fraction tv = new Fraction( 4, 3 );
        Fraction bigTv = new Fraction( scale*4, scale*3 );

        assertEquals( true, tv.equals( bigTv ) );
        assertEquals( true, bigTv.equals( tv ) );

        assertEquals( false, tv.equals( Fraction.ONE ) );

        assertEquals( tv.hashCode(), bigTv.hashCode()  );
    }

    @Test( expected=ArithmeticException.class )
    @Ignore // Deliberately broken for Ibatis model
    public void divideByZero()
    {
        Fraction f = new Fraction( 1, 0 );
    }

    @Test
    public void gcd()
    {
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

    @Test
    public void toAndFromString()
    {
        for( int i = -10; i<11; i++) {
            for( int j = 1; j<11; j++) {
                Fraction f = new Fraction( i, j );
                assertEquals( f, Fraction.parseString( f.toString() ) );
                assertEquals( f, Fraction.parseString( f.getStringForUrl() ) );
            }
        }
    }

    @Test(expected=NumberFormatException.class)
    public void noOverlapWithIntegers()
    {
        Fraction.parseString( "10" );
    }

    @Test
    public void lossyRepresentation()
    {
        Fraction f = new Fraction( 4, 3 );

        assertEquals( true, f.getLossyRepresentation().contains("1.333") );
    }

	@Test
	public void tolerance()
	{
		Fraction f = new Fraction( 8, 10);
		Fraction g = new Fraction( 10, 10);

		// 0.8 is within a 20% tolerance interval of 1.0
		assertEquals( true, f.withinTolerance( g, new Fraction( 20, 100 ) ) );
		assertEquals( false, f.withinTolerance( g, new Fraction( 19, 100 ) ) );

		// 1.0 is within a 25% tolerance interval of 0.8
		assertEquals( true, g.withinTolerance( f, new Fraction( 25, 100 ) ) );
		assertEquals( false, g.withinTolerance( f, new Fraction( 24, 100 ) ) );

		f = new Fraction( -8, 10);
		g = new Fraction( -10, 10);

		// -0.8 is within a 20% tolerance interval of -1.0
		assertEquals( true, f.withinTolerance( g, new Fraction( 20, 100 ) ) );
		assertEquals( false, f.withinTolerance( g, new Fraction( 19, 100 ) ) );

		// -1.0 is within a 25% tolerance interval of -0.8
		assertEquals( true, g.withinTolerance( f, new Fraction( 25, 100 ) ) );
		assertEquals( false, g.withinTolerance( f, new Fraction( 24, 100 ) ) );

		f = new Fraction( -1, 10);
		g = new Fraction( 2, 10);

		// -0.1 is within a 150% tolerance interval of 0.2
		assertEquals( true, f.withinTolerance( g, new Fraction( 150, 100 ) ) );
		assertEquals( false, f.withinTolerance( g, new Fraction( 149, 100 ) ) );

		// 0.2 is within a 300% tolerance interval of -0.1
		assertEquals( true, g.withinTolerance( f, new Fraction( 300, 100 ) ) );
		assertEquals( false, g.withinTolerance( f, new Fraction( 299, 100 ) ) );
	}
}
