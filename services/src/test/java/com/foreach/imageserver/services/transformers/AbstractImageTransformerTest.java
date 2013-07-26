package com.foreach.imageserver.services.transformers;

import com.foreach.imageserver.services.ImageTestData;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public abstract class AbstractImageTransformerTest
{
	protected Logger LOG;
	protected ImageTransformer transformer;

	@Before
	public void setup() {
		LOG = LoggerFactory.getLogger( getClass() );
		transformer = createTransformer();
	}

	protected void dimensions( ImageTestData image, ImageTransformerPriority expectedPriority, boolean shouldSucceed ) {
		long start = System.currentTimeMillis();

		ImageCalculateDimensionsAction action = new ImageCalculateDimensionsAction( image.getImageFile() );

		ImageTransformerPriority priority = transformer.canExecute( action );
		assertEquals( "Wrong priority for calculating dimensions for " + image.getResourcePath(), expectedPriority,
		              priority );

		if ( priority != ImageTransformerPriority.UNABLE ) {
			boolean succeeded = true;
			try {
				transformer.execute( action );
				assertEquals( "Calculated dimensions for " + image.getResourcePath() + " are wrong",
				              image.getDimensions(), action.getResult() );

				LOG.debug( "Dimensions calculated for {} in {}ms", image.getResourcePath(),
				           ( System.currentTimeMillis() - start ) );
			}
			catch ( Exception e ) {
				if ( shouldSucceed ) {
					LOG.error( "Exception calculating dimensions for " + image.getResourcePath(), e );
				}
				succeeded = false;
			}

			assertEquals( "Unexpected outcome of calculating dimensions for " + image.getResourcePath(), shouldSucceed,
			              succeeded );
		}
	}

	protected abstract ImageTransformer createTransformer();
}
