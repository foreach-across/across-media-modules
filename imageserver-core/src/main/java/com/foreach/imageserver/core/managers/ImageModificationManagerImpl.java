package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.business.ImageModification;
import com.foreach.imageserver.core.repositories.ImageModificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * ImageModification objects are not cached by design; the strategies for synchronizing between different imageserver
 * instances rely on this. For this reason, one should take care not to request ImageModification objects during
 * 'normal operation'.
 */
@Repository
public class ImageModificationManagerImpl implements ImageModificationManager
{
	@Autowired
	private ImageModificationRepository imageModificationRepository;

	@Override
	// Not cached -- see comments above.
	public ImageModification getById( long imageId, long contextId, long imageResolutionId ) {
		return imageModificationRepository.getById( imageId, contextId, imageResolutionId );
	}

	@Override
	// Not cached -- see comments above.
	public List<ImageModification> getModifications( long imageId, long contextId ) {
		return Collections.unmodifiableList( imageModificationRepository.getModifications( imageId, contextId ) );
	}

	@Override
	// Not cached -- see comments above.
	public List<ImageModification> getAllModifications( long imageId ) {
		return Collections.unmodifiableList( imageModificationRepository.getAllModifications( imageId ) );
	}

	@Override
	public void insert( ImageModification imageModification ) {
		imageModificationRepository.create( imageModification );
	}

	@Override
	public void update( ImageModification imageModification ) {
		imageModificationRepository.update( imageModification );
	}

	@Override
	// Not cached -- see comments above.
	public boolean hasModification( long imageId ) {
		return imageModificationRepository.hasModification( imageId );
	}

}
