package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.business.ImageProfileModification;
import com.foreach.imageserver.core.repositories.ImageProfileModificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ImageProfileManagerImpl implements ImageProfileManager
{
	private final ImageProfileModificationRepository imageProfileModificationRepository;

	@Override
	public ImageProfileModification getModification( long imageProfileId, long contextId, long resolutionId ) {
		return imageProfileModificationRepository.getModification( imageProfileId, contextId, resolutionId );
	}
}
