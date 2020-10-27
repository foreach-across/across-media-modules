package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Crop;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.dto.CropDto;
import com.foreach.imageserver.dto.DimensionsDto;
import com.foreach.imageserver.dto.ImageModificationDto;
import com.foreach.imageserver.dto.ImageResolutionDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CropGeneratorUtilTest
{
	private CropGeneratorUtil cropGeneratorUtil = new CropGeneratorUtilImpl();

	@Test
	public void normalizeModification() {
		ImageModificationDto mod = new ImageModificationDto();
		CropDto crop = new CropDto( 0, 0, 450, 300 );
		crop.setBox( new DimensionsDto( 450, 300 ) );

		mod.setCrop( crop );
		mod.setResolution( new ImageResolutionDto( 300, 200 ) );

		Image original = new Image();
		original.setDimensions( new Dimensions( 800, 600 ) );

		cropGeneratorUtil.normalizeModificationDto( original, mod );

		assertEquals( new CropDto( 0, 0, 800, 600 ), mod.getCrop() );
	}

	@Test
	public void normalizeWithoutCropReturnsTheLargestCropPossibleFromCenter() {
		Image original = new Image();
		original.setDimensions( new Dimensions( 4000, 2000 ) );

		ImageModificationDto mod = new ImageModificationDto();
		mod.setResolution( new ImageResolutionDto( 50, 100 ) );

		cropGeneratorUtil.normalizeModificationDto( original, mod );
		assertEquals( new ImageResolutionDto( 50, 100 ), mod.getResolution() );
		assertEquals( new CropDto( 1500, 0, 1000, 2000 ), mod.getCrop() );

		mod = new ImageModificationDto();
		mod.setResolution( new ImageResolutionDto( 400, 100 ) );

		cropGeneratorUtil.normalizeModificationDto( original, mod );
		assertEquals( new ImageResolutionDto( 400, 100 ), mod.getResolution() );
		assertEquals( new CropDto( 0, 500, 4000, 1000 ), mod.getCrop() );

		mod = new ImageModificationDto();
		mod.setResolution( new ImageResolutionDto( 400, 400 ) );

		cropGeneratorUtil.normalizeModificationDto( original, mod );
		assertEquals( new ImageResolutionDto( 400, 400 ), mod.getResolution() );
		assertEquals( new CropDto( 1000, 0, 2000, 2000 ), mod.getCrop() );
	}

	@Test
	public void sameAspectRatioIsSimplyAScale() {
		Image original = new Image();
		original.setDimensions( new Dimensions( 4000, 2000 ) );

		ImageModificationDto mod = new ImageModificationDto();
		mod.setResolution( new ImageResolutionDto( 800, 400 ) );

		cropGeneratorUtil.normalizeModificationDto( original, mod );
		assertEquals( new ImageResolutionDto( 800, 400 ), mod.getResolution() );
		assertEquals( new CropDto( 0, 0, 4000, 2000 ), mod.getCrop() );

		mod = new ImageModificationDto();
		mod.setResolution( new ImageResolutionDto( 8000, 4000 ) );

		cropGeneratorUtil.normalizeModificationDto( original, mod );
		assertEquals( new ImageResolutionDto( 8000, 4000 ), mod.getResolution() );
		assertEquals( new CropDto( 0, 0, 4000, 2000 ), mod.getCrop() );
	}

	@Test
	public void applyExactResolution() {
		Dimensions result = cropGeneratorUtil.applyResolution( image( 1000, 2000 ), resolution( 3000, 4000 ) );
		assertEquals( 3000, result.getWidth() );
		assertEquals( 4000, result.getHeight() );
	}

	@Test
	public void applyUnboundedWidthResolution() {
		Dimensions result = cropGeneratorUtil.applyResolution( image( 1000, 2000 ), resolution( 0, 4000 ) );
		assertEquals( 2000, result.getWidth() );
		assertEquals( 4000, result.getHeight() );
	}

	@Test
	public void applyUnboundedHeightResolution() {
		Dimensions result = cropGeneratorUtil.applyResolution( image( 1000, 2000 ), resolution( 3000, 0 ) );
		assertEquals( 3000, result.getWidth() );
		assertEquals( 6000, result.getHeight() );
	}

	@Test
	public void calculateArea() {
		Crop crop = new Crop( 1234, 4321, 254, 782 );
		assertEquals( 198628, cropGeneratorUtil.area( crop ) );
	}

	@Test
	public void noIntersection() {
		assertNull(
				cropGeneratorUtil.intersect( new Crop( 1000, 1000, 1000, 1000 ), new Crop( 2001, 1000, 500, 500 ) ) );
		assertNull(
				cropGeneratorUtil.intersect( new Crop( 1000, 1000, 1000, 1000 ), new Crop( 499, 1000, 500, 500 ) ) );
		assertNull(
				cropGeneratorUtil.intersect( new Crop( 1000, 1000, 1000, 1000 ), new Crop( 1000, 499, 500, 500 ) ) );
		assertNull(
				cropGeneratorUtil.intersect( new Crop( 1000, 1000, 1000, 1000 ), new Crop( 1000, 2001, 500, 500 ) ) );
	}

	@Test
	public void intersection() {
		Crop intersection1 = cropGeneratorUtil.intersect( new Crop( 500, 500, 1000, 1000 ),
		                                                  new Crop( 1000, 1000, 1000, 1000 ) );
		assertEquals( new Crop( 1000, 1000, 500, 500 ), intersection1 );

		Crop intersection2 = cropGeneratorUtil.intersect( new Crop( 1000, 1000, 1000, 1000 ),
		                                                  new Crop( 500, 500, 1000, 1000 ) );
		assertEquals( new Crop( 1000, 1000, 500, 500 ), intersection2 );

		Crop intersection3 = cropGeneratorUtil.intersect( new Crop( 500, 500, 2000, 2000 ),
		                                                  new Crop( 600, 800, 1000, 900 ) );
		assertEquals( new Crop( 600, 800, 1000, 900 ), intersection3 );
	}

	private Image image( int w, int h ) {
		Image image = new Image();
		image.setDimensions( new Dimensions( w, h ) );
		return image;
	}

	private ImageResolution resolution( int w, int h ) {
		ImageResolution resolution = new ImageResolution();
		resolution.setWidth( w );
		resolution.setHeight( h );
		return resolution;
	}

}
