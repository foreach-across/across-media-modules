package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.business.ImageModification;

import java.util.List;

public interface ImageModificationManager
{
	ImageModification getById( long imageId, long contextId, long imageResolutionId );

	List<ImageModification> getModifications( long imageId, long contextId );

	List<ImageModification> getAllModifications( long imageId );

	void insert( ImageModification imageModification );

	void update( ImageModification imageModification );

	boolean hasModification( long imageId );
}
