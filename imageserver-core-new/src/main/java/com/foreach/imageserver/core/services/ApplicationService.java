package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Application;
import com.foreach.imageserver.core.business.ImageResolution;

public interface ApplicationService {
    Application getById(int id);

    ImageResolution getImageResolution(int applicationId, Integer width, Integer height);
}
