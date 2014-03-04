package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageFile;
import com.foreach.imageserver.core.business.ImageVariant;
import com.foreach.imageserver.core.services.repositories.RepositoryLookupResult;

public interface ImageService {
    Image getImageByKey(String key, int applicationId);

    void save(Image image, RepositoryLookupResult lookupResult);

    ImageFile fetchImageFile(Image image, ImageVariant modifier);

    void delete(Image image, boolean variantsOnly);
}
