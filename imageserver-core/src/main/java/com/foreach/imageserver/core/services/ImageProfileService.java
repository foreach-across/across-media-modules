package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.ImageProfile;
import com.foreach.imageserver.dto.ImageProfileDto;

import java.util.Optional;

public interface ImageProfileService
{
	ImageProfile getDefaultProfile();

	Optional<ImageProfile> getById( long id );

	void save( ImageProfileDto imageProfileDto );
}
