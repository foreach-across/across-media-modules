package com.foreach.imageserver.core.web.controllers;

import com.foreach.imageserver.core.business.Context;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageModification;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.services.ContextService;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.core.web.displayables.JsonResponse;
import com.foreach.imageserver.core.web.dto.ImageModificationDto;
import com.foreach.imageserver.core.web.dto.ImageResolutionDto;
import com.foreach.imageserver.core.web.dto.RegisteredImageModificationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${accessToken}")
    private String accessToken;

    @Autowired
    private ContextService contextService;

    @Autowired
    private ImageService imageService;

    @RequestMapping(value = "/" + REGISTER_PATH, method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public JsonResponse register(@RequestParam(value = "token", required = true) String accessToken,
                                 @RequestParam(value = "iid", required = true) int imageId,
                                 @RequestParam(value = "cid", required = true) int contextId,
                                 ImageResolutionDto imageResolutionDto,
                                 ImageModificationDto imageModificationDto) {
        if (!this.accessToken.equals(accessToken)) {
            return error("Access denied.");
        }

        Image image = imageService.getById(imageId);
        if (image == null) {
            return error("No such image.");
        }

        Context context = contextService.getById(contextId);
        if (context == null) {
            return error("No such context.");
        }

        ImageResolution imageResolution = contextService.getImageResolution(contextId, imageResolutionDto.getWidth(), imageResolutionDto.getHeight());
        if (imageResolution == null) {
            return error("No such image resolution.");
        }

        ImageModification modification = new ImageModification();
        modification.setImageId(imageId);
        modification.setContextId(contextId);
        modification.setResolutionId(imageResolution.getId());
        modification.setCrop(imageModificationDto.getCrop());
        modification.setDensity(imageModificationDto.getDensity());

        imageService.saveImageModification(modification);

        return success();
    }

    @RequestMapping(value = "/" + LIST_REGISTERED_PATH, method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse<List<RegisteredImageModificationDto>> listRegistered(@RequestParam(value = "token", required = true) String accessToken,
                                                                             @RequestParam(value = "iid", required = true) int imageId,
                                                                             @RequestParam(value = "cid", required = true) int contextId) {

        //TODO
        return success();
    }

    @RequestMapping(value = "/" + LIST_RESOLUTIONS_PATH, method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse<List<ImageResolutionDto>> listResolutions(@RequestParam(value = "token", required = true) String accessToken,
                                                                  @RequestParam(value = "cid", required = true) int contextId) {
        //TODO
        return success();
    }

}
