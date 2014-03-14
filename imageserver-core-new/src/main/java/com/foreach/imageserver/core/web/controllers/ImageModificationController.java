package com.foreach.imageserver.core.web.controllers;

import com.foreach.imageserver.core.business.Application;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageModification;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.services.ApplicationService;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.core.web.displayables.JsonResponse;
import com.foreach.imageserver.core.web.dto.ImageModificationDto;
import com.foreach.imageserver.core.web.dto.ImageResolutionDto;
import com.foreach.imageserver.core.web.dto.RegisteredImageModificationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/modification")
public class ImageModificationController extends BaseImageAPIController {

    public static final String REGISTER_PATH = "register";
    public static final String LIST_REGISTERED_PATH = "listRegistered";
    public static final String LIST_RESOLUTIONS_PATH = "listResolutions";

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ImageService imageService;

    @RequestMapping(value = "/" + REGISTER_PATH, method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public JsonResponse register(@RequestParam(value = "aid", required = true) int applicationId,
                                 @RequestParam(value = "token", required = true) String applicationKey,
                                 @RequestParam(value = "iid", required = true) int imageId,
                                 ImageResolutionDto imageResolutionDto,
                                 ImageModificationDto imageModificationDto) {
        Application application = applicationService.getById(applicationId);
        if (application == null || !application.canBeManaged(applicationKey)) {
            return error(String.format("Unknown application %s.", applicationId));
        }

        Image image = imageService.getById(applicationId, imageId);
        if (image == null) {
            return error(String.format("No image exists for application id %d and image id %d.", applicationId, imageId));
        }

        ImageResolution imageResolution = applicationService.getImageResolution(applicationId, imageResolutionDto.getWidth(), imageResolutionDto.getHeight());
        if (imageResolution == null) {
            return error("Supplied image resolution is not valid for this application.");
        }

        ImageModification modification = new ImageModification();
        modification.setApplicationId(applicationId);
        modification.setImageId(imageId);
        modification.setResolutionId(imageResolution.getId());
        modification.setCrop(imageModificationDto.getCrop());
        modification.setDensity(imageModificationDto.getDensity());

        imageService.saveImageModification(modification);

        return success();
    }

    @RequestMapping(value = "/" + LIST_REGISTERED_PATH, method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse<List<RegisteredImageModificationDto>> listRegistered(@RequestParam(value = "aid", required = true) int applicationId,
                                                                             @RequestParam(value = "token", required = true) String applicationKey,
                                                                             @RequestParam(value = "iid", required = true) int imageId) {
        //TODO
        return success();
    }

    @RequestMapping(value = "/" + LIST_RESOLUTIONS_PATH, method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse<List<ImageResolutionDto>> listVariants(@RequestParam(value = "aid", required = true) int applicationId,
                                                               @RequestParam(value = "token", required = true) String applicationKey) {
        //TODO
        return success();
    }

}
