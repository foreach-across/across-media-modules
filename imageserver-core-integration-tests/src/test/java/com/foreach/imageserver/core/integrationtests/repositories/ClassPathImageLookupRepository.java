package com.foreach.imageserver.core.integrationtests.repositories;

import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.services.repositories.ImageLookupRepository;
import com.foreach.imageserver.core.services.repositories.RepositoryLookupResult;
import com.foreach.imageserver.core.services.repositories.RepositoryLookupStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Map;

@Component
public class ClassPathImageLookupRepository implements ImageLookupRepository {

    private static final String REPOSITORY_CODE = "classpath";
    private static final String URI_PARAMETER = "uri";

    @Override
    public String getCode() {
        return REPOSITORY_CODE;
    }

    @Override
    public RepositoryLookupResult fetchImage(Map<String, String> parameters) {
        String uri = parameters.get(URI_PARAMETER);
        if (StringUtils.isEmpty(uri)) {
            return failure();
        }

        ImageType imageType = ImageType.getForExtension(uri);
        if (imageType == null) {
            return failure();
        }

        InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(uri);
        if (resourceStream == null) {
            return failure();
        }

        return success(imageType, resourceStream);
    }

    private RepositoryLookupResult success(ImageType imageType, InputStream resourceStream) {
        RepositoryLookupResult result = new RepositoryLookupResult();
        result.setStatus(RepositoryLookupStatus.SUCCESS);
        result.setImageType(imageType);
        result.setContent(resourceStream);
        return result;
    }

    private RepositoryLookupResult failure() {
        RepositoryLookupResult result = new RepositoryLookupResult();
        result.setStatus(RepositoryLookupStatus.ERROR);
        return result;
    }

}