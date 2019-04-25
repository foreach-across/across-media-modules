package com.foreach.imageserver.client;

public class ImageServerException extends RuntimeException
{
	public ImageServerException( String cause ) {
		super( cause );
	}

	public ImageServerException( Throwable cause ) {
		super( cause );
	}
}
