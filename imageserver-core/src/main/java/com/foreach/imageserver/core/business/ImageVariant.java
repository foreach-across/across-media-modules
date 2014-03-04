package com.foreach.imageserver.core.business;

/**
 * Describes a variant to be created from the original image
 */
public class ImageVariant {

    private int width, height;
    private ImageType output;
    private boolean stretch;
    private boolean keepAspect;
    private Dimensions density = new Dimensions();

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

    public ImageType getOutput() {
        return output;
    }

    public void setOutput(ImageType output) {
        this.output = output;
    }

    public boolean isStretch() {
        return stretch;
    }

    public void setStretch(boolean stretch) {
        this.stretch = stretch;
    }

    public boolean isKeepAspect() {
        return keepAspect;
    }

    public void setKeepAspect(boolean keepAspect) {
        this.keepAspect = keepAspect;
    }

    /**
     * Density is the lowest possible numbers to be used as multiplier on the original
     * as to achieve the ideal original dimensions for the output requested.
     *
     * @return Horizontal (width) and vertical (height) density.
     */
    public Dimensions getDensity() {
        return density;
    }

    public void setDensity(Dimensions density) {
        this.density = density;
    }

    public void setDensity(int density) {
        setDensity(density, density);
    }

    public void setDensity(int horizontal, int vertical) {
        density.setWidth(horizontal);
        density.setHeight(vertical);
    }

    public boolean isOnlyDimensions() {
        ImageVariant other = new ImageVariant();
        other.setStretch(stretch);
        other.setKeepAspect(keepAspect);
        other.setWidth(width);
        other.setHeight(height);

        return this.equals(other);
    }

    /**
     * Will normalize the variant based on the dimensions of the original passed in.
     *
     * @param dimensions Dimensions of the original image.
     * @return Normalized ImageVariant fitting the original image.
     */
    public ImageVariant normalize(Dimensions dimensions) {
        ImageVariant normalized = new ImageVariant();
        adjustWidthAndHeight(normalized, dimensions);
        normalized.setOutput(output);
        normalized.setDensity(density);
        calculateDensity(normalized, dimensions);
        return normalized;
    }

    private void calculateDensity(ImageVariant normalized, Dimensions original) {
        if (Dimensions.EMPTY.equals(normalized.getDensity())) {
            Dimensions calculated = new Dimensions();

            int requestedWidth = normalized.getWidth();
            int requestedHeight = normalized.getHeight();

            int originalWidth = original.getWidth();
            int originalHeight = original.getHeight();

            if (originalWidth >= requestedWidth) {
                calculated.setWidth(1);
            } else {
                calculated.setWidth(Double.valueOf(Math.ceil(requestedWidth / (double) originalWidth)).intValue());
            }
            if (originalHeight >= requestedHeight) {
                calculated.setHeight(1);
            } else {
                calculated.setHeight(Double.valueOf(Math.ceil(requestedHeight / (double) originalHeight)).intValue());
            }

            normalized.setDensity(calculated);
        }
    }

    private void adjustWidthAndHeight(ImageVariant normalized, Dimensions maxDimensions) {
        normalized.setStretch(stretch);

        Dimensions dimensionsToUse = new Dimensions();
        dimensionsToUse.setWidth(width);
        dimensionsToUse.setHeight(height);

        if (maxDimensions != null && maxDimensions.getWidth() > 0 && maxDimensions.getHeight() > 0) {
            dimensionsToUse = dimensionsToUse.normalize(maxDimensions);

            if (keepAspect) {
                dimensionsToUse = dimensionsToUse.normalize(maxDimensions.getAspectRatio());
            }
            if (!stretch) {
                dimensionsToUse = dimensionsToUse.scaleToFitIn(maxDimensions);
            }
        }

        normalized.setWidth(dimensionsToUse.getWidth());
        normalized.setHeight(dimensionsToUse.getHeight());
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

        ImageVariant variant = (ImageVariant) o;

        if (height != variant.height) {
            return false;
        }
        if (stretch != variant.stretch) {
            return false;
        }
        if (width != variant.width) {
            return false;
        }
        if (output != variant.output) {
            return false;
        }

        if (density != null && !Dimensions.EMPTY.equals(
                density) && variant.density != null && !Dimensions.EMPTY.equals(variant.density)) {
            return density.equals(variant.density);
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = width;
        result = 31 * result + height;
        result = 31 * result + (output != null ? output.hashCode() : 0);
        result = 31 * result + (stretch ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ImageVariant{" +
                "width=" + width +
                ", height=" + height +
                ", output=" + output +
                ", stretch=" + stretch +
                '}';
    }
}
