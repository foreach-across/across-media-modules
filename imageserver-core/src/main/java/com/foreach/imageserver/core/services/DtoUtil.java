package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Crop;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.dto.CropDto;
import com.foreach.imageserver.dto.DimensionsDto;
import com.foreach.imageserver.dto.ImageResolutionDto;

// TODO: review to simply business and dto objects layer
public final class DtoUtil {

    public static Crop toBusiness(CropDto dto) {
        Crop crop = new Crop();
        crop.setX(dto.getX());
        crop.setY(dto.getY());
        crop.setWidth(dto.getWidth());
        crop.setHeight(dto.getHeight());
        return crop;
    }

    public static Dimensions toBusiness(DimensionsDto dto) {
        Dimensions dimensions = new Dimensions();
        dimensions.setWidth(dto.getWidth());
        dimensions.setHeight(dto.getHeight());
        return dimensions;
    }

    public static CropDto toDto(Crop crop) {
        return new CropDto(crop.getX(), crop.getY(), crop.getWidth(), crop.getHeight());
    }

    public static ImageResolutionDto toDto(ImageResolution resolution) {
        return new ImageResolutionDto(resolution.getWidth(), resolution.getHeight());
    }
}
