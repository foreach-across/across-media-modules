package com.foreach.imageserver.core.business;

import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * The ImageModification specifies how an original image is to be transformed into an image conforming to a specific
 * ImageResolution.
 * <p/>
 * Note that an ImageModification is generic; the non-generic options required for an actual transform are specified
 * using an ImageVariant object.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_DEFAULT)
public class ImageModification {
    private Integer id;
    private int imageId;
    private int resolutionId;
    private Crop crop;
    private boolean stretch;
    private boolean keepAspect;
    private Dimensions density;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public int getResolutionId() {
        return resolutionId;
    }

    public void setResolutionId(int resolutionId) {
        this.resolutionId = resolutionId;
    }

    public Crop getCrop() {
        return crop;
    }

    public void setCrop(Crop crop) {
        this.crop = crop;
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

    public Dimensions getDensity() {
        return density;
    }

    public void setDensity(Dimensions density) {
        this.density = density;
    }
}