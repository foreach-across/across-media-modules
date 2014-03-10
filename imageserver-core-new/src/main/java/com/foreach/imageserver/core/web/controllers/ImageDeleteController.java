package com.foreach.imageserver.core.web.controllers;

import com.foreach.imageserver.core.web.displayables.JsonResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/delete")
public class ImageDeleteController extends BaseImageAPIController {

    @RequestMapping("/all")
    @ResponseBody
    public JsonResponse<Void> delete(@RequestParam(value = "aid", required = true) int applicationId,
                                     @RequestParam(value = "token", required = true) String applicationKey,
                                     @RequestParam(value = "iid", required = true) int imageId) {
        return delete(applicationId, applicationKey, imageId, false);
    }

    @RequestMapping("/variants")
    @ResponseBody
    public JsonResponse<Void> deleteVariants(@RequestParam(value = "aid", required = true) int applicationId,
                                             @RequestParam(value = "token", required = true) String applicationKey,
                                             @RequestParam(value = "iid", required = true) int imageId) {
        return delete(applicationId, applicationKey, imageId, true);
    }

    private JsonResponse<Void> delete(int applicationId, String applicationKey, int imageId, boolean variantsOnly) {
        //TODO
        return success();
    }
}
