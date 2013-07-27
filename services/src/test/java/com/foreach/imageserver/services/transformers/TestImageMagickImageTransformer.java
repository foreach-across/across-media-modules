package com.foreach.imageserver.services.transformers;

import com.foreach.imageserver.services.ImageTestData;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestImageMagickImageTransformer extends AbstractImageTransformerTest
{
	@Override
	protected ImageTransformer createTransformer()
	{
		return new ImageMagickImageTransformer( "c:/imagemagick", true );
	}

	@Test
	public void cantCalculateDimensionsForGhostScriptIfNotInstalled()
	{
		ImageMagickImageTransformer otherTransformer = new ImageMagickImageTransformer( "c:/imagemagick", false );

		ImageCalculateDimensionsAction action =
				new ImageCalculateDimensionsAction( ImageTestData.TEST_EPS.getImageFile() );
		assertEquals( ImageTransformerPriority.UNABLE, otherTransformer.canExecute( action ) );

		action = new ImageCalculateDimensionsAction( ImageTestData.SINGLE_PAGE_PDF.getImageFile() );
		assertEquals( ImageTransformerPriority.UNABLE, otherTransformer.canExecute( action ) );
	}

	@Test(expected = AssertionError.class)
	public void animatedGifSupportIsIncomplete()
	{
		dimensions( ImageTestData.ANIMATED_GIF, ImageTransformerPriority.FALLBACK, true );
	}

	@Test
	public void dimensionsCalculatedOk()
	{
		dimensions( ImageTestData.EARTH, ImageTransformerPriority.FALLBACK, true );
		dimensions( ImageTestData.SUNSET, ImageTransformerPriority.FALLBACK, true );
		dimensions( ImageTestData.HIGH_RES, ImageTransformerPriority.FALLBACK, true );
		dimensions( ImageTestData.CMYK_COLOR, ImageTransformerPriority.FALLBACK, true );
		dimensions( ImageTestData.ICE_ROCK, ImageTransformerPriority.FALLBACK, true );
		dimensions( ImageTestData.KAAIMAN_JPEG, ImageTransformerPriority.FALLBACK, true );
		dimensions( ImageTestData.KAAIMAN_GIF, ImageTransformerPriority.FALLBACK, true );
		dimensions( ImageTestData.KAAIMAN_PNG, ImageTransformerPriority.FALLBACK, true );
		dimensions( ImageTestData.TEST_PNG, ImageTransformerPriority.FALLBACK, true );
		dimensions( ImageTestData.TEST_TRANSPARENT_PNG, ImageTransformerPriority.FALLBACK, true );
		dimensions( ImageTestData.KAAIMAN_SVG, ImageTransformerPriority.FALLBACK, true );
		dimensions( ImageTestData.TEST_EPS, ImageTransformerPriority.FALLBACK, true );
		dimensions( ImageTestData.KAAIMAN_EPS, ImageTransformerPriority.FALLBACK, true );
		dimensions( ImageTestData.BRUXELLES_EPS, ImageTransformerPriority.FALLBACK, true );
		dimensions( ImageTestData.BRUXELLES_ECHO_EPS, ImageTransformerPriority.FALLBACK, true );
		dimensions( ImageTestData.SINGLE_PAGE_PDF, ImageTransformerPriority.FALLBACK, true );
		dimensions( ImageTestData.MULTI_PAGE_PDF, ImageTransformerPriority.FALLBACK, true );
		dimensions( ImageTestData.SMALL_TIFF, ImageTransformerPriority.FALLBACK, true );
		dimensions( ImageTestData.LARGE_TIFF, ImageTransformerPriority.FALLBACK, true );
		dimensions( ImageTestData.HUGE_TIFF, ImageTransformerPriority.FALLBACK, true );
	}
}
