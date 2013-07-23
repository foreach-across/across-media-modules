package com.foreach.imageserver.business;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestCrop
{
	@Test
	public void emptyIfNoCoordinatesSet() {
		assertTrue( new Crop().isEmpty() );
	}

	@Test
	public void dimensions() {
		assertEquals( new Dimensions( 800, 600 ), new Crop( 50, 60, 800, 600 ).getDimensions() );
	}

	@Test
	public void emptyIfNoValidDimensions() {
		assertTrue( new Crop( 0, 0, 0, 0 ).isEmpty() );
		assertTrue( new Crop( 15, 50, 0, 0 ).isEmpty() );
		assertTrue( new Crop( 0, 0, 100, 0 ).isEmpty() );
		assertTrue( new Crop( 0, 0, 0, 100 ).isEmpty() );
		assertTrue( new Crop( 15, 50, 0, 0 ).isEmpty() );
		assertTrue( new Crop( 15, 50, -1, 100 ).isEmpty() );
		assertTrue( new Crop( 15, 50, 100, -10 ).isEmpty() );
		assertTrue( new Crop( 15, 50, -50, -20 ).isEmpty() );
	}

	@Test
	public void notEmptyIfValidDimensions() {
		assertFalse( new Crop( 15, 50, 1, 2 ).isEmpty() );
		assertFalse( new Crop( 0, 0, 100, 50 ).isEmpty() );
	}

	@Test
	public void cropWithinBoundariesIsOk() {
		Crop crop = new Crop( 50, 70, 100, 200 );

		Crop normalized = crop.normalize( new Dimensions( 200, 300 ) );
		assertEquals( crop, normalized );
	}

	@Test
	public void cropExceedingEntireDimensionsSnapsToDimensions() {
		Dimensions dimensions = new Dimensions( 200, 300 );

		assertEquals( new Crop( 0, 0, 200, 300 ), new Crop( 0, 0, 200, 300 ).normalize( dimensions ) );
		assertEquals( new Crop( 0, 0, 200, 300 ), new Crop( 0, 0, 250, 301 ).normalize( dimensions ) );
		assertEquals( new Crop( 0, 0, 195, 290 ), new Crop( -5, -10, 200, 300 ).normalize( dimensions ) );
		assertEquals( new Crop( 0, 0, 200, 300 ), new Crop( -5, -10, 250, 311 ).normalize( dimensions ) );
	}

	@Test
	public void cropExceedingInOneDimensionIsModified() {
		Dimensions dimensions = new Dimensions( 200, 300 );

		assertEquals( new Crop( 50, 70, 100, 230 ), new Crop( 50, 70, 100, 300 ).normalize( dimensions ) );
		assertEquals( new Crop( 50, 70, 150, 200 ), new Crop( 50, 70, 160, 200 ).normalize( dimensions ) );
		assertEquals( new Crop( 0, 0, 95, 190 ), new Crop( -5, -10, 100, 200 ).normalize( dimensions ) );
		assertEquals( new Crop( 50, 70, 150, 230 ), new Crop( 50, 70, 160, 300 ).normalize( dimensions ) );
	}
}
