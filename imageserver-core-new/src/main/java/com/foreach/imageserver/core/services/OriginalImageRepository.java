package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.OriginalImage;

import java.util.Map;

public interface OriginalImageRepository {
    String getRepositoryCode();

    OriginalImage getOriginalImage(int id);

    OriginalImage getOriginalImage(Map<String, String> repositoryParameters);

    RetrievedOriginalImage insertAndRetrieveOriginalImage(Map<String, String> repositoryParameters);

    boolean parametersAreEqual(int originalImageId, Map<String, String> repositoryParameters);
}
