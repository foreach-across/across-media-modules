package com.foreach.imageserver.core.services;


import com.foreach.imageserver.core.business.Crop;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageModification;
import com.foreach.imageserver.core.business.ImageVariant;

public interface ImageVariantService {

    void registerVariant(Image image, ImageModification variant);

    Crop getCropForModifier(Image image, ImageVariant modifier);

}
