package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.business.ImageProfileModification;

public interface ImageProfileManager {
    ImageProfileModification getModification(int imageProfileId, int contextId, int resolutionId);
}
