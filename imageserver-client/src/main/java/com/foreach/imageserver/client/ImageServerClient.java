package com.foreach.imageserver.client;


import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.web.dto.ImageModificationDto;
import com.foreach.imageserver.core.web.dto.ImageResolutionDto;
import com.foreach.imageserver.core.web.dto.RegisteredImageModificationDto;

import java.util.List;

public interface ImageServerClient {

    String createImageUrl(String imageServerUrl, int applicationId, int imageId, Integer width, Integer height, ImageType imageType);

    void registerImageModification(String imageServerUrl, int applicationId, String applicationToken, int imageId, ImageResolutionDto imageResolutionDto, ImageModificationDto imageModificationDto);

    List<RegisteredImageModificationDto> listRegisteredModifications(String imageServerUrl, int applicationId, String applicationToken, int imageId);

    List<ImageResolutionDto> listAllowedResolutions(String imageServerUrl, int applicationId, String applicationToken);

}
