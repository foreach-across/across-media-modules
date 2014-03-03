package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageFile;
import com.foreach.imageserver.core.business.ImageModifier;
import com.foreach.imageserver.core.services.repositories.RepositoryLookupResult;

public interface ImageService {
    Image getImageByKey(String key, int applicationId);

    void save(Image image, RepositoryLookupResult lookupResult);

    ImageFile fetchImageFile(Image image, ImageModifier modifier);

    void registerModification(Image image, Dimensions dimensions, ImageModifier modifier);

    void delete(Image image, boolean variantsOnly);
}
