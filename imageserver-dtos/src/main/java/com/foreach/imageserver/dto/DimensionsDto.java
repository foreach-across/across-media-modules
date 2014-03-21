package com.foreach.imageserver.dto;

public class DimensionsDto {
    private int width;
    private int height;

    public DimensionsDto() {
    }

    public DimensionsDto(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
