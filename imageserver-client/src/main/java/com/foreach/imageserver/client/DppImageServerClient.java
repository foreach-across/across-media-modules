package com.foreach.imageserver.client;

import com.foreach.imageserver.core.web.dto.LoadedImageDto;

public interface DppImageServerClient extends ImageServerClient {

    public LoadedImageDto loadImage(String imageServerUrl, int applicationId, String applicationToken, int dioContentId);

}
