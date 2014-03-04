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
public class ImageModificationServiceImpl implements ImageModificationService {

    @Autowired
    private StoredImageModificationDao variantDao;

    @Transactional
    @Override
    public void saveModification(Image image, ImageModification modification) {
        StoredImageModification storedModification = variantDao.getModification(image.getId(), modification.getVariant());
        if (storedModification == null) {
            storedModification = new StoredImageModification();
            storedModification.setImageId(image.getId());
            storedModification.setModification(modification);
            variantDao.insertModification(storedModification);
        } else {
            storedModification.setModification(modification);
            variantDao.updateModification(storedModification);
        }
    }

    @Override
    public Crop getCropForVariant(Image image, ImageVariant variant) {
        StoredImageModification modification = variantDao.getModification(image.getId(), variant);
        if (modification != null) {
            return modification.getModification().getCrop();
        }
        //No crop registered. Re-use the crop of 'closest' variant for this image
        List<StoredImageModification> modifications = new ArrayList<>(variantDao.getModificationsForImage(image.getId()));
        Collections.sort(modifications, new DistanceToVariant(variant));
        for (StoredImageModification modification1 : modifications) {
            return modification1.getModification().getCrop();
        }
        //No modifications for this image. Use entire image as crop
        return new Crop(0, 0, image.getDimensions().getWidth(), image.getDimensions().getHeight());
    }

    private class DistanceToVariant implements Comparator<StoredImageModification> {
        public DistanceToVariant(ImageVariant variant) {
        }

        @Override
        public int compare(StoredImageModification o1, StoredImageModification o2) {
            //TODO!
            return 0;
        }
    }
}
