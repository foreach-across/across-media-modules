package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.data.StoredImageModificationDao;
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
    private StoredImageModificationDao variantDao;

    @Transactional
    @Override
    public void registerVariant(Image image, ImageModification variant) {
        StoredImageModification modification = variantDao.getModification(image.getId(), variant.getVariant());
        if (modification == null) {
            modification = new StoredImageModification();
            modification.setImageId(image.getId());
            modification.setModification(variant);
            variantDao.insertModification(modification);
        } else {
            modification.setModification(variant);
            variantDao.updateModification(modification);
        }
    }

    @Override
    public Crop getCropForModifier(Image image, ImageVariant modifier) {
        StoredImageModification modification = variantDao.getModification(image.getId(), modifier);
        if (modification != null) {
            return modification.getModification().getCrop();
        }
        //No crop registered. Re-use the crop of 'closest' modifier for this image
        List<StoredImageModification> modifications = new ArrayList<>(variantDao.getModificationsForImage(image.getId()));
        Collections.sort(modifications, new DistanceToModifier(modifier));
        for (StoredImageModification modification1 : modifications) {
            return modification1.getModification().getCrop();
        }
        //No modifiers for this image. Use entire image as crop
        return new Crop(0, 0, image.getDimensions().getWidth(), image.getDimensions().getHeight());
    }

    private class DistanceToModifier implements Comparator<StoredImageModification> {
        public DistanceToModifier(ImageVariant modifier) {
        }

        @Override
        public int compare(StoredImageModification o1, StoredImageModification o2) {
            //TODO!
            return 0;
        }
    }
}
