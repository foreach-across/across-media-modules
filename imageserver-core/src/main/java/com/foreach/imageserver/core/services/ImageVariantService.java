package com.foreach.imageserver.core.services;


import com.foreach.imageserver.core.business.Crop;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageVariant;
import com.foreach.imageserver.core.business.ImageModifier;

public interface ImageVariantService {

    void registerVariant(Image image, ImageVariant variant);

    Crop getCropForModifier(Image image, ImageModifier modifier);

}
