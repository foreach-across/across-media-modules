package be.mediafin.imageserver.client;

import be.persgroep.red.diocontent.api.attachment.Attachment;
import be.persgroep.red.diocontent.api.attachment.AttachmentRole;
import be.persgroep.red.diocontent.api.client.DioContentClient;
import be.persgroep.red.diocontent.webservice.client.DefaultRestDioContentClient;
import com.foreach.imageserver.dto.*;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import org.apache.commons.io.IOUtils;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

public class ImageServerClientImpl implements ImageServerClient {

    private final String imageServerEndpoint;
    private final String imageServerAccessToken;
    private final Client client;
    private final String dioServerUrl;
    private final String dioUsername;
    private final String dioPassword;

    public ImageServerClientImpl(String imageServerEndpoint, String imageServerAccessToken, String dioServerUrl, String dioUsername, String dioPassword) {
        this.imageServerEndpoint = imageServerEndpoint;
        this.imageServerAccessToken = imageServerAccessToken;
        this.dioServerUrl = dioServerUrl;
        this.dioUsername = dioUsername;
        this.dioPassword = dioPassword;

        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        this.client = Client.create(clientConfig);
    }

    @Override
    public String imageUrl(String imageId, ImageServerContext context, Integer width, Integer height, ImageTypeDto imageType) {
        return imageUrl(imageId, context, new ImageResolutionDto(width, height), new ImageVariantDto(imageType));
    }

    @Override
    public String imageUrl(String imageId, ImageServerContext context, ImageResolutionDto imageResolution, ImageVariantDto imageVariant) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.putSingle("iid", imageId);
        queryParams.putSingle("context", context.toString());
        addQueryParams(queryParams, imageResolution);
        addQueryParams(queryParams, imageVariant);

        WebResource resource = getResource("view", queryParams);
        return resource.getURI().toString();
    }

    @Override
    public InputStream imageStream(String imageId, ImageServerContext context, Integer width, Integer height, ImageTypeDto imageType) {
        return imageStream(imageId, context, new ImageResolutionDto(width, height), new ImageVariantDto(imageType));
    }

    @Override
    public InputStream imageStream(String imageId, ImageServerContext context, ImageResolutionDto imageResolution, ImageVariantDto imageVariant) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.putSingle("iid", imageId);
        queryParams.putSingle("context", context.toString());
        addQueryParams(queryParams, imageResolution);
        addQueryParams(queryParams, imageVariant);

        WebResource resource = getResource("view", queryParams);
        return resource.get(InputStream.class);
    }

    @Override
    public DimensionsDto loadImage(String imageId, int dioContentId) {
        byte[] imageBytes = retrieveImageFromDioContent(dioContentId);
        return loadImage(imageId, imageBytes);
    }

    @Override
    public DimensionsDto loadImage(String imageId, byte[] imageBytes) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.putSingle("token", imageServerAccessToken);
        queryParams.putSingle("iid", imageId);

        InputStream imageStream = null;
        try {
            imageStream = new ByteArrayInputStream(imageBytes);
            FormDataMultiPart form = new FormDataMultiPart();
            FormDataBodyPart fdp = new FormDataBodyPart("imageData", imageStream, MediaType.APPLICATION_OCTET_STREAM_TYPE);
            FormDataContentDisposition.FormDataContentDispositionBuilder builder = FormDataContentDisposition.name("imageData").fileName("imageData").size(imageBytes.length);
            fdp.setContentDisposition(builder.build());
            form.bodyPart(fdp);

            GenericType<JsonResponse<DimensionsDto>> responseType = new GenericType<JsonResponse<DimensionsDto>>() {
            };

            return getJsonResponse("load", queryParams, form, responseType);
        } finally {
            IOUtils.closeQuietly(imageStream);
        }
    }

    @Override
    public boolean imageExists(String imageId) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.putSingle("token", imageServerAccessToken);
        queryParams.putSingle("iid", imageId);

        GenericType<JsonResponse<Boolean>> responseType = new GenericType<JsonResponse<Boolean>>() {
        };

        return getJsonResponse("imageExists", queryParams, responseType);
    }

    @Override
    public void registerImageModification(String imageId, ImageServerContext context, Integer width, Integer height, int cropX, int cropY, int cropWidth, int croptHeight, int densityWidth, int densityHeight) {
        ImageResolutionDto resolution = new ImageResolutionDto(width, height);
        CropDto crop = new CropDto(cropX, cropY, cropWidth, croptHeight);
        DimensionsDto density = new DimensionsDto(densityWidth, densityHeight);
        registerImageModification(imageId, context, new ImageModificationDto(resolution, crop, density));
    }

    @Override
    public void registerImageModification(String imageId, ImageServerContext context, ImageModificationDto imageModification) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.putSingle("token", imageServerAccessToken);
        queryParams.putSingle("iid", imageId);
        queryParams.putSingle("context", context.toString());
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
    public List<ImageResolutionDto> listConfigurableResolutions(ImageServerContext context) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.putSingle("token", imageServerAccessToken);
        queryParams.putSingle("context", context.toString());
        queryParams.putSingle("configurable", "true");

        GenericType<JsonResponse<List<ImageResolutionDto>>> responseType = new GenericType<JsonResponse<List<ImageResolutionDto>>>() {
        };

        return getJsonResponse("modification/listResolutions", queryParams, responseType);
    }

    @Override
    public List<ImageModificationDto> listModifications(String imageId, ImageServerContext context) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.putSingle("token", imageServerAccessToken);
        queryParams.putSingle("iid", imageId);
        queryParams.putSingle("context", context.toString());

        GenericType<JsonResponse<List<ImageModificationDto>>> responseType = new GenericType<JsonResponse<List<ImageModificationDto>>>() {
        };

        return getJsonResponse("modification/listModifications", queryParams, responseType);
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
        ImageResolutionDto resolution = imageModification.getResolution();
        CropDto crop = imageModification.getCrop();
        DimensionsDto density = imageModification.getDensity();
        if (resolution.getWidth() != null) {
            queryParams.putSingle("resolution.width", resolution.getWidth().toString());
        }
        if (resolution.getHeight() != null) {
            queryParams.putSingle("resolution.height", resolution.getHeight().toString());
        }
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

    private <T> T getJsonResponse(String path, MultivaluedMap<String, String> queryParams, FormDataMultiPart form, GenericType<JsonResponse<T>> responseType) {
        WebResource resource = getResource(path, queryParams);
        JsonResponse<T> response = resource.type(MediaType.MULTIPART_FORM_DATA).accept(MediaType.APPLICATION_JSON).post(responseType, form);
        if (!response.isSuccess()) {
            throw new ImageServerException(response.getErrorMessage());
        }
        return response.getResult();
    }

    private WebResource getResource(String path, MultivaluedMap<String, String> queryParams) {
        return this.client.resource(imageServerEndpoint).path(path).queryParams(queryParams);
    }

    private byte[] retrieveImageFromDioContent(int dioContentId) {
        ByteArrayOutputStream data = null;
        try {
            DioContentClient client = new DefaultRestDioContentClient(dioServerUrl, dioUsername, dioPassword);
            Attachment attachment = client.getAttachmentWithRole(dioContentId, AttachmentRole.ORIGINAL);

            data = new ByteArrayOutputStream();
            client.downloadAttachment(attachment.getId(), data);
            data.flush();

            return data.toByteArray();
        } catch (Exception e) {
            throw new ImageCouldNotBeRetrievedException();
        } finally {
            IOUtils.closeQuietly(data);
        }
    }

    public static class ImageCouldNotBeRetrievedException extends RuntimeException {
    }

}
