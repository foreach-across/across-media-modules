package com.foreach.imageserver.core.web.controllers;

import com.foreach.imageserver.core.web.displayables.JsonResponse;
import com.foreach.imageserver.core.web.dto.ImageModificationDto;
import com.foreach.imageserver.core.web.dto.ImageResolutionDto;
import com.foreach.imageserver.core.web.dto.RegisteredImageModificationDto;
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

    @RequestMapping(value = "/" + REGISTER_PATH, method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse register(@RequestParam(value = "aid", required = true) int applicationId,
                                 @RequestParam(value = "token", required = true) String applicationKey,
                                 @RequestParam(value = "iid", required = true) int imageId,
                                 ImageResolutionDto imageResolutionDto,
                                 ImageModificationDto imageModificationDto) {
        //TODO
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
