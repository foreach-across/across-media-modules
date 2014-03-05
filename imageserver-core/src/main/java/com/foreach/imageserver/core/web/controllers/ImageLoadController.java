package com.foreach.imageserver.core.web.controllers;

import com.foreach.imageserver.core.business.Application;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.services.ApplicationService;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.core.services.repositories.ImageLookupRepository;
import com.foreach.imageserver.core.services.repositories.RepositoryLookupResult;
import com.foreach.imageserver.core.web.displayables.JsonResponse;
import com.foreach.imageserver.core.web.exceptions.ApplicationDeniedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ImageLoadController extends BaseImageAPIController {
    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private Collection<ImageLookupRepository> imageLookupRepositories;

    @Autowired
    private ImageService imageService;

    @RequestMapping("/load")
    @ResponseBody
    public JsonResponse load(@RequestParam(value = "aid", required = true) int applicationId,
                             @RequestParam(value = "token", required = true) String applicationKey,
                             @RequestParam(value = "repo", required = true) String repositoryCode,
                             @RequestParam(value = "key", required = false) String targetKey,
                             @RequestParam java.util.Map<String, String> allParameters) {
        Application application = applicationService.getApplicationById(applicationId);

        if (application == null || !application.canBeManaged(applicationKey)) {
            throw new ApplicationDeniedException();
        }

        ImageLookupRepository imageLookupRepository = determineLookupRepository(repositoryCode);
        if (imageLookupRepository == null) {
            return error("Unknown repository " + repositoryCode);
        }

        RepositoryLookupResult lookupResult = imageLookupRepository.fetchImage(getRepoParameters(repositoryCode, allParameters));
        if (!lookupResult.isSuccess()) {
            return error("Failed to retrieve image " + lookupResult.getStatus());
        }

        String imageKey = StringUtils.defaultIfEmpty(targetKey, lookupResult.getDefaultKey());

        Image image = imageService.getImageByKey(imageKey, application.getId());

        if (image == null) {
            image = createNewImage(application, imageKey);
        }

        imageService.save(image, lookupResult);
        return success(imageKey);
    }

    private Map<String, String> getRepoParameters(String repositoryCode, Map<String, String> requestParameters) {
        String repositoryPrefix = repositoryCode + ".";
        Map<String, String> result = new HashMap<>();
        for (String key : requestParameters.keySet()) {
            if (key.startsWith(repositoryPrefix)) {
                result.put(key.substring(repositoryPrefix.length()), requestParameters.get(key));
            }
        }
        return result;
    }

    private ImageLookupRepository determineLookupRepository(String connectorCode) {
        for (ImageLookupRepository repository : imageLookupRepositories) {
            if (repository.getCode().equals(connectorCode)) {
                return repository;
            }
        }
        return null;
    }

    private Image createNewImage(Application application, String imageKey) {
        Image image = new Image();
        image.setApplicationId(application.getId());
        image.setKey(imageKey);

        return image;
    }

}
