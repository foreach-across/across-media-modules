package com.foreach.imageserver.core.services;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.annotations.PostRefresh;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.transformers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;

@Service
@Exposed
public class ImageTransformServiceImpl implements ImageTransformService {

    @Autowired
    private ImageTransformerRegistry imageTransformerRegistry;

    private List<ImageTransformer> imageTransformers;
    private Semaphore semaphore;

    @Autowired
    public ImageTransformServiceImpl( @Value( "${transformer.concurrentTransformLimit}" ) int concurrentTransformLimit) {
        /**
         * Right now, we have only one ImageTransformer implementation and it runs on the local machine. In theory,
         * however, we could have implementations that off-load the actual computations to other machines. Should this
         * ever get to be the case, we may want to provide more fine-grained control over the number of concurrent
         * transformations. For now, a single limit will suffice.
         */

        this.semaphore = new Semaphore(concurrentTransformLimit, true);
    }

    @PostRefresh
    public void init() {
        imageTransformers = new ArrayList<>(imageTransformerRegistry.getMembers());
        Collections.sort(imageTransformers, new ImageTransformerComparator());
    }

    @Override
    public Dimensions computeDimensions(StreamImageSource imageSource) {
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
                dimensions = translateDimensions(imageTransformer.execute(action));
            } finally {
                semaphore.release();
            }
        }
        return dimensions;
    }

    @Override
    public InMemoryImageSource modify(StreamImageSource imageSource, int outputWidth, int outputHeight, int cropX, int cropY, int cropWidth, int cropHeight, int densityWidth, int densityHeight, ImageType outputType) {
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
            } finally {
                semaphore.release();
            }
        }
        return result;
    }

    private ImageTransformer findAbleTransformer(CanExecute canExecute) {
        ImageTransformer firstFallback = null;

        for (ImageTransformer imageTransformer : imageTransformers) {
            ImageTransformerPriority priority = canExecute.consider(imageTransformer);
            if (priority == ImageTransformerPriority.PREFERRED) {
                return imageTransformer;
            } else if (priority == ImageTransformerPriority.FALLBACK && firstFallback == null) {
                firstFallback = imageTransformer;
            }
        }

        return firstFallback;
    }

    private Dimensions translateDimensions(com.foreach.imageserver.core.transformers.Dimensions inDimensions) {
        Dimensions outDimensions = null;
        if (inDimensions != null) {
            outDimensions = new Dimensions();
            outDimensions.setWidth(inDimensions.getWidth());
            outDimensions.setHeight(inDimensions.getHeight());
        }
        return outDimensions;
    }

    private interface CanExecute {
        ImageTransformerPriority consider(ImageTransformer imageTransformer);
    }

    private class ImageTransformerComparator implements Comparator<ImageTransformer> {
        @Override
        public int compare(ImageTransformer left, ImageTransformer right) {
            Integer leftOrder = left.getOrder();
            Integer rightOrder = right.getOrder();

            return leftOrder.compareTo(rightOrder);
        }
    }

}
