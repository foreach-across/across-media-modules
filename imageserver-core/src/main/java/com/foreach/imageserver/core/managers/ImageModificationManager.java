package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.business.ImageModification;

import java.util.List;

public interface ImageModificationManager {
    ImageModification getById(int imageId, int contextId, int imageResolutionId);

    List<ImageModification> getModifications(int imageId, int contextId);

    List<ImageModification> getAllModifications(int imageId);

    void insert(ImageModification imageModification);

    void update(ImageModification imageModification);

    boolean hasModification(int imageId);
}
