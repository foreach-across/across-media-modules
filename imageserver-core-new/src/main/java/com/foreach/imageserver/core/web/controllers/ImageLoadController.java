package com.foreach.imageserver.core.web.controllers;

import com.foreach.across.core.annotations.Refreshable;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageSaveResult;
import com.foreach.imageserver.core.services.ImageRepository;
import com.foreach.imageserver.core.services.ImageRepositoryService;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.dto.DimensionsDto;
import com.foreach.imageserver.dto.ImageSaveResultDto;
import com.foreach.imageserver.dto.JsonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@Refreshable
public class ImageLoadController extends BaseImageAPIController {

    public static final String LOAD_IMAGE_PATH = "load";

    @Value("${accessToken}")
    private String accessToken;

    @Autowired
    private ImageService imageService;

    @Autowired
    private ImageRepositoryService imageRepositoryService;

    @RequestMapping("/" + LOAD_IMAGE_PATH)
    @ResponseBody
    public JsonResponse load(@RequestParam(value = "token", required = true) String accessToken,
                             @RequestParam(value = "repo", required = true) String repositoryCode,
                             @RequestParam Map<String, String> allParameters) {
        if (!this.accessToken.equals(accessToken)) {
            return error("Access denied.");
        }

        ImageRepository imageRepository = imageRepositoryService.determineImageRepository(repositoryCode);
        if (imageRepository == null) {
            return error(String.format("Unknown image repository %s.", repositoryCode));
        }

        Map<String, String> repositoryParameters = getRepositoryParameters(repositoryCode, allParameters);

        ImageSaveResult imageSaveResult = imageService.saveImage(imageRepository, repositoryParameters);

        return success(dto(imageSaveResult));
    }

    private Map<String, String> getRepositoryParameters(String code, Map<String, String> requestParameters) {
        String repositoryPrefix = code + ".";
        int repositoryPrefixLength = repositoryPrefix.length();
        Map<String, String> result = new HashMap<>();
        for (String key : requestParameters.keySet()) {
            if (key.startsWith(repositoryPrefix)) {
                result.put(key.substring(repositoryPrefixLength), requestParameters.get(key));
            }
        }
        return result;
    }

    private ImageSaveResultDto dto(ImageSaveResult imageSaveResult) {
        ImageSaveResultDto dto = new ImageSaveResultDto();
        dto.setDimensionsDto(dto(imageSaveResult.getDimensions()));
        dto.setImageId(imageSaveResult.getImageId());
        return dto;
    }

    private DimensionsDto dto(Dimensions dimensions) {
        DimensionsDto dto = new DimensionsDto();
        dto.setWidth(dimensions.getWidth());
        dto.setHeight(dimensions.getHeight());
        return dto;
    }

}

