package com.foreach.imageserver.dto;

public class ImageSaveResultDto {

    private int imageId;
    private DimensionsDto dimensionsDto;

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public DimensionsDto getDimensionsDto() {
        return dimensionsDto;
    }

    public void setDimensionsDto(DimensionsDto dimensionsDto) {
        this.dimensionsDto = dimensionsDto;
    }
}
