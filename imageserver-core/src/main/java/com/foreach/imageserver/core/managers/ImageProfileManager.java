package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.business.ImageProfileModification;

public interface ImageProfileManager
{
	ImageProfileModification getModification( long imageProfileId, long contextId, long resolutionId );
}
