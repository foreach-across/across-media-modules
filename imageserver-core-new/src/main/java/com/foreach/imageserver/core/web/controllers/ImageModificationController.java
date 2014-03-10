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

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse register(@RequestParam(value = "aid", required = true) int applicationId,
                                 @RequestParam(value = "token", required = true) String applicationKey,
                                 @RequestParam(value = "iid", required = true) int imageId,
                                 ImageResolutionDto imageResolutionDto,
                                 ImageModificationDto imageModificationDto) {
        //TODO
        return success();
    }

    @RequestMapping(value = "/listRegistered", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse<List<RegisteredImageModificationDto>> listRegistered(@RequestParam(value = "aid", required = true) int applicationId,
                                                                             @RequestParam(value = "token", required = true) String applicationKey,
                                                                             @RequestParam(value = "iid", required = true) int imageId) {
        //TODO
        return success();
    }

    @RequestMapping(value = "/listResolutions", method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse<List<ImageResolutionDto>> listVariants(@RequestParam(value = "aid", required = true) int applicationId,
                                                               @RequestParam(value = "token", required = true) String applicationKey) {
        //TODO
        return success();
    }

}
