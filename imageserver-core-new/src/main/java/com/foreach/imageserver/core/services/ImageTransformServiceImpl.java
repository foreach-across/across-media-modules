package com.foreach.imageserver.core.services;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.annotations.PostRefresh;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.transformers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@Exposed
public class ImageTransformServiceImpl implements ImageTransformService {

    @Autowired
    private ImageTransformerRegistry imageTransformerRegistry;

    private List<ImageTransformer> imageTransformers;

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
            dimensions = translateDimensions(imageTransformer.execute(action));
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
            result = imageTransformer.execute(action);
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
