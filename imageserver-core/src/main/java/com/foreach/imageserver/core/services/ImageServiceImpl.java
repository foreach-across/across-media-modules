package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.data.ImageDao;
import com.foreach.imageserver.core.data.ImageModificationDao;
import com.foreach.imageserver.core.data.ImageResolutionDao;
import com.foreach.imageserver.core.transformers.InMemoryImageSource;
import com.foreach.imageserver.core.transformers.StreamImageSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ImageServiceImpl implements ImageService {

    @Autowired
    private ImageDao imageDao;

    @Autowired
    private ImageStoreService imageStoreService;

    @Autowired
    private ImageModificationDao imageModificationDao;

    @Autowired
    private ImageTransformService imageTransformService;

    @Autowired
    private ImageRepositoryService imageRepositoryService;

    @Autowired
    private ImageResolutionDao imageResolutionDao;

    @Override
    public Image getById(int imageId) {
        return imageDao.getById(imageId);
    }

    // TODO I'm not taking care of errors right now, make sure to tackle this later on!
    @Override
    @Transactional
    public ImageSaveResult saveImage(ImageRepository imageRepository, Map<String, String> repositoryParameters) throws ImageStoreException {
        Image image = new Image();
        image.setDateCreated(new Date());
        image.setRepositoryCode(imageRepository.getCode());
        imageDao.insert(image);

        RetrievedImage retrievedImage = imageRepository.retrieveImage(image.getImageId(), repositoryParameters);
        image.setDimensions(retrievedImage.getDimensions());
        image.setImageType(retrievedImage.getImageType());
        imageDao.updateParameters(image);
        imageStoreService.storeOriginalImage(image, retrievedImage.getImageBytes());

        ImageSaveResult result = new ImageSaveResult();
        result.setImageId(image.getImageId());
        result.setDimensions(image.getDimensions());
        return result;
    }

    /**
     * DO NOT MAKE THIS METHOD TRANSACTIONAL! If we are updating an existing modification, we need to make sure that
     * the changes are committed to the database *before* we clean up the filesystem. Otherwise a different instance
     * might recreate variants on disk using the old values.
     */
    @Override
    public void saveImageModification(ImageModification modification) {
        ImageModification existingModification = imageModificationDao.getById(modification.getImageId(), modification.getContextId(), modification.getResolutionId());
        if (existingModification == null) {
            imageModificationDao.insert(modification);
        } else {
            imageModificationDao.update(modification);
        }

        imageStoreService.removeVariants(modification.getImageId());
    }

    @Override
    public StreamImageSource getVariantImage(Image image, Context context, ImageResolution imageResolution, ImageVariant imageVariant) {
        StreamImageSource imageSource = imageStoreService.getVariantImage(image, context, imageResolution, imageVariant);
        if (imageSource == null) {
            ImageModification imageModification = imageModificationDao.getById(image.getImageId(), context.getId(), imageResolution.getId());
            if (imageModification == null) {
                throw new ImageCouldNotBeRetrievedException("No image modification was registered for this image.");
            }

            StreamImageSource originalImageSource = imageStoreService.getOriginalImage(image);
            if (originalImageSource == null) {
                ImageRepository imageRepository = imageRepositoryService.determineImageRepository(image.getRepositoryCode());
                if (imageRepository == null) {
                    throw new ImageCouldNotBeRetrievedException("Missing image repository.");
                }

                byte[] imageBytes = imageRepository.retrieveImage(image.getImageId());
                imageStoreService.storeOriginalImage(image, imageBytes);
                originalImageSource = new StreamImageSource(image.getImageType(), imageBytes);
            }

            Dimensions outputResolution = computeOutputResolution(image, imageResolution);

            InMemoryImageSource variantImageSource = imageTransformService.modify(
                    originalImageSource,
                    outputResolution.getWidth(),
                    outputResolution.getHeight(),
                    imageModification.getCrop().getX(),
                    imageModification.getCrop().getY(),
                    imageModification.getCrop().getWidth(),
                    imageModification.getCrop().getHeight(),
                    imageModification.getDensity().getWidth(),
                    imageModification.getDensity().getHeight(),
                    imageVariant.getOutputType());

            // TODO We might opt to catch exceptions here and not fail on the write. We can return the variant in memory regardless.
            imageStoreService.storeVariantImage(image, context, imageResolution, imageVariant, variantImageSource.byteStream());

            /**
             * The ImageModification objects we used to determine the Crop may have changed while we were busy
             * generating it. On the other hand, we expect the chances of this actually happening to be pretty low. To
             * avoid having to keep database locking in mind whenever we work with ImageModification-s, we employ some
             * semi-optimistic concurrency control. Specifically: we always write the file without any advance
             * checking. This may cause us to serve stale variants for a very short while. Then we check that the
             * ImageModification was not altered behind our back. Should this be the case we delete the variant from
             * disk; it will then be recreated during the next request.
             */
            ImageModification reviewImageModification = imageModificationDao.getById(image.getImageId(), context.getId(), imageResolution.getId());
            if (!imageModification.equals(reviewImageModification)) {
                imageStoreService.removeVariantImage(image, context, imageResolution, imageVariant);
            }

            return variantImageSource.stream();
        }

        return imageSource;
    }

    @Override
    public boolean hasModification(int imageId) {
        return imageModificationDao.hasModification(imageId);
    }

    @Override
    public ImageResolution getResolution(int resolutionId) {
        return imageResolutionDao.getById(resolutionId);
    }

    @Override
    public List<ImageModification> getModifications(int imageId, int contextId) {
        return imageModificationDao.getModifications(imageId, contextId);
    }

    private Dimensions computeOutputResolution(Image image, ImageResolution imageResolution) {
        Integer resolutionWidth = imageResolution.getWidth();
        Integer resolutionHeight = imageResolution.getHeight();

        if (resolutionWidth != null && resolutionHeight != null) {
            return dimensions(resolutionWidth, resolutionHeight);
        } else {
            double originalWidth = image.getDimensions().getWidth();
            double originalHeight = image.getDimensions().getHeight();

            if (resolutionWidth != null) {
                return dimensions(resolutionWidth, (int) Math.round(resolutionWidth * (originalHeight / originalWidth)));
            } else {
                return dimensions((int) Math.round(resolutionHeight * (originalWidth / originalHeight)), resolutionHeight);
            }
        }
    }

    private Dimensions dimensions(int width, int height) {
        Dimensions dimensions = new Dimensions();
        dimensions.setWidth(width);
        dimensions.setHeight(height);
        return dimensions;
    }

}
