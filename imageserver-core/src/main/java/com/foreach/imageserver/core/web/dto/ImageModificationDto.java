package com.foreach.imageserver.core.web.dto;

import com.foreach.imageserver.core.business.Crop;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageType;

public class ImageModificationDto {

    private Crop crop = new Crop();
    private Dimensions density = new Dimensions();
    private int width, height;
    private ImageType output;
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
}
