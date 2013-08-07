package com.foreach.imageserver.business;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestImageModifier
{
	private ImageModifier modifier, normalized;
	private Dimensions original;

	@Before
	public void createModifier() {
		modifier = new ImageModifier();
		normalized = null;
		original = new Dimensions( 1024, 768 );
	}

	@Test
	public void defaultIsEmptyModifier() {
		assertTrue( ImageModifier.EMPTY.isEmpty() );
		assertEquals( ImageModifier.EMPTY, modifier );
		assertTrue( modifier.isEmpty() );
	}

	@Test
	public void densityZeroIsIgnoredForEquality() {
		ImageModifier left = new ImageModifier();
		ImageModifier right = new ImageModifier();
		assertEquals( left, right );

		right.setDensity( 100 );
		assertEquals( left, right );

		left.setDensity( 100, 100 );
		assertEquals( left, right );

		right.getDensity().setHeight( 105 );
		assertFalse( left.equals( right ) );
	}

	@Test
	public void hasCrop() {
		assertFalse( modifier.hasCrop() );

		modifier.setCrop( null );
		assertFalse( modifier.hasCrop() );

		modifier.setCrop( new Crop() );
		assertFalse( modifier.hasCrop() );

		modifier.setCrop( new Crop( 10, 10, 100, 0 ) );
		assertFalse( modifier.hasCrop() );

		modifier.setCrop( new Crop( 10, 10, 100, 100 ) );
		assertFalse( modifier.getCrop().isEmpty() );
		assertTrue( modifier.hasCrop() );
	}

	@Test
	public void normalizeOnInvalidDimensionsHasNoEffect() {
		modifier.setWidth( 1200 );
		modifier.setHeight( 100 );
		modifier.setCrop( new Crop( 0, 0, 50, 500 ) );
		modifier.setOutput( ImageType.PNG );
		modifier.setStretch( true );

		assertEquals( modifier, modifier.normalize( null ) );
		assertEquals( modifier, modifier.normalize( new Dimensions( 0, 0 ) ) );
		assertEquals( modifier, modifier.normalize( new Dimensions( -5, 500 ) ) );
		assertEquals( modifier, modifier.normalize( new Dimensions( 5, -500 ) ) );
	}

	@Test
	public void emptyModifierStaysEmpty() {
		normalized = modifier.normalize( original );
		assertNotNull( normalized );
		assertEquals( ImageModifier.EMPTY, normalized );
	}

	@Test
	public void validDimensionsAreKept() {
		modifier.setWidth( 800 );
		modifier.setHeight( 700 );

		assertEquals( modifier, modifier.normalize( original ) );

		checkWidthAndHeight( 800, 700, false, false, 800, 700 );
		checkWidthAndHeight( 800, 700, true, false, 800, 700 );
		checkWidthAndHeight( 1024, 768, false, false, 1024, 768 );
		checkWidthAndHeight( 1024, 768, true, false, 1024, 768 );
	}

	@Test
	public void densityIsBasedOnOutputDimensionsIfNoCrop() {
		checkDensity( 800, 700, 1, 1 );
		checkDensity( 1024, 768, 1, 1 );
		checkDensity( 1025, 768, 2, 1 );
		checkDensity( 1024, 769, 1, 2 );
	}

	@Test
	public void densityIsBasedOnCropAndOutputDimensions() {
		original.setWidth( 100 );
		original.setHeight( 100 );

		checkDensity( 1000, 1000, 10, 10 );
		checkDensity( 1000, 1000, new Crop( 1, 1, 5, 10 ), 200, 100 );
		checkDensity( 1000, 1000, new Crop( 1, 1, 10, 5 ), 100, 200 );
		checkDensity( 1000, 1000, new Crop( 1, 1, 10, 10 ), 100, 100 );
		checkDensity( 500, 1000, new Crop( 1, 1, 10, 10 ), 50, 100 );
		checkDensity( 1000, 500, new Crop( 1, 1, 10, 10 ), 100, 50 );
		checkDensity( 500, 1000, new Crop( 1, 1, 5, 10 ), 100, 100 );
		checkDensity( 1000, 500, new Crop( 1, 1, 10, 5 ), 100, 100 );
	}

	private void checkDensity( int requestedWidth,
	                           int requestedHeight,
	                           int expectedHorizontalDensity,
	                           int expectedVerticalDensity ) {
		checkDensity( requestedWidth, requestedHeight, null, expectedHorizontalDensity, expectedVerticalDensity );
	}

	private void checkDensity( int requestedWidth,
	                           int requestedHeight,
	                           Crop crop,
	                           int expectedHorizontalDensity,
	                           int expectedVerticalDensity ) {
		modifier.setWidth( requestedWidth );
		modifier.setHeight( requestedHeight );
		modifier.setStretch( true );

		if ( crop != null ) {
			modifier.setCrop( crop );
		}

		ImageModifier normalized = modifier.normalize( original );

		Dimensions density = normalized.getDensity();
		assertEquals( expectedHorizontalDensity, density.getWidth() );
		assertEquals( expectedVerticalDensity, density.getHeight() );
	}

	@Test
	public void withoutStretchExceedingDimensionsResultInOriginalOrSmallerAccordingToAspectRatioRequested() {
		checkWidthAndHeight( 1024, 1000, false, false, 786, 768 );
		checkWidthAndHeight( 1200, 768, false, false, 1024, 655 );
		checkWidthAndHeight( 1600, 1200, false, false, 1024, 768 );
		checkWidthAndHeight( 1600, 1500, false, false, 819, 768 );
	}

	@Test
	public void withStretchExceedingDimensionsAreAllowed() {
		checkWidthAndHeight( 1024, 1000, true, false, 1024, 1000 );
		checkWidthAndHeight( 1200, 768, true, false, 1200, 768 );
		checkWidthAndHeight( 1600, 1200, true, false, 1600, 1200 );
	}

	@Test
	public void keepAspectEnforcesAspectRatio() {
		checkWidthAndHeight( 800, 200, false, true, 800, 600 );

		original = new Dimensions( 1000, 500 );
		checkWidthAndHeight( 300, 90, false, true, 300, 150 );

		original = new Dimensions( 500, 1000 );
		checkWidthAndHeight( 300, 90, false, true, 45, 90 );
	}

	@Test
	public void keepAspectButNoStretchModifiesToLargestFittingSideWithAspectRatio() {
		original = new Dimensions( 1000, 500 );
		checkWidthAndHeight( 3000, 900, false, true, 1000, 500 );

		original = new Dimensions( 500, 1000 );
		checkWidthAndHeight( 3000, 900, false, true, 450, 900 );
	}

	@Test
	public void keepAspectAndStretchModifiesToLargestPossibleThatFitsInDimensions() {
		original = new Dimensions( 1000, 500 );
		checkWidthAndHeight( 2000, 2000, true, true, 2000, 1000 );

		original = new Dimensions( 50, 100 );
		checkWidthAndHeight( 2000, 2000, true, true, 1000, 2000 );

		original = new Dimensions( 628, 419 );
//		checkWidthAndHeight( 1000, 500, true, true, 749, 500 );
	}

	@Test
	public void noDimensionsAndNoCropIsAnEmptyModifier() {
		modifier.setWidth( 0 );
		modifier.setHeight( 0 );
		assertEquals( ImageModifier.EMPTY, modifier.normalize( original ) );

		modifier.setStretch( true );
		assertEquals( ImageModifier.EMPTY, modifier.normalize( original ) );
	}

	@Test
	public void unspecifiedDimensionsResultInOriginalAccordingToOriginalAspectRatio() {
		checkWidthAndHeight( 1024, 0, false, false, 1024, 768 );
		checkWidthAndHeight( 1024, 0, true, false, 1024, 768 );
		checkWidthAndHeight( 0, 768, false, false, 1024, 768 );
		checkWidthAndHeight( 0, 768, true, false, 1024, 768 );
	}

	@Test
	public void unspecifiedCombinedWithExceedingDimensionWithoutStretch() {
		checkWidthAndHeight( 1600, 0, false, false, 1024, 768 );
		checkWidthAndHeight( 1600, 0, true, false, 1600, 1200 );
		checkWidthAndHeight( 0, 1200, false, false, 1024, 768 );
		checkWidthAndHeight( 0, 1200, true, false, 1600, 1200 );
	}

	@Test
	public void exceedingDimensionsShouldSnapToLargestSide() {
		original = new Dimensions( 100, 100 );

		checkWidthAndHeight( 10000, 1000, false, false, 100, 10 );
		checkWidthAndHeight( 1000, 10000, false, false, 10, 100 );
		checkWidthAndHeight( 1000, 100, false, false, 100, 10 );
		checkWidthAndHeight( 100, 1000, false, false, 10, 100 );
	}

	private void checkWidthAndHeight( int requestedWidth,
	                                  int requestedHeight,
	                                  boolean stretch,
	                                  boolean keepAspect,
	                                  int normalizedWidth,
	                                  int normalizedHeight ) {
		modifier = new ImageModifier();
		modifier.setWidth( requestedWidth );
		modifier.setHeight( requestedHeight );
		modifier.setStretch( stretch );
		modifier.setKeepAspect( keepAspect );

		normalized = modifier.normalize( original );
		assertEquals( normalizedWidth, normalized.getWidth() );
		assertEquals( normalizedHeight, normalized.getHeight() );
		assertEquals( stretch, normalized.isStretch() );
	}

	@Test
	public void noDimensionsButCropSpecifiedThenCropDimensionsAreUsed() {
		modifier.setCrop( new Crop( 50, 50, 700, 700 ) );

		normalized = modifier.normalize( original );
		assertEquals( 700, normalized.getWidth() );
		assertEquals( 700, normalized.getHeight() );
	}

	@Test
	public void cropWithinBoundariesIsReturned() {
		Crop expected = new Crop( 50, 50, 700, 700 );

		modifier.setCrop( expected );

		normalized = modifier.normalize( original );
		assertEquals( expected, normalized.getCrop() );
	}

	@Test
	public void cropThatEqualsOriginalDimensionsIsRemoved() {
		modifier.setCrop( new Crop( 0, 0, 1024, 768 ) );
		assertTrue( modifier.hasCrop() );

		normalized = modifier.normalize( original );
		assertFalse( normalized.hasCrop() );
	}

	@Test
	public void cropThatExceedsTheEntireImageResultsInNoCrop() {
		modifier.setCrop( new Crop( -1, -1, 2000, 2000 ) );

		normalized = modifier.normalize( original );
		assertFalse( normalized.hasCrop() );
	}

	@Test
	public void cropIsNormalizedOnImageDimensions() {
		Crop mockedCrop = mock( Crop.class );
		modifier.setCrop( mockedCrop );

		when( mockedCrop.isEmpty() ).thenReturn( false );
		when( mockedCrop.normalize( original ) ).thenReturn( new Crop() );

		normalized = modifier.normalize( original );
		verify( mockedCrop, times( 1 ) ).normalize( original );
	}

	@Test
	public void withoutStretchDimensionsAreLimitedToCropDimensions() {
		modifier.setCrop( new Crop( 50, 50, 300, 200 ) );
		modifier.setWidth( 600 );
		modifier.setHeight( 400 );
		modifier.setStretch( false );

		normalized = modifier.normalize( original );
		assertEquals( 300, normalized.getWidth() );
		assertEquals( 200, normalized.getHeight() );
	}

	@Test
	public void withStretchDimensionsAreScaledAccordingToCrop() {
		modifier.setCrop( new Crop( 50, 50, 300, 200 ) );
		modifier.setWidth( 600 );
		modifier.setStretch( true );

		normalized = modifier.normalize( original );
		assertEquals( 600, normalized.getWidth() );
		assertEquals( 400, normalized.getHeight() );

		modifier.setWidth( 0 );
		modifier.setHeight( 400 );

		normalized = modifier.normalize( original );
		assertEquals( 600, normalized.getWidth() );
		assertEquals( 400, normalized.getHeight() );
	}
}
