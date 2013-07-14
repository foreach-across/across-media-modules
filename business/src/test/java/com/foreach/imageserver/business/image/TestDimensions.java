package com.foreach.imageserver.business.image;

import com.foreach.imageserver.business.Dimensions;
import com.foreach.imageserver.business.Fraction;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestDimensions
{
	@Test
	public void absolute() {
		Dimensions da = new Dimensions( 400, 300 );

		assertEquals( true, da.isAbsolute() );

		Dimensions dr = new Dimensions( new Fraction( 400, 300 ) );

		assertEquals( false, dr.isAbsolute() );
	}

	@Test
	public void equality() {
		Dimensions da1 = new Dimensions( 400, 300 );
		Dimensions da2 = new Dimensions( 800, 600 );

		comparison( da1, da2, false );
		assertEquals( true, da1.getAspectRatio().equals( da2.getAspectRatio() ) );

		Dimensions dr1 = new Dimensions( new Fraction( 400, 300 ) );
		Dimensions dr2 = new Dimensions( new Fraction( 800, 600 ) );

		comparison( dr1, dr2, true );
		assertEquals( true, dr1.getAspectRatio().equals( dr2.getAspectRatio() ) );
	}

	private void comparison( Dimensions left, Dimensions right, boolean expected ) {
		assertEquals( expected, right.equals( left ) );
		assertEquals( expected, left.equals( right ) );

		if ( expected ) {
			assertEquals( "hashCode() is broken, it should return identical result for objects that are equal",
			              left.hashCode(), right.hashCode() );
		}
	}

	@Test
	public void hasAspectRatio() {
		Dimensions da0 = new Dimensions( 400, 300 );
		Dimensions da1 = new Dimensions( 400, 0 );
		Dimensions da2 = new Dimensions( 0, 600 );
		Dimensions da3 = new Dimensions( 0, 0 );

		assertEquals( true, da0.hasAspectRatio() );
		assertEquals( false, da1.hasAspectRatio() );
		assertEquals( false, da2.hasAspectRatio() );
		assertEquals( false, da3.hasAspectRatio() );
	}
}
