package com.foreach.imageserver.core.utils;

import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class ImageUtils
{

	public static BufferedImage bufferedImage( byte[] imageBytes ) throws IOException {
		BufferedImage image = null;

		ByteArrayInputStream imageStream = new ByteArrayInputStream( imageBytes );
		try {
			image = ImageIO.read( imageStream );
		}
		finally {
			IOUtils.closeQuietly( imageStream );
		}

		return image;
	}

	public static BufferedImage bufferedImage( InputStream imageStream ) throws IOException {
		BufferedImage image = null;

		try {
			image = ImageIO.read( imageStream );
		}
		finally {
			IOUtils.closeQuietly( imageStream );
		}

		return image;
	}

	public static BufferedImage bufferedImageFromClassPath( String classPath ) throws IOException {
		BufferedImage image = null;

		InputStream resourceStream = ImageUtils.class.getClassLoader().getResourceAsStream( classPath );
		if ( resourceStream != null ) {
			try {
				image = ImageIO.read( resourceStream );
			}
			finally {
				IOUtils.closeQuietly( resourceStream );
			}
		}

		return image;
	}

	public static boolean imagesAreEqual( BufferedImage image1, BufferedImage image2 ) throws IOException {
		if ( image1.getHeight() != image2.getHeight() ) {
			return false;
		}

		if ( image1.getWidth() != image2.getWidth() ) {
			return false;
		}

		double[] imageData1 =
				image1.getData().getPixels( 0, 0, image1.getWidth(), image1.getHeight(), (double[]) null );
		double[] imageData2 =
				image2.getData().getPixels( 0, 0, image2.getWidth(), image2.getHeight(), (double[]) null );

		return Arrays.equals( imageData1, imageData2 );
	}

}
