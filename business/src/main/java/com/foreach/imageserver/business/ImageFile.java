package com.foreach.imageserver.business;

import java.io.InputStream;

public class ImageFile
{
	private final ImageType imageType;
	private final long fileSize;
	private final InputStream content;

	public ImageFile( ImageType imageType, long fileSize, InputStream content ) {
		this.imageType = imageType;
		this.fileSize = fileSize;
		this.content = content;
	}

	public ImageType getImageType() {
		return imageType;
	}

	public long getFileSize() {
		return fileSize;
	}

	public InputStream getContent() {
		return content;
	}
}
