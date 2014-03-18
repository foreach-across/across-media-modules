package com.foreach.imageserver.core.web.controllers;

import com.foreach.imageserver.core.business.Application;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.business.ImageVariant;
import com.foreach.imageserver.core.services.ApplicationService;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.core.transformers.StreamImageSource;
import com.foreach.imageserver.core.web.dto.ImageResolutionDto;
import com.foreach.imageserver.core.web.dto.ImageVariantDto;
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
import java.io.OutputStream;

@Controller
public class ImageStreamingController {

    public static final String VIEW_PATH = "view";
    private static final Logger LOG = LoggerFactory.getLogger(ImageStreamingController.class);

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ImageService imageService;

    @RequestMapping(value = "/" + VIEW_PATH, method = RequestMethod.GET)
    public void view(@RequestParam(value = "aid", required = true) int applicationId,
                     @RequestParam(value = "iid", required = true) int imageId,
                     ImageResolutionDto imageResolutionDto,
                     ImageVariantDto imageVariantDto,
                     HttpServletResponse response) {
        // TODO Make sure we only rely on objects that can be long-term cached for retrieving the image.
        // TODO We may want to remove an application's active flag; we want applications to be long-term cacheable.

        Application application = applicationService.getById(applicationId);
        if (application == null || !application.isActive()) {
            error(response, HttpStatus.NOT_FOUND, "No such application.");
            return;
        }

        Image image = imageService.getById(imageId);
        if (image == null) {
            error(response, HttpStatus.NOT_FOUND, "No such image.");
            return;
        }

        // TODO Implement best-effort matching.
        ImageResolution imageResolution = applicationService.getImageResolution(applicationId, imageResolutionDto.getWidth(), imageResolutionDto.getHeight());
        if (imageResolution == null) {
            error(response, HttpStatus.NOT_FOUND, "No such resolution.");
            return;
        }

        StreamImageSource imageSource = imageService.getVariantImage(image, applicationId, imageResolution, imageVariant(imageVariantDto));
        if (imageSource == null) {
            error(response, HttpStatus.NOT_FOUND, "Could not create variant.");
            return;
        }

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(imageSource.getImageType().getContentType());

        InputStream imageStream = null;
        OutputStream responseStream = null;
        try {
            imageStream = imageSource.getImageStream();
            responseStream = response.getOutputStream();
            IOUtils.copy(imageStream, responseStream);
        } catch (IOException ioe) {
            error(response, HttpStatus.INTERNAL_SERVER_ERROR, ioe.getMessage());
        } finally {
            IOUtils.closeQuietly(responseStream);
            IOUtils.closeQuietly(imageStream);
        }
    }

    private ImageVariant imageVariant(ImageVariantDto imageVariantDto) {
        ImageVariant imageVariant = new ImageVariant();
        imageVariant.setOutputType(imageVariantDto.getImageType());
        return imageVariant;
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
