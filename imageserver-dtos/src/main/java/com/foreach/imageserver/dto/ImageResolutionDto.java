package com.foreach.imageserver.dto;

public class ImageResolutionDto {
    private Integer width;
    private Integer height;

    public ImageResolutionDto() {
    }

    public ImageResolutionDto(Integer width, Integer height) {
        this.width = width;
        this.height = height;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }
}
