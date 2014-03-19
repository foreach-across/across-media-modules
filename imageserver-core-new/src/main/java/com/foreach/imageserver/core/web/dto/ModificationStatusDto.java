package com.foreach.imageserver.core.web.dto;

public class ModificationStatusDto {
    private int imageId;
    private boolean hasCrop;

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public boolean isHasCrop() {
        return hasCrop;
    }

    public void setHasModification(boolean hasCrop) {
        this.hasCrop = hasCrop;
    }
}
