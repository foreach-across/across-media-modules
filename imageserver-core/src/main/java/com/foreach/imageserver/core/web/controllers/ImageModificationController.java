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
import java.util.Iterator;
import java.util.List;

@Controller
@RequestMapping("/modification")
public class ImageModificationController extends BaseImageAPIController {

    public static final String REGISTER_PATH = "register";
    public static final String LIST_RESOLUTIONS_PATH = "listResolutions";
    public static final String LIST_MODIFICATIONS = "listModifications";

    @Value("${accessToken}")
    private String accessToken;

    @Autowired
    private ContextService contextService;

    @Autowired
    private ImageService imageService;

    @RequestMapping(value = "/" + REGISTER_PATH, method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public JsonResponse register(@RequestParam(value = "token", required = true) String accessToken,
                                 @RequestParam(value = "iid", required = true) String externalId,
                                 @RequestParam(value = "context", required = true) String contextCode,
                                 ImageModificationDto imageModificationDto) {
        if (!this.accessToken.equals(accessToken)) {
            return error("Access denied.");
        }

        Image image = imageService.getByExternalId(externalId);
        if (image == null) {
            return error("No such image.");
        }

        Context context = contextService.getByCode(contextCode);
        if (context == null) {
            return error("No such context.");
        }

        ImageResolution imageResolution = contextService.getImageResolution(context.getId(), imageModificationDto.getResolution().getWidth(), imageModificationDto.getResolution().getHeight());
        if (imageResolution == null) {
            return error("No such image resolution : " + imageModificationDto.getResolution().getWidth() + "x" + imageModificationDto.getResolution().getHeight());
        }

        ImageModification modification = new ImageModification();
        modification.setImageId(image.getId());
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
                                        @RequestParam(value = "context", required = true) String contextCode,
                                        @RequestParam(value = "configurable", required = true, defaultValue = "false") boolean configurableOnly
    ) {
        if (!this.accessToken.equals(accessToken)) {
            return error("Access denied.");
        }

        Context context = contextService.getByCode(contextCode);
        if (context == null) {
            return error("No such context.");
        }

        List<ImageResolution> imageResolutions = contextService.getImageResolutions(context.getId());

        if (configurableOnly) {
            removeNonConfigurableResolutions(imageResolutions);
        }

        return success(imageResolutionDtoList(imageResolutions));
    }

    private void removeNonConfigurableResolutions(List<ImageResolution> imageResolutions) {
        Iterator<ImageResolution> iterator = imageResolutions.iterator();

        while (iterator.hasNext()) {
            ImageResolution resolution = iterator.next();

            if (!resolution.isConfigurable()) {
                iterator.remove();
            }
        }
    }

    @RequestMapping(value = "/" + LIST_MODIFICATIONS, method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse listModifications(@RequestParam(value = "token", required = true) String accessToken,
                                          @RequestParam(value = "iid", required = true) String externalId,
                                          @RequestParam(value = "context", required = true) String contextCode) {
        if (!this.accessToken.equals(accessToken)) {
            return error("Access denied.");
        }

        Context context = contextService.getByCode(contextCode);
        if (context == null) {
            return error("No such context.");
        }

        Image image = imageService.getByExternalId(externalId);
        if (image == null) {
            return error(String.format("No image available for identifier %s.", externalId));
        }

        List<ImageModification> modifications = imageService.getModifications(image.getId(), context.getId());

        return success(toModificationDtos(modifications));
    }

    private List<ImageModificationDto> toModificationDtos(List<ImageModification> modifications) {
        List<ImageModificationDto> dtos = new ArrayList<>(modifications.size());
        for (ImageModification modification : modifications) {
            dtos.add(toDto(modification));
        }
        return dtos;
    }

    private ImageModificationDto toDto(ImageModification modification) {
        ImageResolution resolution = imageService.getResolution(modification.getResolutionId());
        ImageModificationDto dto = new ImageModificationDto();
        dto.getResolution().setWidth(resolution.getWidth());
        dto.getResolution().setHeight(resolution.getHeight());
        dto.getCrop().setX(modification.getCrop().getX());
        dto.getCrop().setY(modification.getCrop().getY());
        dto.getCrop().setWidth(modification.getCrop().getWidth());
        dto.getCrop().setHeight(modification.getCrop().getHeight());
        dto.getDensity().setWidth(modification.getDensity().getWidth());
        dto.getDensity().setHeight(modification.getDensity().getHeight());
        return dto;
    }

    private List<ImageResolutionDto> imageResolutionDtoList(List<ImageResolution> imageResolutions) {
        List<ImageResolutionDto> dtos = new ArrayList<>(imageResolutions.size());
        for (ImageResolution imageResolution : imageResolutions) {
            ImageResolutionDto dto = new ImageResolutionDto();
            dto.setWidth(imageResolution.getWidth());
            dto.setHeight(imageResolution.getHeight());
            dto.setConfigurable(imageResolution.isConfigurable());
            dto.setName(imageResolution.getName());
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
