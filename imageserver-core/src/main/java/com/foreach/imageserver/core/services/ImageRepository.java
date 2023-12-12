package com.foreach.imageserver.core.services;

import java.util.Map;

public interface ImageRepository
{
	String getCode();

	RetrievedImage retrieveImage( int imageId, Map<String, String> repositoryParameters );

	byte[] retrieveImage( int imageId );
}
