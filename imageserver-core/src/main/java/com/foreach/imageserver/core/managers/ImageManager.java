package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.business.Image;

public interface ImageManager {
    Image getById(int imageId);

    void insert(Image image);

    void updateParameters(Image image);
}
