package com.foreach.imageserver.core.web.controllers;

import com.foreach.imageserver.core.web.dto.ImageResolutionDto;
import com.foreach.imageserver.core.web.dto.ImageVariantDto;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Controller
public class ImageStreamingController {
    private static final Logger LOG = LoggerFactory.getLogger(ImageStreamingController.class);

    @RequestMapping(value = "/view", method = RequestMethod.GET)
    public void view(@RequestParam(value = "aid", required = true) int applicationId,
                     @RequestParam(value = "iid", required = true) int imageId,
                     ImageResolutionDto imageResolutionDto,
                     ImageVariantDto imageVariantDto,
                     HttpServletResponse response) {


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
