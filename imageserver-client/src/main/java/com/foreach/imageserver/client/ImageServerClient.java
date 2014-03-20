package com.foreach.imageserver.client;

import com.foreach.imageserver.dto.ImageModificationDto;
import com.foreach.imageserver.dto.ImageResolutionDto;
import com.foreach.imageserver.dto.ImageTypeDto;

import java.io.InputStream;
import java.util.List;

public interface ImageServerClient {

    String createImageUrl(String imageServerUrl, int applicationId, int imageId, Integer width, Integer height, ImageTypeDto imageType);

    InputStream fetchImage(String imageServerUrl, int applicationId, int imageId, Integer width, Integer height, ImageTypeDto imageType);

    void registerImageModification(String imageServerUrl, int applicationId, String applicationToken, int imageId, ImageResolutionDto imageResolutionDto, ImageModificationDto imageModificationDto);

    //List<RegisteredImageModificationDto> listRegisteredModifications(String imageServerUrl, int applicationId, String applicationToken, int imageId);

    List<ImageResolutionDto> listAllowedResolutions(String imageServerUrl, int applicationId, String applicationToken);

}
