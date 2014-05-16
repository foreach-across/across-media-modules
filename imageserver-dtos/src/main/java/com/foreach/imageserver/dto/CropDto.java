package com.foreach.imageserver.dto;

/**
 * Source dimensions are the assumed dimensions of the  original image.  Basically they define the
 * scale of the coordinate system.  If not set, the original dimensions of the target image are assumed.
 */
public class CropDto {
    private int x;
    private int y;
    private int width;
    private int height;
    private int sourceWidth;
    private int sourceHeight;

    public CropDto() {
    }

    public CropDto(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public CropDto(int x, int y, int width, int height, int sourceWidth, int sourceHeight) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.sourceWidth = sourceWidth;
        this.sourceHeight = sourceHeight;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
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

    public int getSourceWidth() {
        return sourceWidth;
    }

    public void setSourceWidth(int sourceWidth) {
        this.sourceWidth = sourceWidth;
    }

    public int getSourceHeight() {
        return sourceHeight;
    }

    public void setSourceHeight(int sourceHeight) {
        this.sourceHeight = sourceHeight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CropDto)) return false;

        CropDto cropDto = (CropDto) o;

        if (height != cropDto.height) return false;
        if (sourceHeight != cropDto.sourceHeight) return false;
        if (sourceWidth != cropDto.sourceWidth) return false;
        if (width != cropDto.width) return false;
        if (x != cropDto.x) return false;
        if (y != cropDto.y) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + width;
        result = 31 * result + height;
        result = 31 * result + sourceWidth;
        result = 31 * result + sourceHeight;
        return result;
    }
}
