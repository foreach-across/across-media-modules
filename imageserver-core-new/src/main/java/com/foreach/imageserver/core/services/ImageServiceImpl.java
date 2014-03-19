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
    private Collection<ImageRepository> imageRepositories;

    @Override
    public Image getById(int imageId) {
        return imageDao.getById(imageId);
    }

    // TODO Verify that transactional has been setup properly !
    // TODO I'm not taking care of errors right now, make sure to tackle this later on!
    @Override
    @Transactional
    public Dimensions saveImage(int imageId, ImageRepository imageRepository, Map<String, String> repositoryParameters) throws ImageStoreException {
        Image existingImage = getById(imageId);
        ImageParameters imageParameters = null;
        if (existingImage != null) {
            // We silently allow this, provided that the same original image is loaded.
            verifySameParameters(existingImage, imageRepository, repositoryParameters);
            imageParameters = imageRepository.getImageParameters(existingImage.getImageId());
        } else {
            Image image = new Image();
            image.setImageId(imageId);
            image.setRepositoryCode(imageRepository.getCode());
            imageDao.insert(image);

            RetrievedImage retrievedImage = imageRepository.retrieveImage(imageId, repositoryParameters);
            imageParameters = retrievedImage.getImageParameters();

            imageStoreService.storeOriginalImage(retrievedImage.getImageParameters(), retrievedImage.getImageBytes());
        }

        return imageParameters.getDimensions();
    }

    // TODO Support editing existing crops. (Don't forget to remove all the variants from disk!)
    @Override
    public void saveImageModification(ImageModification modification) {
        imageModificationDao.insert(modification);
    }

    @Override
    public StreamImageSource getVariantImage(Image image, Context context, ImageResolution imageResolution, ImageVariant imageVariant) {
        StreamImageSource imageSource = imageStoreService.getVariantImage(image, context, imageResolution, imageVariant);
        if (imageSource == null) {
            ImageModification imageModification = imageModificationDao.getById(image.getImageId(), context.getId(), imageResolution.getId());
            if (imageModification == null) {
                throw new ImageCouldNotBeRetrievedException("No image modification was registered for this image.");
            }

            ImageRepository imageRepository = determineImageRepository(image.getRepositoryCode());
            if (imageRepository == null) {
                throw new ImageCouldNotBeRetrievedException("Missing image repository.");
            }

            // TODO We'll assume for now that the original image is guaranteed to be available on disk.
            ImageParameters imageParameters = imageRepository.getImageParameters(image.getImageId());
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
            imageStoreService.storeVariantImage(image, context, imageResolution, imageVariant, variantImageSource.stream());

            return variantImageSource.stream();
        }

        return imageSource;
    }

    @Override
    public boolean hasModification(int imageId) {
        return imageModificationDao.hasModification(imageId);
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

    private ImageRepository determineImageRepository(String code) {
        for (ImageRepository repository : imageRepositories) {
            if (repository.getCode().equals(code)) {
                return repository;
            }
        }
        return null;
    }

    private void verifySameParameters(Image existingImage, ImageRepository imageRepository, Map<String, String> repositoryParameters) {
        if (!existingImage.getRepositoryCode().equals(imageRepository.getCode())) {
            throw new ImageStoreException(String.format("Image with id %d already exists.", existingImage.getImageId()));
        }

        if (!imageRepository.parametersAreEqual(existingImage.getImageId(), repositoryParameters)) {
            throw new ImageStoreException(String.format("Image with id %d already exists.", existingImage.getImageId()));
        }
    }

}
