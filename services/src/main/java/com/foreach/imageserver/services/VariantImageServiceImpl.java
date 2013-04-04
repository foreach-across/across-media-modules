package com.foreach.imageserver.services;

import com.foreach.imageserver.business.image.VariantImage;
import com.foreach.imageserver.dao.VariantImageDao;
import com.foreach.imageserver.dao.selectors.VariantImageSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VariantImageServiceImpl implements VariantImageService{

    @Autowired
    private VariantImageDao variantImageDao;

    public final VariantImage getVariantImageById(long id) {
        return variantImageDao.getVariantImageById(id);
    }

    public final List<VariantImage> getAllVariantsForImage(long imageId) {
        return variantImageDao.getAllVariantsForImage(imageId);
    }

    public final void saveVariantImage(VariantImage variantImage) {
		if ( variantImage.getId() > 0 ) {
			variantImageDao.updateVariantImage(variantImage);
		} else {
			variantImageDao.insertVariantImage(variantImage);
		}
    }

    public final void deleteVariantImage(VariantImage variantImage) {
        variantImageDao.deleteVariantImage( variantImage );
    }

    public final VariantImage getVariantImage(VariantImageSelector selector) {
        return variantImageDao.getVariantImage(selector);
    }

    public final List<VariantImage> getVariantImages(VariantImageSelector selector) {
        return variantImageDao.getVariantImages(selector);
    }
}
