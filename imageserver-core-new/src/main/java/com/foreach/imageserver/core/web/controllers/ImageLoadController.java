package com.foreach.imageserver.core.web.controllers;

import com.foreach.across.core.annotations.Refreshable;
import com.foreach.imageserver.core.services.ImageRepository;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.core.web.displayables.JsonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${accessToken}")
    private String accessToken;

    @Autowired
    private ImageService imageService;

    @Autowired
    private Collection<ImageRepository> imageRepositories;

    @RequestMapping("/" + LOAD_IMAGE_PATH)
    @ResponseBody
    public JsonResponse load(@RequestParam(value = "token", required = true) String accessToken,
                             @RequestParam(value = "iid", required = true) int imageId,
                             @RequestParam(value = "repo", required = true) String repositoryCode,
                             @RequestParam Map<String, String> allParameters) {
        if (!this.accessToken.equals(accessToken)) {
            return error("Access denied.");
        }

        ImageRepository imageRepository = determineImageRepository(repositoryCode);
        if (imageRepository == null) {
            return error(String.format("Unknown image repository %s.", repositoryCode));
        }

        Map<String, String> repositoryParameters = getRepositoryParameters(repositoryCode, allParameters);

        imageService.saveImage(imageId, imageRepository, repositoryParameters);

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

    private ImageRepository determineImageRepository(String code) {
        for (ImageRepository repository : imageRepositories) {
            if (repository.getCode().equals(code)) {
                return repository;
            }
        }
        return null;
    }

}

