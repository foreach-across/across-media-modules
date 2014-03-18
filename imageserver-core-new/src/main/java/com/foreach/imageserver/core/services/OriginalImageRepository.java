package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.ImageParameters;

import java.util.Map;

public interface OriginalImageRepository {
    String getRepositoryCode();

    ImageParameters getOriginalImage(int id);

    ImageParameters getOriginalImage(Map<String, String> repositoryParameters);

    RetrievedOriginalImage insertAndRetrieveOriginalImage(Map<String, String> repositoryParameters);

    boolean parametersAreEqual(int originalImageId, Map<String, String> repositoryParameters);
}
