package com.foreach.imageserver.services.repositories;

public interface ImageLookupRepository
{
	RepositoryLookupResult fetchImage( String uri );
}
