package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Image;

import java.util.Map;

public interface ImageService {
    Image getById(int applicationId, int imageId);

    void saveImage(int applicationId, int imageId, OriginalImageRepository imageRepository, Map<String, String> repositoryParameters);
}
