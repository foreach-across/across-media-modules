package com.foreach.imageserver.core.web.controllers;

import com.foreach.across.core.annotations.Refreshable;
import com.foreach.imageserver.core.business.Application;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.services.ApplicationService;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.core.services.OriginalImageRepository;
import com.foreach.imageserver.core.web.displayables.JsonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Controller
@Refreshable
public class ImageLoadController extends BaseImageAPIController {

    public static final String LOAD_IMAGE_PATH = "load";

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private Collection<OriginalImageRepository> imageRepositories;

    @RequestMapping("/" + LOAD_IMAGE_PATH)
    @ResponseBody
    public JsonResponse load(@RequestParam(value = "aid", required = true) int applicationId,
                             @RequestParam(value = "token", required = true) String applicationKey,
                             @RequestParam(value = "repo", required = true) String repositoryCode,
                             @RequestParam(value = "iid", required = true) int imageId,
                             @RequestParam Map<String, String> allParameters) {
        Application application = applicationService.getById(applicationId);
        if (application == null || !application.canBeManaged(applicationKey)) {
            return error(String.format("Unknown application %s.", applicationId));
        }

        OriginalImageRepository imageRepository = determineImageRepository(repositoryCode);
        if (imageRepository == null) {
            return error(String.format("Unknown original image repository %s.", repositoryCode));
        }

        Map<String, String> repositoryParameters = getRepositoryParameters(repositoryCode, allParameters);

        Image existingImage = imageService.getById(applicationId, imageId);
        if (existingImage != null) {
            // We silently allow this, provided that the same original image is loaded.
            if (!existingImage.getRepositoryCode().equals(repositoryCode)) {
                return error(String.format("A different image exists for application id %d and image id %d.", applicationId, imageId));
            }

            if (!imageRepository.parametersAreEqual(existingImage.getOriginalImageId(), repositoryParameters)) {
                return error(String.format("A different image exists for application id %d and image id %d.", applicationId, imageId));
            }
        } else {
            imageService.saveImage(applicationId, imageId, imageRepository, repositoryParameters);
        }

        return success();
    }

    private Map<String, String> getRepositoryParameters(String code, Map<String, String> requestParameters) {
        String repositoryPrefix = code + ".";
        int repositoryPrefixLength = repositoryPrefix.length();
        Map<String, String> result = new HashMap<>();
        for (String key : requestParameters.keySet()) {
            if (key.startsWith(repositoryPrefix)) {
                result.put(key.substring(repositoryPrefixLength), requestParameters.get(key));
            }
        }
        return result;
    }

    private OriginalImageRepository determineImageRepository(String code) {
        for (OriginalImageRepository repository : imageRepositories) {
            if (repository.getRepositoryCode().equals(code)) {
                return repository;
            }
        }
        return null;
    }

}

