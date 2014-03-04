package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.services.exceptions.ImageModificationException;
import com.foreach.imageserver.core.services.transformers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Will iterate over all registered ImageTransformers to find the best one to apply the modification.
 */
@Service
public class ImageTransformServiceImpl implements ImageTransformService {
    private static final Logger LOG = LoggerFactory.getLogger(ImageTransformServiceImpl.class);

    @Autowired
    private List<ImageTransformer> transformerList;
    @Autowired
    private ImageStoreService imageStoreService;

    @PostConstruct
    protected void sortTransformers() {
        LOG.info("Sorting {} image transformers according to configured priority", transformerList.size());
        Collections.sort(transformerList, new Comparator<ImageTransformer>() {
            @Override
            public int compare(ImageTransformer left, ImageTransformer right) {
                return Integer.valueOf(right.getPriority()).compareTo(left.getPriority());
            }
        });

        if (LOG.isDebugEnabled()) {
            LOG.debug("The following {} image transformers have been registered, in priority order:",
                    transformerList.size());
            for (ImageTransformer transformer : transformerList) {
                LOG.debug("class: {} - priority: {}", transformer.getClass(), transformer.getPriority());
            }
        }
    }

    @Override
    public Dimensions calculateDimensions(ImageFile file) {
        return execute(new ImageCalculateDimensionsAction(file));
    }

    @Override
    public ImageFile apply(Image image, ImageModification modifier) {
        ImageFile originalFile = imageStoreService.getImageFile(image);
        ImageModification normalized = modifier.normalize(image.getDimensions());
        verifyOutputType(originalFile.getImageType(), normalized.getVariant());
        return execute(new ImageModifyAction(originalFile, normalized));
    }

    private void verifyOutputType(ImageType original, ImageVariant modifier) {
        if (modifier.getOutput() == null) {
            modifier.setOutput(ImageType.getPreferredOutputType(original));
        }
    }

    private <T> T execute(ImageTransformerAction<T> action) {
        List<ImageTransformer> transformers = findTransformersForAction(action);

        if (transformers.isEmpty()) {
            LOG.error("No possible transformer for action {}", action);
            throw new ImageModificationException("No valid transformer for image modification");
        }

        for (ImageTransformer transformer : transformers) {
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Using ImageTransformer {} for action {}", transformer, action);
                }

                transformer.execute(action);

                T result = action.getResult();
                if (result != null) {
                    return result;
                } else {
                    LOG.warn("ImageTransformer {} said it could handle action {}, but it returned empty result",
                            transformer, action);
                }
            } catch (Exception e) {
                LOG.warn("ImageTransformer {} threw exception on handling action {}: {}", transformer, action, e);
            }
        }

        LOG.error("All transformers failed trying to execute action {}", action);
        throw new ImageModificationException("All transformers failed trying to apply image modification");
    }

    private <T> List<ImageTransformer> findTransformersForAction(ImageTransformerAction<T> action) {
        List<ImageTransformer> transformers = new LinkedList<>();
        List<ImageTransformer> fallback = new LinkedList<>();

        for (ImageTransformer candidate : transformerList) {
            if (candidate.isEnabled()) {
                ImageTransformerPriority priority = candidate.canExecute(action);

                if (priority != null && priority != ImageTransformerPriority.UNABLE) {
                    if (priority == ImageTransformerPriority.PREFERRED) {
                        transformers.add(candidate);
                    } else {
                        fallback.add(candidate);
                    }
                }
            }
        }

        transformers.addAll(fallback);

        return transformers;
    }
}
