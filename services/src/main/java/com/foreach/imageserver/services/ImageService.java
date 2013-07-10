package com.foreach.imageserver.services;

import com.foreach.imageserver.business.Image;
import com.foreach.imageserver.business.ImageFile;
import com.foreach.imageserver.business.ImageModifier;
import com.foreach.imageserver.business.image.ServableImageData;
import com.foreach.imageserver.dao.selectors.ImageSelector;
import com.foreach.imageserver.services.repositories.RepositoryLookupResult;

import java.util.List;

public interface ImageService
{
	Image getImageByKey( String key, int applicationId );

	void save( Image image, RepositoryLookupResult lookupResult );

	ImageFile fetchImageFile( Image image, ImageModifier modifier );

	void delete( Image image );
}
