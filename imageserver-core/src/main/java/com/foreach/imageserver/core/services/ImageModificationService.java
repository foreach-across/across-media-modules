package com.foreach.imageserver.core.services;


import com.foreach.imageserver.core.business.*;

import java.util.List;

public interface ImageModificationService {

    void saveModification(Image image, ImageModification modification);

    List<StoredImageModification> getModificationsForImage(Image image);

    Crop getCropForVariant(Image image, ImageVariant variant);

}
