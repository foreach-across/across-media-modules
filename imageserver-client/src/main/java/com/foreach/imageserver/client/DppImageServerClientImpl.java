package com.foreach.imageserver.client;

import com.foreach.imageserver.dto.DimensionsDto;
import com.foreach.imageserver.dto.JsonResponse;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

public class DppImageServerClientImpl extends BaseImageServerClientImpl implements DppImageServerClient {

    @Override
    public DimensionsDto loadImage(String imageServerUrl, int applicationId, String applicationToken, int dioContentId) {
        Client client = ClientBuilder.newBuilder().newClient();
        WebTarget target = client.target(imageServerUrl).path("load");
        target = addApplicationParams(applicationId, applicationToken, target);
        target = target.queryParam("iid", dioContentId);
        target = target.queryParam("repo", "dio");
        target = target.queryParam("dio.id", dioContentId);
        JsonResponse response = target.request().post(null, JsonResponse.class);
        if (!response.isSuccess()) {
            throw new RuntimeException("Unexpected exception while registering image modification " + response.getErrorMessage());
        }
        return (DimensionsDto) response.getResult();
    }
}
