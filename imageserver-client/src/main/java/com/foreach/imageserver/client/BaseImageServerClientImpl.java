package com.foreach.imageserver.client;

import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.web.controllers.ImageModificationController;
import com.foreach.imageserver.core.web.controllers.ImageStreamingController;
import com.foreach.imageserver.core.web.displayables.JsonResponse;
import com.foreach.imageserver.core.web.dto.ImageModificationDto;
import com.foreach.imageserver.core.web.dto.ImageResolutionDto;
import com.foreach.imageserver.core.web.dto.RegisteredImageModificationDto;
import org.codehaus.jackson.map.ObjectMapper;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public abstract class BaseImageServerClientImpl implements ImageServerClient {

    @Override
    public String createImageUrl(String imageServerUrl, int applicationId, int imageId, Integer width, Integer height, ImageType imageType) {
        String result = "http://" + imageServerUrl + "/" + ImageStreamingController.VIEW_PATH + "?aid=" + applicationId + "&iid=" + imageId;
        if (imageType != null) {
            result += "&type=" + imageType.name();
        }
        if (width != null) {
            result += "&width=" + width;
        }
        if (height != null) {
            result += "&height=" + height;
        }
        return result;
    }

    @Override
    public InputStream fetchImage(String imageServerUrl, int applicationId, int imageId, Integer width, Integer height, ImageType imageType) {
        Client client = ClientBuilder.newBuilder().newClient();
        WebTarget target = client.target(imageServerUrl).path(ImageStreamingController.VIEW_PATH);
        target = target.queryParam("aid", applicationId);
        target = target.queryParam("iid", imageId);
        target = target.queryParam("width", width);
        target = target.queryParam("height", height);
        target = target.queryParam("height", height);
        target = target.queryParam("imageType", imageType.name());
        Response response = target.request().get();
        return (InputStream) response.getEntity();
    }

    @Override
    public void registerImageModification(String imageServerUrl, int applicationId, String applicationToken, int imageId, ImageResolutionDto imageResolutionDto, ImageModificationDto imageModificationDto) {
        Client client = ClientBuilder.newBuilder().newClient();
        WebTarget target = client.target(imageServerUrl).path(ImageModificationController.REGISTER_PATH);
        target = addApplicationParams(applicationId, applicationToken, target);
        target = target.queryParam("iid", imageId);
        target = addObjectFields(target, imageResolutionDto);
        target = addObjectFields(target, imageModificationDto);
        JsonResponse response = target.request().post(null, JsonResponse.class);
        if (!response.isSuccess()) {
            throw new RuntimeException("Unexpected exception while registering image modification " + response.getErrorMessage());
        }
    }
/*
    @Override
    public List<RegisteredImageModificationDto> listRegisteredModifications(String imageServerUrl, int applicationId, String applicationToken, int imageId) {
        Client client = ClientBuilder.newBuilder().newClient();
          WebTarget target = client.target(imageServerUrl).path(ImageModificationController.LIST_REGISTERED_PATH);
      target = addApplicationParams(applicationId, applicationToken, target);
        target = target.queryParam("iid", imageId);
        JsonResponse<List<RegisteredImageModificationDto>> response = target.request().get(JsonResponse.class);
        if (!response.isSuccess()) {
            throw new RuntimeException("Unexpected exception while getting the list of registered modifications for image " + imageId + " " + response.getErrorMessage());
        }
        return response.getResult();
    }
*/
    @Override
    public List<ImageResolutionDto> listAllowedResolutions(String imageServerUrl, int applicationId, String applicationToken) {
        Client client = ClientBuilder.newBuilder().newClient();
        WebTarget target = client.target(imageServerUrl).path(ImageModificationController.LIST_RESOLUTIONS_PATH);
        target = addApplicationParams(applicationId, applicationToken, target);
        JsonResponse<List<ImageResolutionDto>> response = target.request().get(JsonResponse.class);
        if (!response.isSuccess()) {
            throw new RuntimeException("Unexpected exception while getting the list of allowed resolutions " + response.getErrorMessage());
        }
        return response.getResult();
    }

    protected WebTarget addApplicationParams(int applicationId, String applicationToken, WebTarget target) {
        return target.queryParam("aid", applicationId).queryParam("token", applicationToken);
    }


    private WebTarget addObjectFields(WebTarget target, Object object) {
        ObjectMapper m = new ObjectMapper();
        Map<String, Object> props = m.convertValue(object, Map.class);
        for (Map.Entry<String, Object> property : props.entrySet()) {
            target = target.queryParam(property.getKey(), property.getValue());
        }
        return target;
    }

}
