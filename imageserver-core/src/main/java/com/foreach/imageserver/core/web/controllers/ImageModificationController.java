package com.foreach.imageserver.core.web.controllers;

import com.foreach.imageserver.core.business.Application;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageModification;
import com.foreach.imageserver.core.business.ImageVariant;
import com.foreach.imageserver.core.services.ApplicationService;
import com.foreach.imageserver.core.services.ImageModificationService;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.core.services.ImageVariantService;
import com.foreach.imageserver.core.services.exceptions.ImageModificationException;
import com.foreach.imageserver.core.web.dto.ImageModificationDto;
import com.foreach.imageserver.core.web.exceptions.ApplicationDeniedException;
import com.foreach.imageserver.core.web.exceptions.ImageNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/modification")
public class ImageModificationController {
    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private ImageModificationService imageModificationService;

    @Autowired
    private ImageVariantService imageVariantService;

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    @ResponseBody
    public String register(@RequestParam(value = "aid", required = true) int applicationId,
                           @RequestParam(value = "token", required = true) String applicationKey,
                           @RequestParam(value = "key", required = true) String imageKey,
                           ImageModificationDto modificationDto) {
        Application application = applicationService.getApplicationById(applicationId);
        if (application == null || !application.canBeManaged(applicationKey)) {
            throw new ApplicationDeniedException();
        }
        if (modificationDto.getHeight() == 0 && modificationDto.getWidth() == 0) {
            throw new ImageModificationException("No target width or height specified.");
        }
        if (modificationDto.getCrop().isEmpty()) {
            throw new ImageModificationException("No crop specified");
        }
        ImageVariant imageVariant = imageVariantService.getVariantForModification(application, modificationDto);
        if (imageVariant == null) {
            throw new ImageModificationException("Could not find image variant!");
        }
        ImageModification modification = new ImageModification(imageVariant, modificationDto.getCrop());
        Image image = imageService.getImageByKey(imageKey, application.getId());
        if (image == null) {
            throw new ImageNotFoundException();
        }
        imageModificationService.saveModification(image, modification);
        return StringUtils.EMPTY;
    }

}
