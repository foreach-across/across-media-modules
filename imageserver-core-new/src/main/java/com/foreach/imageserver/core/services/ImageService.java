package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageModification;

import java.util.Map;

public interface ImageService {
    Image getById(int applicationId, int imageId);

    void saveImage(int applicationId, int imageId, OriginalImageRepository imageRepository, Map<String, String> repositoryParameters);

    void saveImageModification(ImageModification modification);
}
