package com.foreach.imageserver.math;

import com.foreach.imageserver.dto.DimensionsDto;
import org.junit.Test;

import static com.foreach.imageserver.math.ImageServerConversionUtils.*;
import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
public class TestImageServerConversionUtils
{
	@Test
	public void normalizeOnDimensionsWillDetermineUnknownDimension() {
		DimensionsDto boundaries = new DimensionsDto( 1600, 1200 );

		assertEquals( new DimensionsDto( 1600, 1200 ), normalize( new DimensionsDto(), boundaries ) );
		assertEquals( new DimensionsDto( 800, 600 ), normalize( new DimensionsDto( 800, 0 ), boundaries ) );
		assertEquals( new DimensionsDto( 800, 600 ), normalize( new DimensionsDto( 0, 600 ), boundaries ) );
		assertEquals( new DimensionsDto( 123, 456 ), normalize( new DimensionsDto( 123, 456 ), boundaries ) );
	}

	@Test
	public void normalizeOnAspectRatioWillModifyBasedOnLargestDimension() {
		assertEquals( new DimensionsDto( 800, 600 ),
		              normalize( new DimensionsDto( 800, 200 ), new AspectRatio( 4, 3 ) ) );
		assertEquals( new DimensionsDto( 600, 800 ),
		              normalize( new DimensionsDto( 200, 800 ), new AspectRatio( 3, 4 ) ) );
	}

	@Test
	public void dimensionsFitIn() {
		assertTrue( fitsIn( new DimensionsDto( 1024, 768 ), new DimensionsDto( 1600, 1200 ) ) );
		assertTrue( fitsIn( new DimensionsDto( 1024, 768 ), new DimensionsDto( 1024, 768 ) ) );
		assertFalse( fitsIn( new DimensionsDto( 1600, 1200 ), new DimensionsDto( 1024, 768 ) ) );
		assertFalse( fitsIn( new DimensionsDto( 1024, 1201 ), new DimensionsDto( 1600, 1200 ) ) );
		assertFalse( fitsIn( new DimensionsDto( 1601, 768 ), new DimensionsDto( 1600, 1200 ) ) );
	}

	@Test
	public void scaleToFitIntoWillDownScaleDimensions() {
		DimensionsDto boundaries = new DimensionsDto( 1600, 1200 );

		assertEquals( new DimensionsDto( 1600, 1200 ), scaleToFitIn( new DimensionsDto(), boundaries ) );
		assertEquals( new DimensionsDto( 800, 600 ), scaleToFitIn( new DimensionsDto( 800, 0 ), boundaries ) );
		assertEquals( new DimensionsDto( 800, 600 ),
		              scaleToFitIn( new DimensionsDto( 0, 600 ), boundaries ) );
		assertEquals( new DimensionsDto( 800, 600 ),
		              scaleToFitIn( new DimensionsDto( 800, 600 ), boundaries ) );
		assertEquals( new DimensionsDto( 1600, 1200 ),
		              scaleToFitIn( new DimensionsDto( 1600, 1200 ), boundaries ) );
		assertEquals( new DimensionsDto( 1600, 1200 ),
		              scaleToFitIn( new DimensionsDto( 1600, 0 ), boundaries ) );
		assertEquals( new DimensionsDto( 1600, 1200 ),
		              scaleToFitIn( new DimensionsDto( 0, 1200 ), boundaries ) );
		assertEquals( new DimensionsDto( 1600, 1200 ),
		              scaleToFitIn( new DimensionsDto( 3200, 2400 ), boundaries ) );
		assertEquals( new DimensionsDto( 1600, 1200 ),
		              scaleToFitIn( new DimensionsDto( 3200, 2400 ), boundaries ) );
		assertEquals( new DimensionsDto( 1600, 1200 ),
		              scaleToFitIn( new DimensionsDto( 3200, 0 ), boundaries ) );
		assertEquals( new DimensionsDto( 1600, 1200 ),
		              scaleToFitIn( new DimensionsDto( 0, 2400 ), boundaries ) );
		assertEquals( new DimensionsDto( 1600, 500 ),
		              scaleToFitIn( new DimensionsDto( 3200, 1000 ), boundaries ) );
		assertEquals( new DimensionsDto( 700, 1200 ),
		              scaleToFitIn( new DimensionsDto( 1400, 2400 ), boundaries ) );

		assertEquals( new DimensionsDto( 786, 768 ),
		              scaleToFitIn( new DimensionsDto( 1024, 1000 ), new DimensionsDto( 1024, 768 ) ) );
		assertEquals( new DimensionsDto( 10, 100 ),
		              scaleToFitIn( new DimensionsDto( 1000, 10000 ), new DimensionsDto( 100, 100 ) ) );

		assertEquals( new DimensionsDto( 819, 768 ),
		              scaleToFitIn( new DimensionsDto( 1600, 1500 ), new DimensionsDto( 1024, 768 ) ) );
	}

	@Test
	public void equality() {
		DimensionsDto da1 = new DimensionsDto( 400, 300 );
		DimensionsDto da2 = new DimensionsDto( 800, 600 );

		comparison( da1, da2, false );
		assertEquals( true, calculateAspectRatio( da1 ).equals( calculateAspectRatio( da2 ) ) );
	}

	private void comparison( DimensionsDto left, DimensionsDto right, boolean expected ) {
		assertEquals( expected, right.equals( left ) );
		assertEquals( expected, left.equals( right ) );

		if ( expected ) {
			assertEquals( "hashCode() is broken, it should return identical result for objects that are equal",
			              left.hashCode(), right.hashCode() );
		}
	}
}
