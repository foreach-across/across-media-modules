package com.foreach.imageserver.services.transformers;

import com.foreach.imageserver.business.ImageFile;
import com.foreach.imageserver.business.ImageModifier;
import com.foreach.imageserver.business.ImageType;
import com.foreach.imageserver.services.ImageTestData;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

				LOG.debug( "Dimensions calculated for {} in {} ms", image.getResourcePath(),
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

	protected void modify(
			ImageTestData image,
			ImageModifier modifier,
			ImageTransformerPriority expectedPriority,
			boolean shouldSucceed ) {
		long start = System.currentTimeMillis();

		ImageModifyAction action = new ImageModifyAction( image.getImageFile(), modifier );

		ImageTransformerPriority priority = transformer.canExecute( action );
		assertEquals( "Wrong priority for image modification " + modifier + " for " + image.getResourcePath(),
		              expectedPriority, priority );

		if ( priority != ImageTransformerPriority.UNABLE ) {
			boolean succeeded = true;
			try {
				transformer.execute( action );

				ImageFile modified = action.getResult();
				assertNotNull( modified );

				LOG.debug( "Applied modification {} to {} in {} ms", modifier, image.getResourcePath(),
				           ( System.currentTimeMillis() - start ) );

				IOUtils.copy( modified.openContentStream(), new FileOutputStream(
						"/temp/images_generated/" + image.name() + "." + transformer.getName() + "." + modifier.getOutput().getExtension() ) );
			}
			catch ( Exception e ) {
				if ( shouldSucceed ) {
					LOG.error( "Exception applying modification " + modifier + " to " + image.getResourcePath(), e );
				}
				succeeded = false;
			}

			assertEquals( "Unexpected modification outcome for " + modifier + " on " + image.getResourcePath(),
			              shouldSucceed, succeeded );
		}
	}

	protected ImageModifier scale( ImageTestData image, float delta, ImageType output ) {
		ImageModifier mod = new ImageModifier();
		mod.setOutput( output );
		mod.setWidth( Math.round( image.getDimensions().getWidth() * delta ) );
		mod.setHeight( Math.round( image.getDimensions().getHeight() * delta ) );

		return mod;
	}

	protected abstract ImageTransformer createTransformer();
}
