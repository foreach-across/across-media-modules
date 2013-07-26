package com.foreach.imageserver.services.transformers;

import com.foreach.imageserver.services.ImageTestData;
import org.junit.Test;

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
	}

	@Test
	public void dimensionsCalculationFailures() {
		dimensions( ImageTestData.CMYK_COLOR, ImageTransformerPriority.PREFERRED, false );
	}

	@Test
	public void cantCalculateDimensions() {
		dimensions( ImageTestData.KAAIMAN_SVG, ImageTransformerPriority.UNABLE, false );
		dimensions( ImageTestData.KAAIMAN_EPS, ImageTransformerPriority.UNABLE, false );
		dimensions( ImageTestData.TEST_EPS, ImageTransformerPriority.UNABLE, false );
		dimensions( ImageTestData.BRUXELLES_EPS, ImageTransformerPriority.UNABLE, false );
		dimensions( ImageTestData.BRUXELLES_ECHO_EPS, ImageTransformerPriority.UNABLE, false );
		dimensions( ImageTestData.SINGLE_PAGE_PDF, ImageTransformerPriority.UNABLE, false );
		dimensions( ImageTestData.MULTI_PAGE_PDF, ImageTransformerPriority.UNABLE, false );
	}
}
