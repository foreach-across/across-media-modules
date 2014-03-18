package com.foreach.imageserver.core.business;

public interface ImageParameters {
    int getImageId();

    String getRepositoryCode();

    ImageType getImageType();

    Dimensions getDimensions();

    String getUniqueFileName();
}
