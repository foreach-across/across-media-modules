package com.foreach.imageserver.admin.service;

import com.foreach.imageserver.business.taxonomy.Group;
import com.foreach.imageserver.business.image.ServableImageData;

public interface ImageServerFacade
{
	ServableImageData getImageData( long imageId );

	Group getImageGroup( int groupId );
}
