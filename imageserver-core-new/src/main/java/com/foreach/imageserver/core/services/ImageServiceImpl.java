package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.data.ImageDao;
import com.foreach.imageserver.core.data.ImageModificationDao;
import com.foreach.imageserver.core.transformers.InMemoryImageSource;
import com.foreach.imageserver.core.transformers.StreamImageSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
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
    private Collection<OriginalImageRepository> imageRepositories;

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

        ImageParameters imageParameters = imageRepository.getOriginalImage(repositoryParameters);
        if (imageParameters == null) {
            retrievedOriginalImage = imageRepository.insertAndRetrieveOriginalImage(repositoryParameters);
            imageParameters = retrievedOriginalImage.getImageParameters();
        }

        Image image = new Image();
        image.setApplicationId(applicationId);
        image.setImageId(imageId);
        image.setRepositoryCode(imageRepository.getRepositoryCode());
        image.setOriginalImageId(imageParameters.getId());
        imageDao.insert(image);

        if (retrievedOriginalImage != null) {
            imageStoreService.storeOriginalImage(imageParameters, retrievedOriginalImage.getImageBytes());
        }
    }

    // TODO Support editing existing crops. (Don't forget to remove all the variants from disk!)
    @Override
    public void saveImageModification(ImageModification modification) {
        imageModificationDao.insert(modification);
    }

    @Override
    public StreamImageSource getVariantImage(Image image, ImageResolution imageResolution, ImageVariant imageVariant) {
        StreamImageSource imageSource = imageStoreService.getVariantImage(image, imageResolution, imageVariant);
        if (imageSource == null) {
            ImageModification imageModification = imageModificationDao.getById(image.getApplicationId(), image.getImageId(), imageResolution.getId());
            if (imageModification == null) {
                throw new ImageCouldNotBeRetrievedException("No image modification was registered for this image.");
            }

            OriginalImageRepository imageRepository = determineImageRepository(image.getRepositoryCode());
            if (imageRepository == null) {
                throw new ImageCouldNotBeRetrievedException("Missing image repository.");
            }

            // TODO We'll assume for now that the original image is guaranteed to be available on disk.
            ImageParameters imageParameters = imageRepository.getOriginalImage(image.getOriginalImageId());
            StreamImageSource originalImageSource = imageStoreService.getOriginalImage(imageParameters);

            Dimensions outputResolution = computeOutputResolution(imageParameters, imageResolution);

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

            // TODO Extra locking is needed here to ensure that the modification wasn't altered behind our back.
            // TODO We might opt to catch exceptions here and not fail on the write. We can return the variant in memory regardless.
            imageStoreService.storeVariantImage(image, imageResolution, imageVariant, variantImageSource.stream());

            return variantImageSource.stream();
        }

        return imageSource;
    }

    private Dimensions computeOutputResolution(ImageParameters imageParameters, ImageResolution imageResolution) {
        Integer resolutionWidth = imageResolution.getWidth();
        Integer resolutionHeight = imageResolution.getHeight();

        if (resolutionWidth != null && resolutionHeight != null) {
            return dimensions(resolutionWidth, resolutionHeight);
        } else {
            double originalWidth = imageParameters.getDimensions().getWidth();
            double originalHeight = imageParameters.getDimensions().getHeight();

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

    private OriginalImageRepository determineImageRepository(String code) {
        for (OriginalImageRepository repository : imageRepositories) {
            if (repository.getRepositoryCode().equals(code)) {
                return repository;
            }
        }
        return null;
    }

}
