package com.foreach.imageserver.client;

import com.foreach.imageserver.dto.DimensionsDto;

public interface DppImageServerClient extends ImageServerClient {

    public DimensionsDto loadImage(String imageServerUrl, int applicationId, String applicationToken, int dioContentId);

}
