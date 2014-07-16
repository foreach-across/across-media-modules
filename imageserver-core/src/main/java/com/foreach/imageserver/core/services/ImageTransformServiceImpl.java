package com.foreach.imageserver.core.services;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.logging.LogHelper;
import com.foreach.imageserver.core.transformers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.concurrent.Semaphore;

@Service
@Exposed
public class ImageTransformServiceImpl implements ImageTransformService {

    private static Logger LOG = LoggerFactory.getLogger(ImageTransformServiceImpl.class);

    @Autowired
    private ImageTransformerRegistry imageTransformerRegistry;

    private Semaphore semaphore;

    @Autowired
    public ImageTransformServiceImpl(@Value("${transformer.concurrentTransformLimit}") int concurrentTransformLimit) {
        /**
         * Right now, we have only one ImageTransformer implementation and it runs on the local machine. In theory,
         * however, we could have implementations that off-load the actual computations to other machines. Should this
         * ever get to be the case, we may want to provide more fine-grained control over the number of concurrent
         * transformations. For now, a single limit will suffice.
         */

        this.semaphore = new Semaphore(concurrentTransformLimit, true);
    }

    @Override
    public Dimensions computeDimensions(StreamImageSource imageSource) {
        if (imageSource == null) {
            LOG.warn("Null parameters not allowed - ImageTransformServiceImpl#computeDimensions: imageSource=null");
        }

        final ImageCalculateDimensionsAction action = new ImageCalculateDimensionsAction(imageSource);

        ImageTransformer imageTransformer = findAbleTransformer(new CanExecute() {
            @Override
            public ImageTransformerPriority consider(ImageTransformer imageTransformer) {
                return imageTransformer.canExecute(action);
            }
        });

        // TODO I'm opting for returning null in case of failure now, maybe raise an exception instead?
        Dimensions dimensions = null;
        if (imageTransformer != null) {
            semaphore.acquireUninterruptibly();
            try {
                dimensions = imageTransformer.execute(action);
            } catch(Exception e){
                LOG.error("Error while computing dimensions - ImageTransformServiceImpl#computeDimensions: imageSource={}", LogHelper.flatten(imageSource), e);
            } finally {
                semaphore.release();
            }
        }
        return dimensions;
    }

    @Override
    public ImageAttributes getAttributes(InputStream imageStream) {
        if (imageStream == null) {
            LOG.warn("Null parameters not allowed - ImageTransformServiceImpl#getAttributes: imageStream=null");
        }

        final GetImageAttributesAction action = new GetImageAttributesAction(imageStream);

        ImageTransformer imageTransformer = findAbleTransformer(new CanExecute() {
            @Override
            public ImageTransformerPriority consider(ImageTransformer imageTransformer) {
                return imageTransformer.canExecute(action);
            }
        });

        // TODO I'm opting for returning null in case of failure now, maybe raise an exception instead?
        ImageAttributes imageAttributes = null;
        if (imageTransformer != null) {
            semaphore.acquireUninterruptibly();
            try {
                imageAttributes = imageTransformer.execute(action);
            } catch (Exception e) {
                LOG.error("Encountered failure during image transform - ImageTransformServiceImpl#getAttributes: imageStream={}", imageStream, e);
            } finally {
                semaphore.release();
            }
        }
        return imageAttributes;
    }

    @Override
    public InMemoryImageSource modify(StreamImageSource imageSource, int outputWidth, int outputHeight, int cropX, int cropY, int cropWidth, int cropHeight, int densityWidth, int densityHeight, ImageType outputType) {
        if (imageSource == null) {
            LOG.warn("Null parameters not allowed - ImageTransformServiceImpl#modify: imageSource, outputWidth={}, outputHeight={}, cropX={}, cropY={}, cropWidth={}, cropHeight={}, densityWidth={}, densityHeight={}, outputType={}", LogHelper.flatten(imageSource, outputWidth, outputHeight, cropX, cropY, cropWidth, cropHeight, densityWidth, densityHeight, outputType));
        }

        final ImageModifyAction action = new ImageModifyAction(
                imageSource,
                outputWidth,
                outputHeight,
                cropX,
                cropY,
                cropWidth,
                cropHeight,
                densityWidth,
                densityHeight,
                outputType);

        ImageTransformer imageTransformer = findAbleTransformer(new CanExecute() {
            @Override
            public ImageTransformerPriority consider(ImageTransformer imageTransformer) {
                return imageTransformer.canExecute(action);
            }
        });

        // TODO I'm opting for returning null in case of failure now, maybe raise an exception instead?
        InMemoryImageSource result = null;
        if (imageTransformer != null) {
            semaphore.acquireUninterruptibly();
            try {
                result = imageTransformer.execute(action);
            } catch (Exception e){
                LOG.warn("Encountered error modifying file - ImageTransformServiceImpl#modify: imageSource, outputWidth={}, outputHeight={}, cropX={}, cropY={}, cropWidth={}, cropHeight={}, densityWidth={}, densityHeight={}, outputType={}", LogHelper.flatten(imageSource, outputWidth, outputHeight, cropX, cropY, cropWidth, cropHeight, densityWidth, densityHeight, outputType));
            } finally {
                semaphore.release();
            }
        }
        return result;
    }

    private ImageTransformer findAbleTransformer(CanExecute canExecute) {
        ImageTransformer firstFallback = null;

        for (ImageTransformer imageTransformer : imageTransformerRegistry) {
            ImageTransformerPriority priority = canExecute.consider(imageTransformer);
            if (priority == ImageTransformerPriority.PREFERRED) {
                return imageTransformer;
            } else if (priority == ImageTransformerPriority.FALLBACK && firstFallback == null) {
                firstFallback = imageTransformer;
            }
        }

        return firstFallback;
    }

    private interface CanExecute {
        ImageTransformerPriority consider(ImageTransformer imageTransformer);
    }
}
