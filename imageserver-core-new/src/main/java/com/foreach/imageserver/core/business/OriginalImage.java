package com.foreach.imageserver.core.business;

public interface OriginalImage {
    int getId();

    String getRepositoryCode();

    ImageType getImageType();

    Dimensions getDimensions();

    String getUniqueFileName();
}
