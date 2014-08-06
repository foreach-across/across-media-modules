package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.ImageProfile;
import com.foreach.imageserver.dto.ImageProfileDto;

public interface ImageProfileService
{
	ImageProfile getById( long id );

	void save( ImageProfileDto imageProfileDto );
}
