package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.managers.ImageManager;
import com.foreach.imageserver.core.managers.ImageModificationManager;
import com.foreach.imageserver.core.managers.ImageResolutionManager;
import com.foreach.imageserver.core.transformers.ImageAttributes;
import com.foreach.imageserver.core.transformers.InMemoryImageSource;
import com.foreach.imageserver.core.transformers.StreamImageSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImageServiceImpl implements ImageService {

    @Autowired
    private ImageManager imageManager;

    @Autowired
    private ImageStoreService imageStoreService;

    @Autowired
    private ImageModificationManager imageModificationManager;

    @Autowired
    private ImageTransformService imageTransformService;

    @Autowired
    private CropGenerator cropGenerator;

    @Autowired
    private ImageResolutionManager imageResolutionManager;

    @Override
    public Image getById(int imageId) {
        return imageManager.getById(imageId);
    }

    // Used concurrently from multiple threads.
    private Map<VariantImageRequest, FutureVariantImage> futureVariantImages = new HashMap<>();

    // TODO I'm not taking care of errors right now, make sure to tackle this later on!
    @Override
    @Transactional
    public ImageSaveResult saveImage(byte[] imageBytes) throws ImageStoreException {
        ImageAttributes imageAttributes = imageTransformService.getAttributes(new ByteArrayInputStream(imageBytes));

        Image image = new Image();
        image.setDateCreated(new Date());
        image.setDimensions(imageAttributes.getDimensions());
        image.setImageType(imageAttributes.getType());
        imageManager.insert(image);

        imageStoreService.storeOriginalImage(image, imageBytes);

        ImageSaveResult result = new ImageSaveResult();
        result.setImageId(image.getId());
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
        ImageModification existingModification = imageModificationManager.getById(modification.getImageId(), modification.getContextId(), modification.getResolutionId());
        if (existingModification == null) {
            imageModificationManager.insert(modification);
        } else {
            imageModificationManager.update(modification);
        }

        imageStoreService.removeVariants(modification.getImageId());
    }

    @Override
    public StreamImageSource getVariantImage(Image image, Context context, ImageResolution imageResolution, ImageVariant imageVariant) {
        StreamImageSource imageSource = imageStoreService.getVariantImage(image, context, imageResolution, imageVariant);
        if (imageSource == null) {
            imageSource = generateVariantImage(image, context, imageResolution, imageVariant);
        }
        return imageSource;
    }

    /**
     * We allow just one thread to generate a specific variant. Other threads that require this variant simultaneously
     * will block and re-use the same result.
     */
    private StreamImageSource generateVariantImage(Image image, Context context, ImageResolution imageResolution, ImageVariant imageVariant) {
        VariantImageRequest request = new VariantImageRequest(image.getId(), context.getId(), imageResolution.getId(), imageVariant.getOutputType());

        FutureVariantImage futureVariantImage;
        boolean otherThreadIsCreatingVariant;
        synchronized (this) {
            otherThreadIsCreatingVariant = futureVariantImages.containsKey(request);
            if (otherThreadIsCreatingVariant) {
                futureVariantImage = futureVariantImages.get(request);
            } else {
                futureVariantImage = new FutureVariantImage();
                futureVariantImages.put(request, futureVariantImage);
            }
        }

        if (otherThreadIsCreatingVariant) {
            return futureVariantImage.get();
        } else {
            try {
                InMemoryImageSource variantImageSource = generateVariantImageInCurrentThread(image, context, imageResolution, imageVariant);
                synchronized (this) {
                    futureVariantImage.setResult(variantImageSource);
                    futureVariantImages.remove(request);
                }
                return variantImageSource.stream();
            } catch (RuntimeException e) {
                synchronized (this) {
                    futureVariantImage.setRuntimeException(e);
                    futureVariantImages.remove(request);
                }
                throw e;
            } catch (Error e) {
                synchronized (this) {
                    futureVariantImage.setError(e);
                    futureVariantImages.remove(request);
                }
                throw e;
            }
        }
    }

    private InMemoryImageSource generateVariantImageInCurrentThread(Image image, Context context, ImageResolution imageResolution, ImageVariant imageVariant) {
        Crop crop = obtainCrop(image, context, imageResolution);

        StreamImageSource originalImageSource = imageStoreService.getOriginalImage(image);
        if (originalImageSource == null) {
            throw new ImageCouldNotBeRetrievedException("The original image is not available on disk.");
        }

        Dimensions outputResolution = computeOutputResolution(image, imageResolution);

        InMemoryImageSource variantImageSource = imageTransformService.modify(
                originalImageSource,
                outputResolution.getWidth(),
                outputResolution.getHeight(),
                crop.getX(),
                crop.getY(),
                crop.getWidth(),
                crop.getHeight(),
                0,
                0,
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
        Crop reviewCrop = obtainCrop(image, context, imageResolution);
        if (!crop.equals(reviewCrop)) {
            imageStoreService.removeVariantImage(image, context, imageResolution, imageVariant);
        }

        return variantImageSource;
    }

    private Crop obtainCrop(Image image, Context context, ImageResolution imageResolution) {
        Crop result;

        ImageModification imageModification = imageModificationManager.getById(image.getId(), context.getId(), imageResolution.getId());
        if (imageModification != null) {
            result = imageModification.getCrop();
        } else {
            List<ImageModification> modifications = imageModificationManager.getAllModifications(image.getId());
            result = cropGenerator.generateCrop(image, context, imageResolution, modifications);
        }

        if (result == null) {
            throw new ImageCouldNotBeRetrievedException("No crop could be determined for this image.");
        }

        return result;
    }

    @Override
    public boolean hasModification(int imageId) {
        return imageModificationManager.hasModification(imageId);
    }

    @Override
    public ImageResolution getResolution(int resolutionId) {
        return imageResolutionManager.getById(resolutionId);
    }

    @Override
    public List<ImageModification> getModifications(int imageId, int contextId) {
        return imageModificationManager.getModifications(imageId, contextId);
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

    private static class VariantImageRequest {
        private final int imageId;
        private final int contextId;
        private final int resolutionId;
        private final ImageType outputType;

        public VariantImageRequest(int imageId, int contextId, int resolutionId, ImageType outputType) {
            this.imageId = imageId;
            this.contextId = contextId;
            this.resolutionId = resolutionId;
            this.outputType = outputType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            VariantImageRequest that = (VariantImageRequest) o;

            if (contextId != that.contextId) return false;
            if (imageId != that.imageId) return false;
            if (resolutionId != that.resolutionId) return false;
            if (outputType != that.outputType) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = imageId;
            result = 31 * result + contextId;
            result = 31 * result + resolutionId;
            result = 31 * result + (outputType != null ? outputType.hashCode() : 0);
            return result;
        }
    }

    private static class FutureVariantImage {
        private InMemoryImageSource result;
        private RuntimeException exception;
        private Error error;

        public synchronized StreamImageSource get() {
            try {
                while (result == null && exception == null && error == null) {
                    wait();
                }

                if (exception != null) {
                    throw exception;
                }

                if (error != null) {
                    throw error;
                }

                return result.stream();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new VariantGenerationWasInterruptedException();
            }
        }

        public synchronized void setResult(InMemoryImageSource result) {
            this.result = result;
            notifyAll();
        }

        public synchronized void setRuntimeException(RuntimeException e) {
            this.exception = e;
            notifyAll();
        }

        public synchronized void setError(Error e) {
            this.error = e;
            notifyAll();
        }
    }

    private static class VariantGenerationWasInterruptedException extends RuntimeException {
    }
}
