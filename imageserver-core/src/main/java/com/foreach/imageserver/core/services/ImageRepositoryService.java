package com.foreach.imageserver.core.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ImageRepositoryService
{
	Logger LOG = LoggerFactory.getLogger( ImageRepositoryService.class );

	ImageRepository determineImageRepository( String code );
}
