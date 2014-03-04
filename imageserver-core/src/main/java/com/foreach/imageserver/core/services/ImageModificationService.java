package com.foreach.imageserver.core.services;


import com.foreach.imageserver.core.business.Crop;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageModification;
import com.foreach.imageserver.core.business.ImageVariant;

public interface ImageModificationService {

    void saveModification(Image image, ImageModification modification);

    Crop getCropForVariant(Image image, ImageVariant variant);

}
