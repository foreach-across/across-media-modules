package com.foreach.imageserver.math;

import com.foreach.imageserver.dto.CropDto;
import com.foreach.imageserver.dto.DimensionsDto;
import org.junit.jupiter.api.Test;

import static com.foreach.imageserver.math.ImageServerConversionUtils.*;
import static org.junit.jupiter.api.Assertions.*;

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
	public void normalizeOnAspectRatioWillExtendBasedOnLargestDimension() {
		assertEquals( new DimensionsDto( 800, 600 ),
		              normalize( new DimensionsDto( 800, 200 ), new AspectRatio( 4, 3 ) ) );
		assertEquals( new DimensionsDto( 800, 600 ),
		              normalize( new DimensionsDto( 200, 600 ), new AspectRatio( 4, 3 ) ) );
		assertEquals( new DimensionsDto( 600, 800 ),
		              normalize( new DimensionsDto( 200, 800 ), new AspectRatio( 3, 4 ) ) );

		assertEquals( new DimensionsDto( 1000, 1000 ),
		              normalize( new DimensionsDto( 0, 1000 ), new AspectRatio( 1, 1 ) ) );
		assertEquals( new DimensionsDto( 1000, 1000 ),
		              normalize( new DimensionsDto( 1000, 0 ), new AspectRatio( 1, 1 ) ) );

		assertEquals( new DimensionsDto( 800, 600 ),
		              normalize( new DimensionsDto( 600, 600 ), new AspectRatio( 4, 3 ) ) );
		assertEquals( new DimensionsDto( 600, 800 ),
		              normalize( new DimensionsDto( 600, 600 ), new AspectRatio( 3, 4 ) ) );
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
	public void cropIsWhithinBox() {
		assertTrue( isWithinBox( new CropDto( 0, 0, 100, 100 ), new DimensionsDto( 100, 100 ) ) );
		assertTrue( isWithinBox( new CropDto( 50, 10, 100, 100 ), new DimensionsDto( 1000, 1000 ) ) );
		assertFalse( isWithinBox( new CropDto( 0, 0, 100, 100 ), new DimensionsDto( 90, 100 ) ) );
		assertFalse( isWithinBox( new CropDto( -50, 10, 100, 100 ), new DimensionsDto( 1000, 1000 ) ) );
		assertFalse( isWithinBox( new CropDto( 50, -10, 100, 100 ), new DimensionsDto( 1000, 1000 ) ) );
		assertFalse( isWithinBox( new CropDto( -50, -10, 100, 100 ), new DimensionsDto( 1000, 1000 ) ) );
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
	public void normalizeCropToBox() {
		CropDto crop = new CropDto( 0, 0, 500, 300 );
		crop.setSource( new DimensionsDto( 500, 300 ) );

		CropDto normalized = normalize( crop, new DimensionsDto( 250, 150 ) );
		assertEquals( 0, normalized.getX() );
		assertEquals( 0, normalized.getY() );
		assertEquals( 250, normalized.getWidth() );
		assertEquals( 150, normalized.getHeight() );

		crop = new CropDto( 30, 90, 300, 600 );
		crop.setSource( new DimensionsDto( 900, 3000 ) );

		normalized = normalize( crop, new DimensionsDto( 300, 1000 ) );
		assertEquals( 10, normalized.getX() );
		assertEquals( 30, normalized.getY() );
		assertEquals( 100, normalized.getWidth() );
		assertEquals( 200, normalized.getHeight() );
	}

	@Test
	public void normalizeCropToBoxRequiresSourceToBeSet() {
		CropDto crop = new CropDto( 0, 0, 500, 300 );
		assertThrows( IllegalArgumentException.class, () -> {
			normalize( crop, new DimensionsDto( 250, 150 ) );
		} );
	}

	@Test
	public void dimensionsAndAspectRatioEquality() {
		DimensionsDto da1 = new DimensionsDto( 400, 300 );
		DimensionsDto da2 = new DimensionsDto( 800, 600 );

		comparison( da1, da2, false );
		assertEquals( true, calculateAspectRatio( da1 ).equals( calculateAspectRatio( da2 ) ) );
	}

	@Test
	public void distanceBetweenEqualsIsZero() {
		DimensionsDto one = new DimensionsDto( 800, 600 );
		DimensionsDto two = new DimensionsDto( 800, 600 );

		assertEquals( 0, calculateDistance( one, two ) );
		assertEquals( 0, calculateDistance( two, one ) );
	}

	@Test
	public void distanceIsPositiveIfDimensionIsLarger() {
		DimensionsDto one = new DimensionsDto( 800, 600 );

		assertEquals( ( 1600 * 1200 - 800 * 600 ), calculateDistance( one, new DimensionsDto( 1600, 1200 ) ) );
		assertEquals( ( 900 * 700 - 800 * 600 ), calculateDistance( one, new DimensionsDto( 900, 700 ) ) );
		assertEquals( Math.abs( 200 * 600 - 800 * 600 ), calculateDistance( new DimensionsDto( 200, 600 ), one ) );
	}

	@Test
	public void distanceIsNegativeIfDimensionIsSmaller() {
		DimensionsDto one = new DimensionsDto( 800, 600 );

		assertEquals( -( 1600 * 1200 - 800 * 600 ), calculateDistance( new DimensionsDto( 1600, 1200 ), one ) );
		assertEquals( -( 900 * 700 - 800 * 600 ), calculateDistance( new DimensionsDto( 900, 700 ), one ) );

		assertEquals( -1, calculateDistance( new DimensionsDto( 600, 800 ), one ) );
		assertEquals( -1, calculateDistance( one, new DimensionsDto( 600, 800 ) ) );

		assertEquals( -Math.abs( 200 * 600 - 800 * 600 ), calculateDistance( one, new DimensionsDto( 200, 600 ) ) );
	}

	@Test
	public void extendCropCanReturnNegativeCoordinates() {
		AspectRatio aspectRatio = new AspectRatio( 1, 1 );

		assertEquals( new CropDto( 0, -25, 100, 100 ), extendCrop( new CropDto( 0, 0, 100, 50 ), aspectRatio ) );
		assertEquals( new CropDto( -25, 0, 100, 100 ), extendCrop( new CropDto( 0, 0, 50, 100 ), aspectRatio ) );
		assertEquals( new CropDto( 50, 100, 600, 600 ), extendCrop( new CropDto( 200, 100, 300, 600 ), aspectRatio ) );
		assertEquals( new CropDto( 50, -100, 600, 600 ), extendCrop( new CropDto( 50, 100, 600, 200 ), aspectRatio ) );
	}

	@Test
	public void shrinkCropTests() {
		AspectRatio aspectRatio = new AspectRatio( 1, 1 );

		assertEquals( new CropDto( 100, 0, 600, 600 ), shrinkCrop( new CropDto( 0, 0, 800, 600 ), aspectRatio ) );
		assertEquals( new CropDto( 50, 150, 600, 600 ), shrinkCrop( new CropDto( 50, 50, 600, 800 ), aspectRatio ) );
	}

	@Test
	public void moveCropsToFit() {
		DimensionsDto boxDto = new DimensionsDto( 800, 800 );

		assertEquals( new CropDto( 0, 0, 800, 600 ), moveToFit( new CropDto( -50, 0, 800, 600 ), boxDto ) );
		assertEquals( new CropDto( 0, 0, 640, 480 ), moveToFit( new CropDto( 0, -25, 640, 480 ), boxDto ) );
		assertEquals( new CropDto( 0, 200, 800, 600 ), moveToFit( new CropDto( 300, 500, 800, 600 ), boxDto ) );
	}

	private void comparison( DimensionsDto left, DimensionsDto right, boolean expected ) {
		assertEquals( expected, right.equals( left ) );
		assertEquals( expected, left.equals( right ) );

		if ( expected ) {
			assertEquals( left.hashCode(), right.hashCode(), "hashCode() is broken, it should return identical result for objects that are equal" );
		}
	}
}
