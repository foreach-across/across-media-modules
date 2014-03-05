package com.foreach.imageserver.core.web.controllers;

import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.services.ApplicationService;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.core.services.ImageVariantService;
import com.foreach.imageserver.core.web.dto.ImageModificationDto;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Controller
public class ImageStreamingController {
    private static final Logger LOG = LoggerFactory.getLogger(ImageStreamingController.class);

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ImageService imageService;
    @Autowired
    private ImageVariantService imageVariantService;

    @RequestMapping(value = "/view", method = RequestMethod.GET)
    public void view(@RequestParam(value = "aid", required = true) int applicationId,
                     @RequestParam(value = "key", required = true) String imageKey,
                     ImageModificationDto modificationDto,
                     HttpServletResponse response) {

        Application application = applicationService.getApplicationById(applicationId);
        if (application == null || !application.isActive()) {
            LOG.debug("Application not found or inactive {}", applicationId);
            error(response, HttpStatus.UNAUTHORIZED, "Application " + applicationId + " not found or inactive");
            return;
        }

        ImageVariant imageVariant = imageVariantService.getBestVariantForModification(application, modificationDto);
        if (imageVariant == null) {
            error(response, HttpStatus.NOT_FOUND, "Could not find image variant");
            return;
        }

        ImageModification modification = new ImageModification(imageVariant, modificationDto.getCrop());
        Image image = imageService.getImageByKey(imageKey, application.getId());

        if (image == null) {
            error(response, HttpStatus.NOT_FOUND, "Image " + imageKey + " not found");
            return;
        }

        ImageFile imageFile = imageService.fetchImageFile(image, modification);

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(imageFile.getImageType().getContentType());
        response.setContentLength(Long.valueOf(imageFile.getFileSize()).intValue());

        InputStream content = null;

        try {
            content = imageFile.openContentStream();
            IOUtils.copy(content, response.getOutputStream());
        } catch (IOException ioe) {
            error(response, HttpStatus.INTERNAL_SERVER_ERROR, ioe.getMessage());
        } finally {
            IOUtils.closeQuietly(content);
        }
    }

    private void error(HttpServletResponse response, HttpStatus status, String errorMessage) {
        response.setStatus(status.value());
        response.setContentType("text/plain");
        ByteArrayInputStream bis = new ByteArrayInputStream(errorMessage.getBytes());
        try {
            IOUtils.copy(bis, response.getOutputStream());
        } catch (IOException e) {
            LOG.error("Failed to write error message to output stream");
        } finally {
            IOUtils.closeQuietly(bis);
        }
    }
}
