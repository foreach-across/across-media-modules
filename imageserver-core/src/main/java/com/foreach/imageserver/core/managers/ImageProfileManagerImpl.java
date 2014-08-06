package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.business.ImageProfileModification;
import com.foreach.imageserver.core.repositories.ImageProfileModificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ImageProfileManagerImpl implements ImageProfileManager
{
	@Autowired
	private ImageProfileModificationRepository imageProfileModificationRepository;

	@Override
	public ImageProfileModification getModification( long imageProfileId, long contextId, long resolutionId ) {
		return imageProfileModificationRepository.getModification( imageProfileId, contextId, resolutionId );
	}
}
