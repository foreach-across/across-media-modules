package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Context;
import com.foreach.imageserver.core.business.ImageResolution;

import java.util.List;

public interface ContextService {
    Context getById(int id);

    ImageResolution getImageResolution(int contextId, Integer width, Integer height);

    List<ImageResolution> getImageResolutions(int contextId);
}