package com.foreach.imageserver.core.services.exceptions;

public class ImageResolutionException extends RuntimeException
{
	public ImageResolutionException( String cause ) {
		super( cause );
	}

	public ImageResolutionException( Throwable cause ) {
		super( cause );
	}
}
