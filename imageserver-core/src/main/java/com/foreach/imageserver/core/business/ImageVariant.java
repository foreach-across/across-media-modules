package com.foreach.imageserver.core.business;

import com.foreach.imageserver.core.web.dto.ImageModifierDto;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Specifies a single set of modifications to be done to an image.
 */
public class ImageVariant {

    public static final ImageVariant EMPTY = new ImageVariant();
    private static final ImageVariant EMPTY_WITH_STRETCH;

    static {
        EMPTY_WITH_STRETCH = new ImageVariant();
        EMPTY_WITH_STRETCH.getModifier().setStretch(true);
    }

    private Crop crop = new Crop();
    private ImageModifier modifier = new ImageModifier();

    public ImageVariant() {

    }

    public ImageVariant(ImageModifierDto imageModifierDto) {
        crop.setX(imageModifierDto.getCrop().getX());
        crop.setY(imageModifierDto.getCrop().getY());
        crop.setWidth(imageModifierDto.getCrop().getWidth());
        crop.setHeight(imageModifierDto.getCrop().getHeight());
        crop.setSourceHeight(imageModifierDto.getCrop().getSourceHeight());
        crop.setSourceWidth(imageModifierDto.getCrop().getSourceWidth());
        modifier.setHeight(imageModifierDto.getHeight());
        modifier.setWidth(imageModifierDto.getWidth());
        modifier.setDensity(new Dimensions(imageModifierDto.getDensity()));
        modifier.setKeepAspect(imageModifierDto.isKeepAspect());
        modifier.setStretch(imageModifierDto.isStretch());
        modifier.setOutput(imageModifierDto.getOutput());
    }

    public ImageVariant normalize(Dimensions dimensions) {
        if (dimensions.getHeight() < 0 || dimensions.getWidth() < 0) {
            throw new RuntimeException("Illegal dimensions!");
        }
        ImageVariant result = new ImageVariant();
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
        result.setModifier(modifier.normalize(minDimensions));
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

    public ImageModifier getModifier() {
        return modifier;
    }

    public void setModifier(ImageModifier modifier) {
        this.modifier = modifier;
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

        ImageVariant modifier = (ImageVariant) o;

        return ObjectUtils.equals(crop, modifier.getCrop()) && ObjectUtils.equals(this.modifier, modifier.getModifier());
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(crop).append(modifier).toHashCode();
    }

    @Override
    public String toString() {
        return "ImageVariant{" +
                "modifier=" + modifier +
                ", crop=" + crop +
                '}';
    }
}
