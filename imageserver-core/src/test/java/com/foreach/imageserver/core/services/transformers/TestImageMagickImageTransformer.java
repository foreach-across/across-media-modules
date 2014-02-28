package com.foreach.imageserver.core.services.transformers;

import com.foreach.imageserver.core.business.Crop;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageModifier;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.services.ImageTestData;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class TestImageMagickImageTransformer extends AbstractImageTransformerTest
{
	@Override
	protected ImageTransformer createTransformer() {
		return new ImageMagickImageTransformer( 0, "c:/imagemagick", true );
	}

	@Test
	public void cantCalculateDimensionsForGhostScriptIfNotInstalled() {
		ImageMagickImageTransformer otherTransformer = new ImageMagickImageTransformer( 0, "c:/imagemagick", false );

		ImageCalculateDimensionsAction action =
				new ImageCalculateDimensionsAction( ImageTestData.TEST_EPS.getImageFile() );
		assertEquals( ImageTransformerPriority.UNABLE, otherTransformer.canExecute( action ) );

		action = new ImageCalculateDimensionsAction( ImageTestData.SINGLE_PAGE_PDF.getImageFile() );
		assertEquals( ImageTransformerPriority.UNABLE, otherTransformer.canExecute( action ) );
	}

	@Test(expected = AssertionError.class)
	public void animatedGifSupportIsIncomplete() {
		dimensions( ImageTestData.ANIMATED_GIF, ImageTransformerPriority.FALLBACK, true );
	}

	@Test
	public void dimensionsCalculatedOk() {
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

	@Test
	public void referenceActionOnLargeJPEG() {
		modify( "crop+downscale-custom", ImageTestData.EARTH,
		        cropAndScale( ImageType.JPEG, createCrop( ImageTestData.EARTH ), 0.5f, 0.33f ),
		        ImageTransformerPriority.PREFERRED, true );
	}

	@Test
	public void referenceActionOnLargeTIF() {
		modify( "crop+downscale-custom", ImageTestData.LARGE_TIFF,
		        cropAndScale( ImageType.JPEG, createCrop( ImageTestData.LARGE_TIFF ), 0.5f, 0.33f ),
		        ImageTransformerPriority.PREFERRED, true );
		modify( "crop+downscale-custom", ImageTestData.LARGE_TIFF,
		        cropAndScale( ImageType.PNG, createCrop( ImageTestData.LARGE_TIFF ), 0.5f, 0.33f ),
		        ImageTransformerPriority.PREFERRED, true );
	}

	@Test
	public void referenceActionOnJPEGWithCMYK() {
		modify( "crop+upscale-custom", ImageTestData.CMYK_COLOR,
		        cropAndScale( ImageType.JPEG, createCrop( ImageTestData.CMYK_COLOR ), 1.5f, 1.33f ),
		        ImageTransformerPriority.PREFERRED, true );
	}

	@Test
	public void referenceActionOnJPEGWithColorTranslationProblems() {
		modify( "crop+upscale-custom", ImageTestData.ICE_ROCK,
		        cropAndScale( ImageType.PNG, createCrop( ImageTestData.ICE_ROCK ), 1.5f, 1.33f ),
		        ImageTransformerPriority.PREFERRED, true );
	}

	@Test
	public void croppedEpsImageShouldScaleInHighQuality() {
		ImageTestData image = ImageTestData.TEST_EPS;

		assertEquals( "Test data has been modified - test unreliable", 321, image.getDimensions().getWidth() );
		assertEquals( "Test data has been modified - test unreliable", 583, image.getDimensions().getHeight() );

		ImageModifier modifier = new ImageModifier();
		modifier.setOutput( ImageType.PNG );
		modifier.setStretch( true );
		modifier.setCrop( new Crop( 10, 10, 300, 250 ) );

		// uniform 3 times larger should be sharp
		modifier.setWidth( 1500 );
		modifier.setHeight( 1250 );
		modifier.setDensity( 5 );
		modify( "crop+x5", image, modifier, ImageTransformerPriority.PREFERRED, true );

		// Non uniform scale of the crop
		modifier.setWidth( 1500 );
		modifier.setHeight( 500 );
		modifier.setDensity( 5, 2 );
		modify( "crop+x5x2", image, modifier, ImageTransformerPriority.PREFERRED, true );
	}

	@Test
	public void epsImageShouldScaleInHighQuality() {
		ImageTestData image = ImageTestData.BRUXELLES_ECHO_EPS;

		assertEquals( "Test data has been modified - test unreliable", 130, image.getDimensions().getWidth() );
		assertEquals( "Test data has been modified - test unreliable", 104, image.getDimensions().getHeight() );

		ImageModifier modifier = new ImageModifier();
		modifier.setOutput( ImageType.PNG );
		modifier.setStretch( true );

		// uniform 3 times larger should be sharp
		modifier.setWidth( 130 * 3 );
		modifier.setHeight( 104 * 3 );
		modifier.setDensity( 3 );
		modify( "x3", image, modifier, ImageTransformerPriority.PREFERRED, true );

		// 10 times larger should also be sharp
		modifier.setWidth( 130 * 10 );
		modifier.setHeight( 104 * 10 );
		modifier.setDensity( 10 );
		modify( "x10", image, modifier, ImageTransformerPriority.PREFERRED, true );

		// Non uniform scales
		modifier.setWidth( 130 * 3 );
		modifier.setHeight( 104 * 6 );
		modifier.setDensity( 3, 6 );
		modify( "x3x6", image, modifier, ImageTransformerPriority.PREFERRED, true );

		modifier.setWidth( 130 * 10 );
		modifier.setHeight( 104 * 3 );
		modifier.setDensity( 10, 3 );
		modify( "x10x3", image, modifier, ImageTransformerPriority.PREFERRED, true );
	}

	@Test
	public void runAllActionsOnReferenceImages() {
		Collection<ImageTestData> images =
				Arrays.asList( ImageTestData.KAAIMAN_JPEG, ImageTestData.KAAIMAN_PNG, ImageTestData.KAAIMAN_GIF,
				               ImageTestData.KAAIMAN_EPS, ImageTestData.KAAIMAN_SVG );

		for ( ImageTestData image : images ) {
			runAllActions( image, ImageType.JPEG );
			runAllActions( image, ImageType.PNG );
		}
	}

	private void runAllActions( ImageTestData image, ImageType output ) {
		modify( "downscale", image, scale( image, output, 0.33f ), ImageTransformerPriority.PREFERRED, true );
		modify( "upscale", image, scale( image, output, 1.253f ), ImageTransformerPriority.PREFERRED, true );
		modify( "downscale-custom", image, scale( image, output, 0.33f, 0.5f ), ImageTransformerPriority.PREFERRED,
		        true );
		modify( "upscale-custom", image, scale( image, output, 1.5f, 1.25f ), ImageTransformerPriority.PREFERRED,
		        true );
		modify( "crop", image, crop( output, createCrop( image ) ), ImageTransformerPriority.PREFERRED, true );
		modify( "crop+downscale", image, cropAndScale( output, createCrop( image ), 0.33f ),
		        ImageTransformerPriority.PREFERRED, true );
		modify( "crop+upscale", image, cropAndScale( output, createCrop( image ), 1.25f ),
		        ImageTransformerPriority.PREFERRED, true );
		modify( "crop+downscale-custom", image, cropAndScale( output, createCrop( image ), 0.33f, 0.5f ),
		        ImageTransformerPriority.PREFERRED, true );
		modify( "crop+upscale-custom", image, cropAndScale( output, createCrop( image ), 1.5f, 1.25f ),
		        ImageTransformerPriority.PREFERRED, true );
	}

	private ImageModifier scale( ImageTestData image, ImageType output, float delta ) {
		return scale( image, output, delta, delta );
	}

	private ImageModifier scale( ImageTestData image, ImageType output, float deltaWidth, float deltaHeight ) {
		ImageModifier mod = new ImageModifier();
		mod.setStretch( true );
		mod.setOutput( output );
		mod.setWidth( Math.round( image.getDimensions().getWidth() * deltaWidth ) );
		mod.setHeight( Math.round( image.getDimensions().getHeight() * deltaHeight ) );

		return mod;
	}

	private ImageModifier crop( ImageType output, Crop crop ) {
		return cropAndScale( output, crop, 1f );
	}

	private ImageModifier cropAndScale( ImageType output, Crop crop, float delta ) {
		return cropAndScale( output, crop, delta, delta );
	}

	private ImageModifier cropAndScale( ImageType output, Crop crop, float deltaWidth, float deltaHeight ) {
		ImageModifier mod = new ImageModifier();
		mod.setStretch( true );
		mod.setOutput( output );
		mod.setCrop( crop );
		mod.setWidth( Math.round( crop.getWidth() * deltaWidth ) );
		mod.setHeight( Math.round( crop.getHeight() * deltaHeight ) );

		return mod;
	}

	private Crop createCrop( ImageTestData image ) {
		return createCrop( image.getDimensions() );
	}

	private Crop createCrop( Dimensions dimensions ) {
		Crop crop = new Crop();
		crop.setX( Math.round( dimensions.getWidth() * 0.25f ) );
		crop.setX( Math.round( dimensions.getHeight() * 0.33f ) );
		crop.setWidth( Math.round( dimensions.getWidth() * 0.5f ) );
		crop.setHeight( Math.round( dimensions.getHeight() * 0.33f ) );

		return crop;
	}
}
