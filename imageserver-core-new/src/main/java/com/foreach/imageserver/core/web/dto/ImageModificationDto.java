package com.foreach.imageserver.core.web.dto;

import com.foreach.imageserver.core.business.Crop;
import com.foreach.imageserver.core.business.Dimensions;

public class ImageModificationDto {

    private Crop crop = new Crop();
    private Dimensions density = new Dimensions();

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
}
