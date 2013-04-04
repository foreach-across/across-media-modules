package com.foreach.imageserver.services;

import com.foreach.imageserver.business.image.VariantImage;
import com.foreach.imageserver.dao.selectors.VariantImageSelector;

import java.util.List;

public interface VariantImageService {
    VariantImage getVariantImageById( long id );

    List<VariantImage> getAllVariantsForImage( long imageId);

    void saveVariantImage( VariantImage variantImage );

    void deleteVariantImage( VariantImage variantImage );

    VariantImage getVariantImage(VariantImageSelector selector);

    List<VariantImage> getVariantImages(VariantImageSelector selector);
}
