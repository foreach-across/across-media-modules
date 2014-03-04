package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Application;
import com.foreach.imageserver.core.business.ImageVariant;
import com.foreach.imageserver.core.web.dto.ImageModificationDto;

import java.util.List;

public interface ImageVariantService {

    List<ImageVariant> getRegisteredVariantsForApplication(Application application);

    ImageVariant getVariantForModification(Application application, ImageModificationDto modificationDto);
}
