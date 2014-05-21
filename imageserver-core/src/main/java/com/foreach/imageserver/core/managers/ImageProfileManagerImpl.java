package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.business.ImageProfileModification;
import com.foreach.imageserver.core.data.ImageProfileDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ImageProfileManagerImpl implements ImageProfileManager {

    @Autowired
    private ImageProfileDao imageProfileDao;

    @Override
    public ImageProfileModification getModification(int imageProfileId, int contextId, int resolutionId) {
        return imageProfileDao.getModification(imageProfileId, contextId, resolutionId);
    }
}
