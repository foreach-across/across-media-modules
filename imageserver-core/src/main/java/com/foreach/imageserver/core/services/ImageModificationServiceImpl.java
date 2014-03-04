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
    private StoredImageModificationDao modificationDao;

    @Transactional
    @Override
    public void saveModification(Image image, ImageModification modification) {
        StoredImageModification storedModification = modificationDao.getModification(image.getId(), modification.getVariant());
        if (storedModification == null) {
            storedModification = new StoredImageModification();
            storedModification.setImageId(image.getId());
            storedModification.setModification(modification);
            modificationDao.insertModification(storedModification);
        } else {
            storedModification.setModification(modification);
            modificationDao.updateModification(storedModification);
        }
    }

    @Override
    public List<StoredImageModification> getModificationsForImage(Image image) {
        return modificationDao.getModificationsForImage(image.getId());
    }

    @Override
    public Crop getCropForVariant(Image image, ImageVariant variant) {
        StoredImageModification modification = modificationDao.getModification(image.getId(), variant);
        if (modification != null) {
            return modification.getModification().getCrop();
        }
        //No crop registered. Re-use the crop of 'closest' variant for this image
        List<StoredImageModification> modifications = new ArrayList<>(modificationDao.getModificationsForImage(image.getId()));
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
