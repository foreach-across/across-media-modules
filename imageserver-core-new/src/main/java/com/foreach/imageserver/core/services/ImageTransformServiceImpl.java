package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.transformers.ImageCalculateDimensionsAction;
import com.foreach.imageserver.core.transformers.ImageSource;
import com.foreach.imageserver.core.transformers.ImageTransformer;
import com.foreach.imageserver.core.transformers.ImageTransformerPriority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class ImageTransformServiceImpl implements ImageTransformService {

    @Autowired
    private List<ImageTransformer> imageTransformers;

    @PostConstruct
    public void init() {
        Collections.sort(imageTransformers, new ImageTransformerComparator());
    }

    @Override
    public Dimensions computeDimensions(ImageType imageType, byte[] imageBytes) {
        final ImageSource imageSource = new ImageSource(imageType, new ByteArrayInputStream(imageBytes));
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