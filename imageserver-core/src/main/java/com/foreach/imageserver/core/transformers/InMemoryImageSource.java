package com.foreach.imageserver.core.transformers;

import com.foreach.imageserver.core.business.ImageType;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class InMemoryImageSource implements ImageSource
{
	private final ImageType imageType;
	private final byte[] imageBytes;

	public InMemoryImageSource( ImageType imageType, byte[] imageBytes ) {
		this.imageType = imageType;
		this.imageBytes = imageBytes;
	}

	@Override
	public ImageType getImageType() {
		return imageType;
	}

	public byte[] getImageBytes() {
		return imageBytes;
	}

	@Deprecated
	public StreamImageSource stream() {
		return new StreamImageSource( imageType, new ByteArrayInputStream( imageBytes ) );
	}

	@Override
	public InputStream getImageStream() {
		return new ByteArrayInputStream( imageBytes );
	}
}
