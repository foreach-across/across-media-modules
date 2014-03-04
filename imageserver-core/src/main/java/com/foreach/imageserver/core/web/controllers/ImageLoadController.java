package com.foreach.imageserver.core.web.controllers;

import com.foreach.imageserver.core.business.Application;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.services.ApplicationService;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.core.services.repositories.ImageLookupRepository;
import com.foreach.imageserver.core.services.repositories.RepositoryLookupResult;
import com.foreach.imageserver.core.web.displayables.JsonResponse;
import com.foreach.imageserver.core.web.exceptions.ApplicationDeniedException;
import com.foreach.imageserver.core.web.exceptions.ImageLookupException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;

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
                              @RequestParam(value = "uri", required = true) String repositoryURI,
                              @RequestParam(value = "key", required = false) String targetKey) {
        Application application = applicationService.getApplicationById(applicationId);

        if (application == null || !application.canBeManaged(applicationKey)) {
            throw new ApplicationDeniedException();
        }

        ImageLookupRepository imageLookupRepository = determineLookupRepository(repositoryURI);

        RepositoryLookupResult lookupResult = imageLookupRepository.fetchImage(repositoryURI);
        if (!lookupResult.isSuccess()) {
            return error("Failed to retrieve image " + lookupResult.getStatus());
        }

        String imageKey = StringUtils.defaultIfEmpty(targetKey, repositoryURI);
        Image image = imageService.getImageByKey(imageKey, application.getId());

        if (image == null) {
            image = createNewImage(application, imageKey);
        }

        imageService.save(image, lookupResult);
        return success();
    }

    private ImageLookupRepository determineLookupRepository(String uri) {
        for (ImageLookupRepository repository : imageLookupRepositories) {
            if (repository.isValidURI(uri)) {
                return repository;
            }
        }

        throw new ImageLookupException("Did not find any lookup repositories that can handle uri: " + uri);
    }

    private Image createNewImage(Application application, String imageKey) {
        Image image = new Image();
        image.setApplicationId(application.getId());
        image.setKey(imageKey);

        return image;
    }

}
