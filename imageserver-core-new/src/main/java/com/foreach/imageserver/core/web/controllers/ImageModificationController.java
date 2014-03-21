package com.foreach.imageserver.core.web.controllers;

import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.services.ContextService;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/modification")
public class ImageModificationController extends BaseImageAPIController {

    public static final String REGISTER_PATH = "register";
    public static final String LIST_RESOLUTIONS_PATH = "listResolutions";
    public static final String LIST_MODIFICATION_STATUS_PATH = "listModificationStatus";

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
                                 @RequestParam(value = "context", required = true) String contextCode,
                                 ImageResolutionDto imageResolutionDto,
                                 ImageModificationDto imageModificationDto) {
        if (!this.accessToken.equals(accessToken)) {
            return error("Access denied.");
        }

        Image image = imageService.getById(imageId);
        if (image == null) {
            return error("No such image.");
        }

        Context context = contextService.getByCode(contextCode);
        if (context == null) {
            return error("No such context.");
        }

        ImageResolution imageResolution = contextService.getImageResolution(context.getId(), imageResolutionDto.getWidth(), imageResolutionDto.getHeight());
        if (imageResolution == null) {
            return error("No such image resolution.");
        }

        ImageModification modification = new ImageModification();
        modification.setImageId(imageId);
        modification.setContextId(context.getId());
        modification.setResolutionId(imageResolution.getId());
        modification.setCrop(toBusiness(imageModificationDto.getCrop()));
        modification.setDensity(toBusiness(imageModificationDto.getDensity()));

        imageService.saveImageModification(modification);

        return success();
    }

    @RequestMapping(value = "/" + LIST_RESOLUTIONS_PATH, method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse listResolutions(@RequestParam(value = "token", required = true) String accessToken,
                                        @RequestParam(value = "context", required = true) String contextCode) {
        if (!this.accessToken.equals(accessToken)) {
            return error("Access denied.");
        }

        Context context = contextService.getByCode(contextCode);
        if (context == null) {
            return error("No such context.");
        }

        List<ImageResolution> imageResolutions = contextService.getImageResolutions(context.getId());

        return success(imageResolutionDtoList(imageResolutions));
    }

    @RequestMapping(value = "/" + LIST_MODIFICATION_STATUS_PATH, method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse listModificationStatus(@RequestParam(value = "token", required = true) String accessToken,
                                               @RequestParam(value = "iid", required = true) List<Integer> imageIds) {
        if (!this.accessToken.equals(accessToken)) {
            return error("Access denied.");
        }

        List<ModificationStatusDto> modificationStatusList = new ArrayList<>(imageIds.size());
        for (int imageId : imageIds) {
            boolean hasModification = imageService.hasModification(imageId);

            ModificationStatusDto modificationStatus = new ModificationStatusDto();
            modificationStatus.setImageId(imageId);
            modificationStatus.setModified(hasModification);

            modificationStatusList.add(modificationStatus);
        }

        return success(modificationStatusList);
    }

    private List<ImageResolutionDto> imageResolutionDtoList(List<ImageResolution> imageResolutions) {
        List<ImageResolutionDto> dtos = new ArrayList<>(imageResolutions.size());
        for (ImageResolution imageResolution : imageResolutions) {
            ImageResolutionDto dto = new ImageResolutionDto();
            dto.setWidth(imageResolution.getWidth());
            dto.setHeight(imageResolution.getHeight());
            dtos.add(dto);
        }
        return dtos;
    }

    private Crop toBusiness(CropDto dto) {
        Crop crop = new Crop();
        crop.setX(dto.getX());
        crop.setY(dto.getY());
        crop.setWidth(dto.getWidth());
        crop.setHeight(dto.getHeight());
        return crop;
    }

    private Dimensions toBusiness(DimensionsDto dto) {
        Dimensions dimensions = new Dimensions();
        dimensions.setWidth(dto.getWidth());
        dimensions.setHeight(dto.getHeight());
        return dimensions;
    }

}
