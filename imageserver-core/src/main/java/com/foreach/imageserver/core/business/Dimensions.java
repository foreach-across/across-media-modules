package com.foreach.imageserver.core.business;

/**
 * A simple object to specify the actual dimensions of an Image.
 * <p/>
 * Not to be confused with the ImageResolution, which specifies a permitted output resolution which may be unrestricted
 * for a certain dimension.
 */
public class Dimensions {
    private int width;
    private int height;

    public Dimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Dimensions() {
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
