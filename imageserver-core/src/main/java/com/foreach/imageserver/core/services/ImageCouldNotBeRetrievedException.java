package com.foreach.imageserver.core.services;

public class ImageCouldNotBeRetrievedException extends RuntimeException
{
	public ImageCouldNotBeRetrievedException( String cause ) {
		super( cause );
	}

	public ImageCouldNotBeRetrievedException( Throwable cause ) {
		super( cause );
	}
}
