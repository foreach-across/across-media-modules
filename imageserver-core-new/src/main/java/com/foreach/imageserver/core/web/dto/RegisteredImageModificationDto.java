package com.foreach.imageserver.core.web.dto;

public class RegisteredImageModificationDto {

    private ImageResolutionDto resolution;
    private ImageModificationDto modification;

    public ImageModificationDto getModification() {
        return modification;
    }

    public void setModification(ImageModificationDto modification) {
        this.modification = modification;
    }

    public ImageResolutionDto getResolution() {
        return resolution;
    }

    public void setResolution(ImageResolutionDto resolution) {
        this.resolution = resolution;
    }
}