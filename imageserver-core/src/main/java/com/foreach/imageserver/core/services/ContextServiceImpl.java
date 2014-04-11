package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Context;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.data.ContextDao;
import com.foreach.imageserver.core.data.ImageResolutionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContextServiceImpl implements ContextService {

    // default image resolution width/height proportion
    public static final double DEFAULT_ASPECT_RATIO = 3.0 / 2.0;

    @Autowired
    private ContextDao contextDao;

    @Autowired
    private ImageResolutionDao imageResolutionDao;

    @Override
    public Context getByCode(String contextCode) {
        return contextDao.getByCode(contextCode);
    }

    /**
     * Provides an image resolution that most closely matches the given dimensions.
     * When both width and height are given this means the smallest image resolution that encompasses these dimensions.
     * When no height is given (i.e. <= 0), a height is calculated bases on the default aspect ratio (3/2).
     * The given width is expected to always be larger than zero.
     * When all image resolutions are smaller than the given dimensions, null is returned.
     *
     * @param contextId The id of the context
     * @param width     The wanted image resolution width
     * @param height    The wanted image resolution height
     * @return An image resolution
     */
    @Override
    public ImageResolution getImageResolution(int contextId, Integer width, Integer height) {
        if (width <= 0) {
            throw new ImageResolutionException("No image resolution width is specified.");
        }

        ImageResolution selectedResolution = null;

        List<ImageResolution> imageResolutions = imageResolutionDao.getForContext(contextId);
        for (ImageResolution imageResolution : imageResolutions) {

            // image resolution is large enough to contain (width, height)  (or only with, if height is zero)
            if (imageResolution.getWidth() >= width && (imageResolution.getHeight() >= height || height == 0)) {

                // image resolution is larger (in width) than previously selected image resolution
                boolean betterFit = true;

                if (selectedResolution != null) {
                    // tightest fit for width
                    betterFit &= imageResolution.getWidth() <= selectedResolution.getWidth();

                    if (height > 0) {
                        // tightest fit for height
                        betterFit &= imageResolution.getHeight() <= selectedResolution.getHeight();
                    } else {
                        // closest to default aspect ratio
                        double oldDist = Math.abs((double) selectedResolution.getWidth() / (double) selectedResolution.getHeight() - DEFAULT_ASPECT_RATIO);
                        double newDist = Math.abs((double) imageResolution.getWidth() / (double) imageResolution.getHeight() - DEFAULT_ASPECT_RATIO);
                        betterFit &= newDist <= oldDist;
                    }
                }

                if (betterFit) {
                    selectedResolution = imageResolution;
                }
            }
        }

        return selectedResolution;
    }

    @Override
    public List<ImageResolution> getImageResolutions(int contextId) {
        return imageResolutionDao.getForContext(contextId);
    }

}