package com.foreach.imageserver.services.transformers;

import com.foreach.imageserver.business.ImageFile;
import com.foreach.imageserver.services.ImageTestData;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestPureJavaImageTransformer
{
	private PureJavaImageTransformer transformer = new PureJavaImageTransformer();

	@Test
	public void calculateDimensions() {
		ImageTestData testImage = ImageTestData.EARTH;

		ImageFile imageFile =
				new ImageFile( testImage.getImageType(), testImage.getFileSize(), testImage.getResourceAsStream() );
		ImageCalculateDimensionsAction action = new ImageCalculateDimensionsAction( imageFile );

		transformer.execute( action );

		assertEquals( testImage.getDimensions(), action.getResult() );
	}
}
