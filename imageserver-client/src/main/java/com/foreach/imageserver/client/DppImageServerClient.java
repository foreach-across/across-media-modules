package com.foreach.imageserver.client;

public interface DppImageServerClient extends ImageServerClient {

    public void loadImage(String imageServerUrl, int applicationId, String applicationToken, int dioContentId);

}
