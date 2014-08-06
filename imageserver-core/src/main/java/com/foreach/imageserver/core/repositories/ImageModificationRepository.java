package com.foreach.imageserver.core.repositories;

import com.foreach.imageserver.core.business.ImageModification;

import java.util.List;

public interface ImageModificationRepository extends BasicRepository<ImageModification>
{
	ImageModification getById( long imageId, long contextId, long imageResolutionId );

	List<ImageModification> getModifications( long imageId, long contextId );

	List<ImageModification> getAllModifications( long imageId );

	boolean hasModification( long imageId );
}
