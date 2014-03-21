package be.mediafin.imageserver.client;

import com.foreach.imageserver.dto.*;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ImageServerClientImpl implements ImageServerClient {

    private final String imageServerEndpoint;
    private final String imageServerAccessToken;
    private final Client client;

    public ImageServerClientImpl(String imageServerEndpoint, String imageServerAccessToken) {
        this.imageServerEndpoint = imageServerEndpoint;
        this.imageServerAccessToken = imageServerAccessToken;

        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        this.client = Client.create(clientConfig);
    }

    @Override
    public String imageUrl(int imageId, ImageServerContext context, Integer width, Integer height, ImageTypeDto imageType) {
        return imageUrl(imageId, context, new ImageResolutionDto(width, height), new ImageVariantDto(imageType));
    }

    @Override
    public String imageUrl(int imageId, ImageServerContext context, ImageResolutionDto imageResolution, ImageVariantDto imageVariant) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.putSingle("iid", "" + imageId);
        queryParams.putSingle("context", context.toString());
        addQueryParams(queryParams, imageResolution);
        addQueryParams(queryParams, imageVariant);

        WebResource resource = getResource("view", queryParams);
        return resource.getURI().toString();
    }

    @Override
    public InputStream imageStream(int imageId, ImageServerContext context, Integer width, Integer height, ImageTypeDto imageType) {
        return imageStream(imageId, context, new ImageResolutionDto(width, height), new ImageVariantDto(imageType));
    }

    @Override
    public InputStream imageStream(int imageId, ImageServerContext context, ImageResolutionDto imageResolution, ImageVariantDto imageVariant) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.putSingle("iid", "" + imageId);
        queryParams.putSingle("context", context.toString());
        addQueryParams(queryParams, imageResolution);
        addQueryParams(queryParams, imageVariant);

        WebResource resource = getResource("view", queryParams);
        return resource.get(InputStream.class);
    }

    @Override
    public DimensionsDto loadImage(int imageId, int dioContentId) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.putSingle("token", imageServerAccessToken);
        queryParams.putSingle("iid", "" + imageId);
        queryParams.putSingle("repo", "dc");
        queryParams.putSingle("dc.id", "" + dioContentId);

        GenericType<JsonResponse<DimensionsDto>> responseType = new GenericType<JsonResponse<DimensionsDto>>() {
        };

        return getJsonResponse("load", queryParams, responseType);
    }

    @Override
    public void registerImageModification(int imageId, ImageServerContext context, Integer width, Integer height, int cropX, int cropY, int cropWidth, int croptHeight, int densityWidth, int densityHeight) {
        CropDto crop = new CropDto(cropX, cropY, cropWidth, croptHeight);
        DimensionsDto density = new DimensionsDto(densityWidth, densityHeight);
        registerImageModification(imageId, context, new ImageResolutionDto(width, height), new ImageModificationDto(crop, density));
    }

    @Override
    public void registerImageModification(int imageId, ImageServerContext context, ImageResolutionDto imageResolution, ImageModificationDto imageModification) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.putSingle("token", imageServerAccessToken);
        queryParams.putSingle("iid", "" + imageId);
        queryParams.putSingle("context", context.toString());
        addQueryParams(queryParams, imageResolution);
        addQueryParams(queryParams, imageModification);

        GenericType<JsonResponse<Object>> responseType = new GenericType<JsonResponse<Object>>() {
        };

        getJsonResponse("modification/register", queryParams, responseType);
    }

    @Override
    public List<ImageResolutionDto> listAllowedResolutions(ImageServerContext context) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.putSingle("token", imageServerAccessToken);
        queryParams.putSingle("context", context.toString());

        GenericType<JsonResponse<List<ImageResolutionDto>>> responseType = new GenericType<JsonResponse<List<ImageResolutionDto>>>() {
        };

        return getJsonResponse("modification/listResolutions", queryParams, responseType);
    }

    @Override
    public List<ModificationStatusDto> listModificationStatus(List<Integer> imageIds) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.putSingle("token", imageServerAccessToken);
        queryParams.put("iid", stringList(imageIds));

        GenericType<JsonResponse<List<ModificationStatusDto>>> responseType = new GenericType<JsonResponse<List<ModificationStatusDto>>>() {
        };

        return getJsonResponse("modification/listModificationStatus", queryParams, responseType);
    }

    private List<String> stringList(List<Integer> integers) {
        List<String> strings = new ArrayList<>(integers.size());
        for (Integer i : integers) {
            strings.add(i.toString());
        }
        return strings;
    }

    private void addQueryParams(MultivaluedMap<String, String> queryParams, ImageResolutionDto imageResolution) {
        if (imageResolution.getWidth() != null) {
            queryParams.putSingle("width", imageResolution.getWidth().toString());
        }
        if (imageResolution.getHeight() != null) {
            queryParams.putSingle("height", imageResolution.getHeight().toString());
        }
    }

    private void addQueryParams(MultivaluedMap<String, String> queryParams, ImageVariantDto imageVariant) {
        queryParams.putSingle("imageType", imageVariant.getImageType().toString());
    }

    private void addQueryParams(MultivaluedMap<String, String> queryParams, ImageModificationDto imageModification) {
        CropDto crop = imageModification.getCrop();
        DimensionsDto density = imageModification.getDensity();
        queryParams.putSingle("crop.x", Integer.toString(crop.getX()));
        queryParams.putSingle("crop.y", Integer.toString(crop.getY()));
        queryParams.putSingle("crop.width", Integer.toString(crop.getWidth()));
        queryParams.putSingle("crop.height", Integer.toString(crop.getHeight()));
        queryParams.putSingle("density.width", Integer.toString(density.getWidth()));
        queryParams.putSingle("density.height", Integer.toString(density.getHeight()));
    }

    private <T> T getJsonResponse(String path, MultivaluedMap<String, String> queryParams, GenericType<JsonResponse<T>> responseType) {
        WebResource resource = getResource(path, queryParams);
        JsonResponse<T> response = resource.accept(MediaType.APPLICATION_JSON).get(responseType);
        if (!response.isSuccess()) {
            throw new ImageServerException(response.getErrorMessage());
        }
        return response.getResult();
    }

    private WebResource getResource(String path, MultivaluedMap<String, String> queryParams) {
        return this.client.resource(imageServerEndpoint).path(path).queryParams(queryParams);
    }

}