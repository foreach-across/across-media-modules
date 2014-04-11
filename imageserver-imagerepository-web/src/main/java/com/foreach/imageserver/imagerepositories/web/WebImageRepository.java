package com.foreach.imageserver.imagerepositories.web;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.imagerepositories.web.business.WebImageParameters;
import com.foreach.imageserver.core.services.*;
import com.foreach.imageserver.core.transformers.InMemoryImageSource;
import com.foreach.imageserver.imagerepositories.web.data.WebImageParametersDao;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Service
@Exposed
@Conditional(WebImageRepositoryConditional.class)
public class WebImageRepository implements ImageRepository {

    public static final String CODE = "web";

    @Autowired
    private WebImageParametersDao webImageParametersDao;

    @Autowired
    private ImageTransformService imageTransformService;

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    public RetrievedImage retrieveImage(int imageId, Map<String, String> repositoryParameters) {
        String url = extractUrl(repositoryParameters);

        InMemoryImageSource imageSource = retrieveImageFromWeb(url);
        Dimensions dimensions = imageTransformService.computeDimensions(imageSource.stream());

        WebImageParameters imageParameters = new WebImageParameters();
        imageParameters.setImageId(imageId);
        imageParameters.setUrl(url);
        webImageParametersDao.insert(imageParameters);

        return new RetrievedImage(dimensions, imageSource.getImageType(), imageSource.getImageBytes());
    }

    @Override
    public byte[] retrieveImage(int imageId) {
        // TODO We blindly assume that the image hasn't changed since we first downloaded it.
        WebImageParameters imageParameters = webImageParametersDao.getById(imageId);
        InMemoryImageSource imageSource = retrieveImageFromWeb(imageParameters.getUrl());
        return imageSource.getImageBytes();
    }

    private String extractUrl(Map<String, String> repositoryParameters) {
        if (!repositoryParameters.containsKey("url")) {
            throw new MissingRepositoryParameterException("Missing repository parameter 'url'.");
        }

        String url = repositoryParameters.get("url");
        if (StringUtils.isBlank(url)) {
            throw new MissingRepositoryParameterException("Repository parameter 'url' should not be blank.");
        }

        return url;
    }

    private InMemoryImageSource retrieveImageFromWeb(String url) {
        InputStream imageStream = null;
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            imageStream = entity.getContent();

            if (response.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                throw new ImageCouldNotBeRetrievedException("Received HTTP error response code when trying to retrieve image.");
            }

            ImageType imageType = ImageType.getForContentType(entity.getContentType().getValue());
            if (imageType == null) {
                throw new ImageCouldNotBeRetrievedException("Retrieved image has unknown content type.");
            }

            byte[] imageBytes = IOUtils.toByteArray(imageStream);

            return new InMemoryImageSource(imageType, imageBytes);
        } catch (IOException e) {
            throw new ImageCouldNotBeRetrievedException(e);
        } finally {
            IOUtils.closeQuietly(imageStream);
        }
    }

}
