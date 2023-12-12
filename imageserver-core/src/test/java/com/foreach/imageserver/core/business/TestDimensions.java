package com.foreach.imageserver.core.business;

import com.foreach.imageserver.math.AspectRatio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestDimensions
{
	@Test
	public void normalizeOnDimensionsWillDetermineUnknownDimension() {
		Dimensions boundaries = new Dimensions( 1600, 1200 );

		assertEquals( new Dimensions( 1600, 1200 ), new Dimensions().normalize( boundaries ) );
		assertEquals( new Dimensions( 800, 600 ), new Dimensions( 800, 0 ).normalize( boundaries ) );
		assertEquals( new Dimensions( 800, 600 ), new Dimensions( 0, 600 ).normalize( boundaries ) );
		assertEquals( new Dimensions( 123, 456 ), new Dimensions( 123, 456 ).normalize( boundaries ) );
	}

	@Test
	public void normalizeOnAspectRatioWillModifyBasedOnLargestDimension() {
		assertEquals( new Dimensions( 800, 600 ), new Dimensions( 800, 200 ).normalize( new AspectRatio( 4, 3 ) ) );
		assertEquals( new Dimensions( 600, 800 ), new Dimensions( 200, 800 ).normalize( new AspectRatio( 3, 4 ) ) );
	}

	@Test
	public void fitsIn() {
		assertTrue( new Dimensions( 1024, 768 ).fitsIn( new Dimensions( 1600, 1200 ) ) );
		assertTrue( new Dimensions( 1024, 768 ).fitsIn( new Dimensions( 1024, 768 ) ) );
		assertFalse( new Dimensions( 1600, 1200 ).fitsIn( new Dimensions( 1024, 768 ) ) );
		assertFalse( new Dimensions( 1024, 1201 ).fitsIn( new Dimensions( 1600, 1200 ) ) );
		assertFalse( new Dimensions( 1601, 768 ).fitsIn( new Dimensions( 1600, 1200 ) ) );
	}

	@Test
	public void scaleToFitIntoWillDownScaleDimensions() {
		Dimensions boundaries = new Dimensions( 1600, 1200 );

		assertEquals( new Dimensions( 1600, 1200 ), new Dimensions().scaleToFitIn( boundaries ) );
		assertEquals( new Dimensions( 800, 600 ), new Dimensions( 800, 0 ).scaleToFitIn( boundaries ) );
		assertEquals( new Dimensions( 800, 600 ), new Dimensions( 0, 600 ).scaleToFitIn( boundaries ) );
		assertEquals( new Dimensions( 800, 600 ), new Dimensions( 800, 600 ).scaleToFitIn( boundaries ) );
		assertEquals( new Dimensions( 1600, 1200 ), new Dimensions( 1600, 1200 ).scaleToFitIn( boundaries ) );
		assertEquals( new Dimensions( 1600, 1200 ), new Dimensions( 1600, 0 ).scaleToFitIn( boundaries ) );
		assertEquals( new Dimensions( 1600, 1200 ), new Dimensions( 0, 1200 ).scaleToFitIn( boundaries ) );
		assertEquals( new Dimensions( 1600, 1200 ), new Dimensions( 3200, 2400 ).scaleToFitIn( boundaries ) );
		assertEquals( new Dimensions( 1600, 1200 ), new Dimensions( 3200, 2400 ).scaleToFitIn( boundaries ) );
		assertEquals( new Dimensions( 1600, 1200 ), new Dimensions( 3200, 0 ).scaleToFitIn( boundaries ) );
		assertEquals( new Dimensions( 1600, 1200 ), new Dimensions( 0, 2400 ).scaleToFitIn( boundaries ) );
		assertEquals( new Dimensions( 1600, 500 ), new Dimensions( 3200, 1000 ).scaleToFitIn( boundaries ) );
		assertEquals( new Dimensions( 700, 1200 ), new Dimensions( 1400, 2400 ).scaleToFitIn( boundaries ) );

		assertEquals( new Dimensions( 786, 768 ),
		              new Dimensions( 1024, 1000 ).scaleToFitIn( new Dimensions( 1024, 768 ) ) );
		assertEquals( new Dimensions( 10, 100 ),
		              new Dimensions( 1000, 10000 ).scaleToFitIn( new Dimensions( 100, 100 ) ) );

		assertEquals( new Dimensions( 819, 768 ),
		              new Dimensions( 1600, 1500 ).scaleToFitIn( new Dimensions( 1024, 768 ) ) );
	}

	@Test
	public void equality() {
		Dimensions da1 = new Dimensions( 400, 300 );
		Dimensions da2 = new Dimensions( 800, 600 );

		comparison( da1, da2, false );
		assertEquals( true, da1.fetchAspectRatio().equals( da2.fetchAspectRatio() ) );
	}

	private void comparison( Dimensions left, Dimensions right, boolean expected ) {
		assertEquals( expected, right.equals( left ) );
		assertEquals( expected, left.equals( right ) );

		if ( expected ) {
			assertEquals( left.hashCode(), right.hashCode(), "hashCode() is broken, it should return identical result for objects that are equal" );
		}
	}
}
