package com.foreach.imageserver.core.transformers;

import java.io.InputStream;

public class GetImageAttributesAction
{
	private final InputStream imageStream;

	public GetImageAttributesAction( InputStream imageStream ) {
		this.imageStream = imageStream;
	}

	public InputStream getImageStream() {
		return imageStream;
	}
}
