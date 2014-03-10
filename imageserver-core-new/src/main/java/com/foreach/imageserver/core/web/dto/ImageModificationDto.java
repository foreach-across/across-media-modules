package com.foreach.imageserver.core.web.dto;

import com.foreach.imageserver.core.business.Crop;
import com.foreach.imageserver.core.business.Dimensions;

public class ImageModificationDto {

    private Crop crop = new Crop();
    private Dimensions density = new Dimensions();
    private boolean stretch = false;
    private boolean keepAspect = true;

    public Crop getCrop() {
        return crop;
    }

    public void setCrop(Crop crop) {
        this.crop = crop;
    }

    public Dimensions getDensity() {
        return density;
    }

    public void setDensity(Dimensions density) {
        this.density = density;
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
}
