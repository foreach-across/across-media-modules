package com.foreach.imageserver.dto;

public class ImageModificationDto {
    private CropDto crop = new CropDto();
    private DimensionsDto density = new DimensionsDto();

    public ImageModificationDto() {
    }

    public ImageModificationDto(CropDto crop, DimensionsDto density) {
        this.crop = crop;
        this.density = density;
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
