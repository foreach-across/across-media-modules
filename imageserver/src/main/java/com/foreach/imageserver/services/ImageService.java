package com.foreach.imageserver.services;

import com.foreach.imageserver.business.Dimensions;
import com.foreach.imageserver.business.Image;
import com.foreach.imageserver.business.ImageFile;
import com.foreach.imageserver.business.ImageModifier;
import com.foreach.imageserver.services.repositories.RepositoryLookupResult;

public interface ImageService
{
	Image getImageByKey( String key, int applicationId );

	void save( Image image, RepositoryLookupResult lookupResult );

	ImageFile fetchImageFile( Image image, ImageModifier modifier );

	void registerModification( Image image, Dimensions dimensions, ImageModifier modifier );

	void delete( Image image, boolean variantsOnly );
}
