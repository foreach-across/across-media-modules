package com.foreach.imageserver.core.business;

public class Dimensions {
    public static final Dimensions EMPTY = new Dimensions();

    private int width;
    private int height;

    public Dimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Dimensions() {
    }

    public Dimensions(Dimensions dimensions) {
        this.width = dimensions.getWidth();
        this.height = dimensions.getHeight();
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

    public boolean fitsIn(Dimensions boundaries) {
        return getWidth() <= boundaries.getWidth() && getHeight() <= boundaries.getHeight();
    }

    /**
     * Will calculate the unknown dimensions according to the boundaries specified.
     * Any unknown dimensions will be scaled according to the aspect ratio of the boundaries.
     */
    public Dimensions normalize(Dimensions boundaries) {
        Dimensions normalized = new Dimensions(width, height);

        Fraction originalAspectRatio = boundaries.getAspectRatio();

        if (width == 0 && height == 0) {
            normalized.setWidth(boundaries.getWidth());
            normalized.setHeight(boundaries.getHeight());
        } else if (height == 0) {
            normalized.setHeight(originalAspectRatio.calculateHeightForWidth(width));
        } else if (width == 0) {
            normalized.setWidth(originalAspectRatio.calculateWidthForHeight(height));
        }

        return normalized;
    }

    /**
     * Will verify and modify the dimensions so that they match the aspect ratio.
     * The largest dimension according to aspect ratio is kept.
     */
    public Dimensions normalize(Fraction aspectRatio) {
        Dimensions normalized = new Dimensions(width, height);

        if (!normalized.getAspectRatio().equals(aspectRatio)) {
            if (aspectRatio.getNumerator() > aspectRatio.getDenominator()) {
                normalized.setHeight(aspectRatio.calculateHeightForWidth(normalized.getWidth()));
            } else {
                normalized.setWidth(aspectRatio.calculateWidthForHeight(normalized.getHeight()));
            }
        }

        return normalized;
    }

    /**
     * Will downscale the dimensions to fit in the boundaries if they are larger.
     */
    public Dimensions scaleToFitIn(Dimensions boundaries) {
        Dimensions normalized = normalize(boundaries);
        Dimensions scaled = new Dimensions(normalized.getWidth(), normalized.getHeight());

        Fraction aspectRatio = normalized.getAspectRatio();

        if (!normalized.fitsIn(boundaries)) {
            if (normalized.getAspectRatio().isLargerOnWidth()) {
                scaled.setWidth(boundaries.getWidth());
                scaled.setHeight(aspectRatio.calculateHeightForWidth(boundaries.getWidth()));
            } else {
                scaled.setHeight(boundaries.getHeight());
                scaled.setWidth(aspectRatio.calculateWidthForHeight(boundaries.getHeight()));
            }

            if (!scaled.fitsIn(boundaries)) {
                // Reverse the side as scaling basis, we made the wrong decision
                if (normalized.getAspectRatio().isLargerOnWidth()) {
                    scaled.setHeight(boundaries.getHeight());
                    scaled.setWidth(aspectRatio.calculateWidthForHeight(boundaries.getHeight()));
                } else {
                    scaled.setWidth(boundaries.getWidth());
                    scaled.setHeight(aspectRatio.calculateHeightForWidth(boundaries.getWidth()));

                }
            }
        }

        return scaled;
    }

    public Fraction getAspectRatio() {
        return new Fraction(width, height);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Dimensions that = (Dimensions) o;

        if (height != that.height) {
            return false;
        }
        if (width != that.width) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = width;
        result = 31 * result + height;
        return result;
    }

    @Override
    public String toString() {
        return width + "x" + height;
    }
}
