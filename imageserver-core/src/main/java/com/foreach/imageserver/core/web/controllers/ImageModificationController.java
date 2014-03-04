package com.foreach.imageserver.core.web.controllers;

import com.foreach.imageserver.core.business.Application;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageVariant;
import com.foreach.imageserver.core.services.ApplicationService;
import com.foreach.imageserver.core.services.ImageVariantService;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.core.services.exceptions.ImageModificationException;
import com.foreach.imageserver.core.web.dto.ImageModifierDto;
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
    private ImageVariantService imageVariantService;

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    @ResponseBody
    public String register(@RequestParam(value = "aid", required = true) int applicationId,
                           @RequestParam(value = "token", required = true) String applicationKey,
                           @RequestParam(value = "key", required = true) String imageKey,
                           ImageModifierDto modifierDto) {
        Application application = applicationService.getApplicationById(applicationId);
        ImageVariant modifier = new ImageVariant(modifierDto);

        if (application == null || !application.canBeManaged(applicationKey)) {
            throw new ApplicationDeniedException();
        }

        if (modifier.getModifier().getHeight() == 0 && modifier.getModifier().getWidth() == 0) {
            throw new ImageModificationException("No target width or height specified.");
        }

        if (modifier.getCrop().isEmpty()) {
            throw new ImageModificationException("No crop specified");
        }

        Image image = imageService.getImageByKey(imageKey, application.getId());

        if (image == null) {
            throw new ImageNotFoundException();
        }

        imageVariantService.registerVariant(image, modifier);

        return StringUtils.EMPTY;
    }

}
