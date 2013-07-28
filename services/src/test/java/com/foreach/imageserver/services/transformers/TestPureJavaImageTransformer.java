package com.foreach.imageserver.services.transformers;

import com.foreach.imageserver.business.ImageType;
import com.foreach.imageserver.services.ImageTestData;
import org.junit.Test;

import java.util.Arrays;

public class TestPureJavaImageTransformer extends AbstractImageTransformerTest
{
	@Override
	protected ImageTransformer createTransformer() {
		return new PureJavaImageTransformer();
	}

	@Test
	public void dimensionsCalculatedOk() {
		dimensions( ImageTestData.EARTH, ImageTransformerPriority.PREFERRED, true );
		dimensions( ImageTestData.SUNSET, ImageTransformerPriority.PREFERRED, true );
		dimensions( ImageTestData.HIGH_RES, ImageTransformerPriority.PREFERRED, true );
		dimensions( ImageTestData.ICE_ROCK, ImageTransformerPriority.PREFERRED, true );
		dimensions( ImageTestData.KAAIMAN_JPEG, ImageTransformerPriority.PREFERRED, true );
		dimensions( ImageTestData.KAAIMAN_GIF, ImageTransformerPriority.PREFERRED, true );
		dimensions( ImageTestData.KAAIMAN_PNG, ImageTransformerPriority.PREFERRED, true );
		dimensions( ImageTestData.TEST_PNG, ImageTransformerPriority.PREFERRED, true );
		dimensions( ImageTestData.TEST_TRANSPARENT_PNG, ImageTransformerPriority.PREFERRED, true );
		dimensions( ImageTestData.CMYK_COLOR, ImageTransformerPriority.PREFERRED, true );
		dimensions( ImageTestData.SMALL_TIFF, ImageTransformerPriority.PREFERRED, true );
		dimensions( ImageTestData.LARGE_TIFF, ImageTransformerPriority.PREFERRED, true );
		dimensions( ImageTestData.HUGE_TIFF, ImageTransformerPriority.PREFERRED, true );
		dimensions( ImageTestData.SINGLE_PAGE_PDF, ImageTransformerPriority.PREFERRED, true );
		dimensions( ImageTestData.MULTI_PAGE_PDF, ImageTransformerPriority.PREFERRED, true );
		dimensions( ImageTestData.ANIMATED_GIF, ImageTransformerPriority.PREFERRED, true );
	}

	@Test
	public void cantCalculateDimensions() {
		dimensions( ImageTestData.KAAIMAN_SVG, ImageTransformerPriority.UNABLE, false );
		dimensions( ImageTestData.KAAIMAN_EPS, ImageTransformerPriority.UNABLE, false );
		dimensions( ImageTestData.TEST_EPS, ImageTransformerPriority.UNABLE, false );
		dimensions( ImageTestData.BRUXELLES_EPS, ImageTransformerPriority.UNABLE, false );
		dimensions( ImageTestData.BRUXELLES_ECHO_EPS, ImageTransformerPriority.UNABLE, false );
	}
/*
	@Test
	public void smallResizes() {
		for ( ImageTestData image : Arrays.asList( ImageTestData.EARTH, ImageTestData.SUNSET, ImageTestData.HIGH_RES,
		                                           ImageTestData.ICE_ROCK, ImageTestData.KAAIMAN_JPEG ) ) {
			modify( image, scale( image, 0.33f, ImageType.JPEG ), ImageTransformerPriority.PREFERRED, true );
		}
//ImageTestData.CMYK_COLOR,
		for ( ImageTestData image : Arrays.asList( ImageTestData.KAAIMAN_GIF,
		                                           ImageTestData.KAAIMAN_PNG, ImageTestData.TEST_PNG,
		                                           ImageTestData.TEST_TRANSPARENT_PNG, ImageTestData.SMALL_TIFF,
		                                           ImageTestData.LARGE_TIFF, ImageTestData.HUGE_TIFF ) ) {
			modify( image, scale( image, 0.33f, ImageType.JPEG ), ImageTransformerPriority.PREFERRED, true );
		}
	}
	*/
}
