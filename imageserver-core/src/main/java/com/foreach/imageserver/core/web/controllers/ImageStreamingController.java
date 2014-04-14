package com.foreach.imageserver.core.web.controllers;

import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.services.ContextService;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.core.transformers.StreamImageSource;
import com.foreach.imageserver.dto.ImageResolutionDto;
import com.foreach.imageserver.dto.ImageTypeDto;
import com.foreach.imageserver.dto.ImageVariantDto;
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
    private ContextService contextService;

    @Autowired
    private ImageService imageService;

    @RequestMapping(value = "/" + VIEW_PATH, method = RequestMethod.GET)
    public void view(@RequestParam(value = "iid", required = true) int imageId,
                     @RequestParam(value = "context", required = true) String contextCode,
                     ImageResolutionDto imageResolutionDto,
                     ImageVariantDto imageVariantDto,
                     HttpServletResponse response) {
        // TODO Make sure we only rely on objects that can be long-term cached for retrieving the image.

        Image image = imageService.getById(imageId);
        if (image == null) {
            error(response, HttpStatus.NOT_FOUND, "No such image.");
            return;
        }

        Context context = contextService.getByCode(contextCode);
        if (context == null) {
            error(response, HttpStatus.NOT_FOUND, "No such context.");
            return;
        }

        ImageResolution imageResolution = contextService.getImageResolution(context.getId(), imageResolutionDto.getWidth(), imageResolutionDto.getHeight());
        if (imageResolution == null) {
            error(response, HttpStatus.NOT_FOUND, "No such resolution.");
            return;
        }

        StreamImageSource imageSource = imageService.getVariantImage(image, context, imageResolution, toBusiness(imageVariantDto));
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

    private ImageVariant toBusiness(ImageVariantDto dto) {
        ImageVariant imageVariant = new ImageVariant();
        imageVariant.setOutputType(toBusiness(dto.getImageType()));
        return imageVariant;
    }

    private ImageType toBusiness(ImageTypeDto dto) {
        switch (dto) {
            case JPEG:
                return ImageType.JPEG;
            case PNG:
                return ImageType.PNG;
            case GIF:
                return ImageType.GIF;
            case SVG:
                return ImageType.SVG;
            case EPS:
                return ImageType.EPS;
            case PDF:
                return ImageType.PDF;
            case TIFF:
                return ImageType.TIFF;
            default:
                throw new RuntimeException( "Unknown image type." );
        }
    }
}
