package com.foreach.imageserver.services.repositories;

public interface ImageLookupRepository
{
	boolean isValidURI( String uri );

	RepositoryLookupResult fetchImage( String uri );
}
