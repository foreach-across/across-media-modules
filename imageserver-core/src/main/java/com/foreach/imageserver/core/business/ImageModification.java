package com.foreach.imageserver.core.business;

import com.foreach.imageserver.core.web.dto.ImageModificationDto;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Specifies a single set of modifications to be done to an image.
 */
public class ImageModification {

    public static final ImageModification EMPTY = new ImageModification();
    private static final ImageModification EMPTY_WITH_STRETCH;

    static {
        EMPTY_WITH_STRETCH = new ImageModification();
        EMPTY_WITH_STRETCH.getVariant().setStretch(true);
    }

    private Crop crop = new Crop();
    private ImageVariant variant = new ImageVariant();

    public ImageModification() {

    }

    public ImageModification(ImageVariant variant, Crop crop) {
        this.crop = crop ;
        this.variant = variant;
    }

    public ImageModification normalize(Dimensions dimensions) {
        if (dimensions.getHeight() < 0 || dimensions.getWidth() < 0) {
            throw new RuntimeException("Illegal dimensions!");
        }
        ImageModification result = new ImageModification();
        if (this.isEmpty()) {
            return result;
        }
        Crop scaledCrop = crop.normalize(dimensions);
        result.setCrop(scaledCrop);
        Dimensions minDimensions;
        if (!scaledCrop.isEmpty()) {
            minDimensions = new Dimensions(Math.min(dimensions.getWidth(), scaledCrop.getWidth()), Math.min(dimensions.getHeight(), scaledCrop.getHeight()));
        } else {
            minDimensions = dimensions;
        }
        result.setVariant(variant.normalize(minDimensions));
        return result;
    }


    public boolean isEmpty() {
        return this.equals(EMPTY) || this.equals(EMPTY_WITH_STRETCH);
    }

    public Crop getCrop() {
        return crop;
    }


    public void setCrop(Crop crop) {
        this.crop = crop;
    }

    public boolean hasCrop() {
        return crop != null && !crop.isEmpty();
    }

    public ImageVariant getVariant() {
        return variant;
    }

    public void setVariant(ImageVariant variant) {
        this.variant = variant;
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

        ImageModification modification = (ImageModification) o;

        return ObjectUtils.equals(crop, modification.getCrop()) && ObjectUtils.equals(this.variant, modification.getVariant());
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(crop).append(variant).toHashCode();
    }

    @Override
    public String toString() {
        return "ImageModification{" +
                "variant=" + variant +
                ", crop=" + crop +
                '}';
    }
}
