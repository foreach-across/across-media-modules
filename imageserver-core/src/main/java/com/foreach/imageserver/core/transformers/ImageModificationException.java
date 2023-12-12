package com.foreach.imageserver.core.transformers;

public class ImageModificationException extends RuntimeException
{
	public ImageModificationException() {
	}

	public ImageModificationException( String message ) {
		super( message );
	}

	public ImageModificationException( String message, Throwable cause ) {
		super( message, cause );
	}

	public ImageModificationException( Throwable cause ) {
		super( cause );
	}
}
