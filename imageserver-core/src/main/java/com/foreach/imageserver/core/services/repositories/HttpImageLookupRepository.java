package com.foreach.imageserver.core.services.repositories;

import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.services.exceptions.RepositoryLookupException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.UnknownHostException;
import java.util.Map;

/**
 * Will fetch an image from a specific url.
 */
@Service
public class HttpImageLookupRepository implements ImageLookupRepository {

    private static final Logger LOG = LoggerFactory.getLogger(HttpImageLookupRepository.class);

    @Override
    public String getCode() {
        return "web";
    }

    public RepositoryLookupResult fetchImage(Map<String, String> parameters) {
        RepositoryLookupResult result = new RepositoryLookupResult();

        String uri = parameters.get("url");
        if (StringUtils.isEmpty(uri)) {
            LOG.info("No url parameter passed ");
            result.setStatus(RepositoryLookupStatus.ERROR);
            return result;
        }
        result.setDefaultKey(uri);

        try {
            LOG.info("Fetching remote image with url " + uri);

            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(uri);

            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();

            result.setStatus(RepositoryLookupStatus.getForHttpStatusCode(response.getStatusLine().getStatusCode()));

            if (result.getStatus() == RepositoryLookupStatus.SUCCESS) {
                ImageType imageType = ImageType.getForContentType(entity.getContentType().getValue());

                if (imageType == null) {
                    throw new RepositoryLookupException("Unknown Content-Type: " + entity.getContentType());
                }
                result.setImageType(imageType);
                result.setContent(entity.getContent());
            }
        } catch (UnknownHostException uhe) {
            LOG.error("Could not fetch image from " + uri, uhe);

            result.setStatus(RepositoryLookupStatus.NOT_FOUND);
        } catch (Exception e) {
            LOG.error("Exception fetching image from " + uri, e);

            result.setStatus(RepositoryLookupStatus.ERROR);
        }

        return result;
    }
}
