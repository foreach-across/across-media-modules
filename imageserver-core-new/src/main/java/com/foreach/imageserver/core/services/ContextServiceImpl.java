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

    @Autowired
    private ContextDao contextDao;

    @Autowired
    private ImageResolutionDao imageResolutionDao;

    @Override
    public Context getByCode(String contextCode) {
        return contextDao.getByCode(contextCode);
    }

    // TODO Move logic directly into the data layer please; let's not iterate.
    @Override
    public ImageResolution getImageResolution(int contextId, Integer width, Integer height) {
        List<ImageResolution> imageResolutions = imageResolutionDao.getForContext(contextId);
        for (ImageResolution imageResolution : imageResolutions) {
            if (imageResolution.getWidth().equals(width) && imageResolution.getHeight().equals(height)) {
                return imageResolution;
            }
        }

        return null;
    }

    @Override
    public List<ImageResolution> getImageResolutions(int contextId) {
        return imageResolutionDao.getForContext(contextId);
    }

}
