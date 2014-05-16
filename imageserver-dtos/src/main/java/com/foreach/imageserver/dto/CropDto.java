package com.foreach.imageserver.dto;

/**
 * Source dimensions are the assumed dimensions of the  original image.  Basically they define the
 * scale of the coordinate system.  If not set, the original dimensions of the target image are assumed unless a
 * box is defined.  In the latter case the box will be used to scale down the original and then use those coordinates
 * as source.
 */
public class CropDto {
    private int x;
    private int y;
    private int width;
    private int height;

    // Source for the coordinates
    private DimensionsDto source = new DimensionsDto();

    // Box wrapping the original source
    // If box and source are specified, source is used.  If only box is specified, then the source is calculated
    // based on the box
    private DimensionsDto box = new DimensionsDto();

    public CropDto() {
    }

    public CropDto(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
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

    public DimensionsDto getSource() {
        return source;
    }

    public void setSource(DimensionsDto source) {
        this.source = source;
    }

    public DimensionsDto getBox() {
        return box;
    }

    public void setBox(DimensionsDto box) {
        this.box = box;
    }

    public boolean hasBox() {
        return box != null && !box.equals(new DimensionsDto());
    }

    public boolean hasSource() {
        return source != null && !source.equals(new DimensionsDto());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CropDto)) return false;

        CropDto cropDto = (CropDto) o;

        if (height != cropDto.height) return false;
        if (width != cropDto.width) return false;
        if (x != cropDto.x) return false;
        if (y != cropDto.y) return false;
        if (box != null ? !box.equals(cropDto.box) : cropDto.box != null) return false;
        if (source != null ? !source.equals(cropDto.source) : cropDto.source != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + width;
        result = 31 * result + height;
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (box != null ? box.hashCode() : 0);
        return result;
    }
}
