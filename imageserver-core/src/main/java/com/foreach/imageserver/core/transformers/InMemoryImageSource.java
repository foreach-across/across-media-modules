package com.foreach.imageserver.core.transformers;

import com.foreach.imageserver.core.business.ImageType;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class InMemoryImageSource
{
	private final ImageType imageType;
	private final byte[] imageBytes;

	public InMemoryImageSource( ImageType imageType, byte[] imageBytes ) {
		this.imageType = imageType;
		this.imageBytes = imageBytes;
	}

	public ImageType getImageType() {
		return imageType;
	}

	public byte[] getImageBytes() {
		return imageBytes;
	}

	public StreamImageSource stream() {
		return new StreamImageSource( imageType, new ByteArrayInputStream( imageBytes ) );
	}

	public InputStream byteStream() {
		return new ByteArrayInputStream( imageBytes );
	}
}
