package com.foreach.imageserver.core.business;

import org.codehaus.jackson.annotate.JsonIgnore;

public class Crop {
    private int x, y, width, height, sourceWidth, sourceHeight;

    public Crop() {
    }

    public Crop(int x, int y, int width, int height) {
        this(x, y, width, height, 0, 0);
    }

    public Crop(int x, int y, int width, int height, int sourceWidth, int sourceHeight) {
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

    @JsonIgnore
    public boolean isEmpty() {
        return height <= 0 || width <= 0;
    }

    public Dimensions getDimensions() {
        return new Dimensions(width, height);
    }

    /**
     * Snaps a crop to match with the dimensions specified (points exceeding dimensions are snapped to boundaries).
     */
    public Crop normalize(Dimensions dimensions) {
        int leftX = this.x;
        int leftY = this.y;
        int rightX = leftX + this.width;
        int rightY = leftY + this.height;

        Dimensions source = determineSourceDimensions(dimensions);

        leftX = snap(leftX, source.getWidth());
        leftY = snap(leftY, source.getHeight());
        rightX = snap(rightX, source.getWidth());
        rightY = snap(rightY, source.getHeight());

        if (!dimensions.equals(source)) {
            double modX = (double) dimensions.getWidth() / source.getWidth();
            double modY = (double) dimensions.getHeight() / source.getHeight();

            return new Crop(Double.valueOf(leftX * modX).intValue(), Double.valueOf(leftY * modY).intValue(),
                    Double.valueOf((rightX - leftX) * modX).intValue(),
                    Double.valueOf((rightY - leftY) * modY).intValue(), dimensions.getWidth(),
                    dimensions.getHeight());
        }

        return new Crop(leftX, leftY, rightX - leftX, rightY - leftY, sourceWidth, sourceHeight);
    }

    private Dimensions determineSourceDimensions(Dimensions dimensions) {
        if (sourceWidth > 0 || sourceHeight > 0) {
            Fraction aspectRatio = dimensions.getAspectRatio();

            int sw = sourceWidth <= 0 ? aspectRatio.calculateWidthForHeight(sourceHeight) : sourceWidth;
            int sh = sourceHeight <= 0 ? aspectRatio.calculateHeightForWidth(sourceWidth) : sourceHeight;

            return new Dimensions(sw, sh);
        }

        return dimensions;
    }

    private int snap(int pos, int max) {
        if (pos < 0) {
            return 0;
        } else if (pos > max) {
            return max;
        }
        return pos;
    }

    @SuppressWarnings("all")
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Crop crop = (Crop) o;

        if (height != crop.height) {
            return false;
        }
        if (sourceHeight != crop.sourceHeight) {
            return false;
        }
        if (sourceWidth != crop.sourceWidth) {
            return false;
        }
        if (width != crop.width) {
            return false;
        }
        if (x != crop.x) {
            return false;
        }
        if (y != crop.y) {
            return false;
        }

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

    @Override
    public String toString() {
        return "Crop{" +
                "x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                ", sourceWidth=" + sourceWidth +
                ", sourceHeight=" + sourceHeight +
                '}';
    }
}
