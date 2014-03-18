package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.business.ImageParameters;
import com.foreach.imageserver.core.business.WebImageParameters;
import com.foreach.imageserver.core.data.WebImageParametersDao;
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
    public ImageParameters getImageParameters(int id) {
        return webImageParametersDao.getById(id);
    }

    @Override
    public ImageParameters getImageParameters(Map<String, String> repositoryParameters) {
        String url = extractUrl(repositoryParameters);
        return webImageParametersDao.getByParameters(url);
    }

    @Override
    public RetrievedOriginalImage retrieveImage(Map<String, String> repositoryParameters) {
        String url = extractUrl(repositoryParameters);

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

            Dimensions dimensions = imageTransformService.computeDimensions(imageType, imageBytes);

            WebImageParameters originalImage = new WebImageParameters();
            originalImage.setUrl(url);
            originalImage.setDimensions(dimensions);
            originalImage.setImageType(imageType);
            webImageParametersDao.insert(originalImage);

            return new RetrievedOriginalImage(originalImage, imageBytes);
        } catch (IOException e) {
            throw new ImageCouldNotBeRetrievedException(e);
        } finally {
            IOUtils.closeQuietly(imageStream);
        }
    }

    @Override
    public boolean parametersAreEqual(int imageId, Map<String, String> repositoryParameters) {
        WebImageParameters storedImage = webImageParametersDao.getById(imageId);
        String suppliedUrl = extractUrl(repositoryParameters);
        return storedImage.getUrl().equals(suppliedUrl);
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

}
