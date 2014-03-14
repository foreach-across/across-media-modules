package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.OriginalImage;

import java.io.InputStream;

public interface ImageStoreService {
    void storeOriginalImage(OriginalImage originalImage, byte[] imageBytes);

    void storeOriginalImage(OriginalImage originalImage, InputStream imageStream);
}
