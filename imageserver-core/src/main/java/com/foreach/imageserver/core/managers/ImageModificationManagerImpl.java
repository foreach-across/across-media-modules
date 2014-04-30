package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.business.ImageModification;
import com.foreach.imageserver.core.data.ImageModificationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ImageModification objects are not cached by design; the strategies for synchronizing between different imageserver
 * instances rely on this. For this reason, one should take care not to request ImageModification objects during
 * 'normal operation'.
 */
@Repository
public class ImageModificationManagerImpl implements ImageModificationManager {

    @Autowired
    private ImageModificationDao imageModificationDao;

    @Override
    // Not cached -- see comments above.
    public ImageModification getById(int imageId, int contextId, int imageResolutionId) {
        return imageModificationDao.getById(imageId, contextId, imageResolutionId);
    }

    @Override
    // Not cached -- see comments above.
    public List<ImageModification> getModifications(int imageId, int contextId) {
        return imageModificationDao.getModifications(imageId, contextId);
    }

    @Override
    // Not cached -- see comments above.
    public List<ImageModification> getAllModifications(int imageId) {
        return imageModificationDao.getAllModifications(imageId);
    }

    @Override
    public void insert(ImageModification imageModification) {
        imageModificationDao.insert(imageModification);
    }

    @Override
    public void update(ImageModification imageModification) {
        imageModificationDao.update(imageModification);
    }

    @Override
    // Not cached -- see comments above.
    public boolean hasModification(int imageId) {
        return imageModificationDao.hasModification(imageId);
    }

}
