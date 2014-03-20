package com.foreach.imageserver.dto;

public class ImageModificationDto {
    private CropDto crop = new CropDto();
    private DimensionsDto density = new DimensionsDto();

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
