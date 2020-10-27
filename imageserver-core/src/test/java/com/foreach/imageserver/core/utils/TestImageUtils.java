package com.foreach.imageserver.core.utils;

import org.junit.jupiter.api.Test;
import support.ImageUtils;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestImageUtils
{

	@Test
	public void sameImageContent() throws Exception {
		BufferedImage bufferedImage1 = ImageUtils.bufferedImageFromClassPath( "images/baseImage.jpg" );
		BufferedImage bufferedImage2 = ImageUtils.bufferedImageFromClassPath( "images/differentHeader.jpg" );

		assertTrue( ImageUtils.imagesAreEqual( bufferedImage1, bufferedImage2 ) );
	}

	@Test
	public void differentImageContent() throws Exception {
		BufferedImage bufferedImage1 = ImageUtils.bufferedImageFromClassPath( "images/baseImage.jpg" );
		BufferedImage bufferedImage2 = ImageUtils.bufferedImageFromClassPath( "images/onePixelDifferent.jpg" );

		assertFalse( ImageUtils.imagesAreEqual( bufferedImage1, bufferedImage2 ) );
	}

}
