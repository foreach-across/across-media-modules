package com.foreach.imageserver.dto;

public class ImageModificationDto {
    private ImageResolutionDto resolution;
    private CropDto crop;
    private DimensionsDto density;

    public ImageModificationDto() {
        this.resolution = new ImageResolutionDto();
        this.crop = new CropDto();
        this.density = new DimensionsDto();
    }

    public ImageModificationDto(ImageResolutionDto resolution, CropDto crop, DimensionsDto density) {
        this.resolution = resolution;
        this.crop = crop;
        this.density = density;
    }

    public ImageResolutionDto getResolution() {
        return resolution;
    }

    public void setResolution(ImageResolutionDto resolution) {
        this.resolution = resolution;
    }

    public CropDto getCrop() {
        return crop;
    }

    public void setCrop(CropDto crop) {
        this.crop = crop;
    }

    public DimensionsDto getDensity() {
        return density;
    }

    public void setDensity(DimensionsDto density) {
        this.density = density;
    }
}
