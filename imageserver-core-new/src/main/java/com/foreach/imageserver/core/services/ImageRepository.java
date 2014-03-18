package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.ImageParameters;

import java.util.Map;

public interface ImageRepository {
    String getCode();

    ImageParameters getImageParameters(int id);

    RetrievedImage retrieveImage(int imageId, Map<String, String> repositoryParameters);

    boolean parametersAreEqual(int imageId, Map<String, String> repositoryParameters);
}
