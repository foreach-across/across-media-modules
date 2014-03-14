package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Application;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.data.ApplicationDao;
import com.foreach.imageserver.core.data.ImageResolutionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private ImageResolutionDao imageResolutionDao;

    @Override
    public Application getById(int id) {
        return applicationDao.getById(id);
    }

    // TODO Move logic directly into the data layer please; let's not iterate.
    @Override
    public ImageResolution getImageResolution(int applicationId, Integer width, Integer height) {
        List<ImageResolution> imageResolutions = imageResolutionDao.getForApplication(applicationId);
        for (ImageResolution imageResolution : imageResolutions) {
            if (imageResolution.getWidth() == width && imageResolution.getHeight() == height) {
                return imageResolution;
            }
        }

        return null;
    }

}
