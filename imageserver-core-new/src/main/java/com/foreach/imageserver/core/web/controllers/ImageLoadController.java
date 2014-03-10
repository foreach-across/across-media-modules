package com.foreach.imageserver.core.web.controllers;

import com.foreach.across.core.annotations.Refreshable;
import com.foreach.imageserver.core.web.displayables.JsonResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@Refreshable
public class ImageLoadController extends BaseImageAPIController {

    @RequestMapping("/load")
    @ResponseBody
    public JsonResponse load(@RequestParam(value = "aid", required = true) int applicationId,
                             @RequestParam(value = "token", required = true) String applicationKey,
                             @RequestParam(value = "repo", required = true) String repositoryCode,
                             @RequestParam(value = "iid", required = true) int imageId,
                             @RequestParam Map<String, String> allParameters) {
        //TODO
        return success();
    }

}
