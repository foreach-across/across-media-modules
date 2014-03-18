package com.foreach.imageserver.core.business;

public interface ImageParameters {
    int getId();

    String getRepositoryCode();

    ImageType getImageType();

    Dimensions getDimensions();

    String getUniqueFileName();
}
