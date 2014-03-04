package com.foreach.imageserver.core.business;

import com.foreach.imageserver.core.web.dto.ImageModifierDto;
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

    public ImageModification(ImageModifierDto imageModifierDto) {
        crop.setX(imageModifierDto.getCrop().getX());
        crop.setY(imageModifierDto.getCrop().getY());
        crop.setWidth(imageModifierDto.getCrop().getWidth());
        crop.setHeight(imageModifierDto.getCrop().getHeight());
        crop.setSourceHeight(imageModifierDto.getCrop().getSourceHeight());
        crop.setSourceWidth(imageModifierDto.getCrop().getSourceWidth());
        variant.setHeight(imageModifierDto.getHeight());
        variant.setWidth(imageModifierDto.getWidth());
        variant.setDensity(new Dimensions(imageModifierDto.getDensity()));
        variant.setKeepAspect(imageModifierDto.isKeepAspect());
        variant.setStretch(imageModifierDto.isStretch());
        variant.setOutput(imageModifierDto.getOutput());
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

        ImageModification modifier = (ImageModification) o;

        return ObjectUtils.equals(crop, modifier.getCrop()) && ObjectUtils.equals(this.variant, modifier.getVariant());
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
