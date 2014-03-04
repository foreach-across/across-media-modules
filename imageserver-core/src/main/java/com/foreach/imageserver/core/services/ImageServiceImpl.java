package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageFile;
import com.foreach.imageserver.core.business.ImageModification;
import com.foreach.imageserver.core.data.ImageDao;
import com.foreach.imageserver.core.services.repositories.RepositoryLookupResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ImageServiceImpl implements ImageService {
    private static final Logger LOG = LoggerFactory.getLogger(ImageServiceImpl.class);

    @Autowired
    private ImageDao imageDao;

    @Autowired
    private ImageStoreService imageStoreService;

    @Autowired
    private ImageTransformService imageTransformService;

    @Autowired
    private TempFileService tempFileService;

    @Autowired
    private ImageVariantService imageVariantService;

    @Override
    public Image getImageByKey(String key, int applicationId) {
        return imageDao.getImageByKey(key, applicationId);
    }

    @Transactional
    @Override
    public void save(Image image, RepositoryLookupResult lookupResult) {
        image.setImageType(lookupResult.getImageType());

        ImageFile tempFile = tempFileService.createImageFile(lookupResult.getImageType(), lookupResult.getContent());
        image.setDimensions(imageTransformService.calculateDimensions(tempFile));

        boolean isInsert = isNewImage(image);

        if (isInsert) {
            image.setFilePath(imageStoreService.generateRelativeImagePath(image));
            imageDao.insertImage(image);
        }

        ImageFile savedFile = imageStoreService.saveImage(image, tempFile);

        image.setFileSize(savedFile.getFileSize());

        imageDao.updateImage(image);
        if (!isInsert) {
            imageStoreService.deleteVariants(image);
        }
    }

    private boolean isNewImage(Image image) {
        return image.getId() <= 0;
    }


    @Override
    public ImageFile fetchImageFile(Image image, ImageModification modifier) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Requesting image {} with modifier {}", image.getId(), modifier);
        }

        if (modifier.getCrop().isEmpty()) {
            //No crop given, compute or fetch one
            modifier.setCrop(imageVariantService.getCropForModifier(image, modifier.getVariant()));
        }

        ImageFile file = imageStoreService.getImageFile(image, modifier);

        if (file == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Generating image {} for modifier {}", image.getId(), modifier);
            }
            //File was not yet created, do that now and save it
            ImageFile modified = imageTransformService.apply(image, modifier);
            file = imageStoreService.saveImage(image, modifier, modified);
        }

        return file;
    }

    @Transactional
    @Override
    public void delete(Image image, boolean variantsOnly) {
        if (variantsOnly) {
            // Delete physical variant files
            imageStoreService.deleteVariants(image);
        } else {
            // First delete the database entry - this avoids requests coming in
            imageDao.deleteImage(image.getId());
            // Delete the actual physical files
            imageStoreService.delete(image);
        }
    }


}
