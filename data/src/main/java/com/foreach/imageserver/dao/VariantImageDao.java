package com.foreach.imageserver.dao;

import com.foreach.imageserver.business.image.VariantImage;
import com.foreach.imageserver.dao.selectors.VariantImageSelector;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VariantImageDao {

    VariantImage getVariantImageById( long id );

    List<VariantImage> getAllVariantsForImage( long imageId );

    void insertVariantImage( VariantImage variantImage );

    void updateVariantImage( VariantImage variantImage );

    void updateVariantImageDate( VariantImage variantImage );

    void deleteVariantImage( VariantImage variantImage );

    VariantImage getVariantImage(VariantImageSelector selector);

    List<VariantImage> getVariantImages(VariantImageSelector selector);

    void deleteVariantImages(VariantImageSelector selector);
}
