package com.foreach.imageserver.client;

import com.foreach.imageserver.core.web.controllers.ImageLoadController;
import com.foreach.imageserver.core.web.displayables.JsonResponse;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

@Service
public class DppImageServerClientImpl extends BaseImageServerClientImpl implements DppImageServerClient {

    @Override
    public void loadImage(String imageServerUrl, int applicationId, String applicationToken, int dioContentId) {
        Client client = ClientBuilder.newBuilder().newClient();
        WebTarget target = client.target(imageServerUrl).path(ImageLoadController.LOAD_IMAGE_PATH);
        target = addApplicationParams(applicationId, applicationToken, target);
        target = target.queryParam("iid", dioContentId);
        target = target.queryParam("repo", "dio");
        target = target.queryParam("dio.id", dioContentId);
        JsonResponse response = target.request().post(null, JsonResponse.class);
        if (!response.isSuccess()) {
            throw new RuntimeException("Unexpected exception while registering image modification " + response.getErrorMessage());
        }
    }
}
