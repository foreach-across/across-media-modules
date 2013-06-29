package com.foreach.imageserver.services;

import com.foreach.imageserver.business.Image;
import com.foreach.imageserver.business.image.ServableImageData;
import com.foreach.imageserver.dao.selectors.ImageSelector;
import com.foreach.imageserver.services.repositories.RepositoryLookupResult;

import java.util.List;

public interface ImageService
{
	Image getImageByKey( String key, int applicationId );

	@Deprecated
	ServableImageData getImageById( long id );

	@Deprecated
	ServableImageData getImageByPath( ImageSelector selector );

	@Deprecated
	List<ServableImageData> getAllImages();

	@Deprecated
	int getImageCount( ImageSelector selector );

	@Deprecated
	long saveImage( ServableImageData image );

	@Deprecated
	long saveImage( ServableImageData image, boolean deleteCrops );

	@Deprecated
	List<ServableImageData> getImages( ImageSelector selector );

	@Deprecated
	void save( Image image, RepositoryLookupResult lookupResult );
}
