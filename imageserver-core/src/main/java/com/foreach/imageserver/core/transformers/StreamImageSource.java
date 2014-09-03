package com.foreach.imageserver.core.transformers;

import com.foreach.imageserver.core.business.ImageType;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class StreamImageSource
{
	private final ImageType imageType;
	private final InputStream imageStream;

	public StreamImageSource( ImageType imageType, InputStream imageStream ) {
		this.imageType = imageType;
		this.imageStream = imageStream;
	}

	public StreamImageSource( ImageType imageType, byte[] imageBytes ) {
		this.imageType = imageType;
		this.imageStream = new ByteArrayInputStream( imageBytes );
	}

	public ImageType getImageType() {
		return imageType;
	}

	public InputStream getImageStream() {
		return imageStream;
	}
}
