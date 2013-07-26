package com.foreach.imageserver.services;

import com.foreach.imageserver.business.Dimensions;
import com.foreach.imageserver.business.ImageFile;
import com.foreach.imageserver.business.ImageType;

import java.io.InputStream;

public enum ImageTestData
{
	SUNSET( "/images/sunset.jpg", "image/jpeg", ImageType.JPEG, 1083349L, 1420, 930 ),
	EARTH( "/images/earth_large.jpg", "image/jpeg", ImageType.JPEG, 11803928L, 9104, 6828 ),
	HIGH_RES( "/images/high_res_conversion.jpeg", "image/jpeg", ImageType.JPEG, 4569331L, 3072, 2048 ),
	CMYK_COLOR( "/images/CMYK_Error_COLOR_ID.jpg", "image/jpeg", ImageType.JPEG, 192947L, 679, 602 ),
	ICE_ROCK( "/images/icerock.jpeg", "image/jpeg", ImageType.JPEG, 138605L, 1600, 1200 ),
	KAAIMAN_JPEG( "/images/kaaimangrootkleur.jpg", "image/jpeg", ImageType.JPEG, 166746L, 900, 900 ),
	KAAIMAN_GIF( "/images/kaaimangrootkleur.gif", "image/gif", ImageType.GIF, 152568L, 900, 900 ),
	KAAIMAN_PNG( "/images/kaaimangrootkleur.png", "image/png", ImageType.PNG, 436783L, 900, 900 ),
	KAAIMAN_SVG( "/images/kaaimangrootkleur.svg", "image/svg+xml", ImageType.SVG, 327030L, 1125, 1125 ),
	KAAIMAN_EPS( "/images/kaaimangrootkleur.eps", "image/x-eps", ImageType.EPS, 410057L, 7500, 7500 ),
	TEST_PNG( "/images/test.png", "image/png", ImageType.PNG, 163L, 100, 55 ),
	TEST_EPS( "/images/test.eps", "application/eps", ImageType.EPS, 1521754L, 2675, 4858 ),
	TEST_TRANSPARENT_PNG( "/images/test.transparent.png", "image/png", ImageType.PNG, 145620L, 800, 415 ),
	BRUXELLES_EPS( "/images/BRUXELLES_RGB.eps", "image/eps", ImageType.EPS, 89228L, 1083, 867 ),
	BRUXELLES_ECHO_EPS( "/images/BRUXELLES_ECHO.eps", "application/postscript", ImageType.EPS, 838966L, 1083, 867 ),
	SINGLE_PAGE_PDF( "/images/test_singlepage.pdf", "application/pdf", ImageType.PDF, 965622L, 1469, 1016 ),
	MULTI_PAGE_PDF( "/images/test_multipage.pdf", "application/x-pdf", ImageType.PDF, 7013693L, 893, 1332 );

	private final String resourcePath, contentType;
	private final ImageType imageType;
	private final long fileSize;
	private final Dimensions dimensions;

	ImageTestData( String resourcePath,
	               String contentType,
	               ImageType imageType,
	               long fileSize,
	               int width,
	               int height ) {
		this.resourcePath = resourcePath;
		this.contentType = contentType;
		this.imageType = imageType;
		this.fileSize = fileSize;
		this.dimensions = new Dimensions( width, height );
	}

	public String getContentType() {
		return contentType;
	}

	public ImageType getImageType() {
		return imageType;
	}

	public String getResourcePath() {
		return resourcePath;
	}

	public InputStream getResourceAsStream() {
		return getClass().getResourceAsStream( getResourcePath() );
	}

	public long getFileSize() {
		return fileSize;
	}

	public Dimensions getDimensions() {
		return dimensions;
	}

	public ImageFile getImageFile() {
		return new ImageFile( imageType, fileSize, getResourceAsStream() );
	}
}
