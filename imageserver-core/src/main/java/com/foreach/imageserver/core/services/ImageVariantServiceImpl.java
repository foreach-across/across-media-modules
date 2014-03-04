package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.data.ImageVariantDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class ImageVariantServiceImpl implements ImageVariantService {

    @Autowired
    private ImageVariantDao variantDao;

    @Transactional
    @Override
    public void registerVariant(Image image, ImageVariant variant) {
        StoredImageVariant modification = variantDao.getVariant(image.getId(), variant.getModifier());
        if (modification == null) {
            modification = new StoredImageVariant();
            modification.setImageId(image.getId());
            modification.setVariant(variant);
            variantDao.insertVariant(modification);
        } else {
            modification.setVariant(variant);
            variantDao.updateVariant(modification);
        }
    }

    @Override
    public Crop getCropForModifier(Image image, ImageModifier modifier) {
        StoredImageVariant modification = variantDao.getVariant(image.getId(), modifier);
        if (modification != null) {
            return modification.getVariant().getCrop();
        }
        //No crop registered. Re-use the crop of 'closest' modifier for this image
        List<StoredImageVariant> modifications = new ArrayList<>(variantDao.getVariantsForImage(image.getId()));
        Collections.sort(modifications, new DistanceToModifier(modifier));
        for (StoredImageVariant modification1 : modifications) {
            return modification1.getVariant().getCrop();
        }
        //No modifiers for this image. Use entire image as crop
        return new Crop(0, 0, image.getDimensions().getWidth(), image.getDimensions().getHeight());
    }

    private class DistanceToModifier implements Comparator<StoredImageVariant> {
        public DistanceToModifier(ImageModifier modifier) {
        }

        @Override
        public int compare(StoredImageVariant o1, StoredImageVariant o2) {
            //TODO!
            return 0;
        }
    }
}
