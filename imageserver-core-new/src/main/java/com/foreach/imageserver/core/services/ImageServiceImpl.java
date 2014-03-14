package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageModification;
import com.foreach.imageserver.core.business.OriginalImage;
import com.foreach.imageserver.core.data.ImageDao;
import com.foreach.imageserver.core.data.ImageModificationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class ImageServiceImpl implements ImageService {

    @Autowired
    private ImageDao imageDao;

    @Autowired
    private ImageStoreService imageStoreService;

    @Autowired
    private ImageModificationDao imageModificationDao;

    @Override
    public Image getById(int applicationId, int imageId) {
        return imageDao.getById(applicationId, imageId);
    }

    // TODO Verify that transactional has been setup properly !
    // TODO I'm not taking care of errors right now, make sure to tackle this later on!
    @Override
    @Transactional
    public void saveImage(int applicationId, int imageId, OriginalImageRepository imageRepository, Map<String, String> repositoryParameters) {
        RetrievedOriginalImage retrievedOriginalImage = null;

        OriginalImage originalImage = imageRepository.getOriginalImage(repositoryParameters);
        if (originalImage == null) {
            retrievedOriginalImage = imageRepository.insertAndRetrieveOriginalImage(repositoryParameters);
            originalImage = retrievedOriginalImage.getOriginalImage();
        }

        Image image = new Image();
        image.setApplicationId(applicationId);
        image.setImageId(imageId);
        image.setRepositoryCode(imageRepository.getRepositoryCode());
        image.setOriginalImageId(originalImage.getId());
        imageDao.insert(image);

        if (retrievedOriginalImage != null) {
            imageStoreService.storeOriginalImage(originalImage, retrievedOriginalImage.getImageBytes());
        }
    }

    // TODO Support editing existing crops. (Don't forget to remove all the variants from disk!)
    @Override
    public void saveImageModification(ImageModification modification) {
        imageModificationDao.insert(modification);
    }

}
