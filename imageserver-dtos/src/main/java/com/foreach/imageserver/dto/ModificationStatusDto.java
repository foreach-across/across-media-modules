package com.foreach.imageserver.dto;

public class ModificationStatusDto {
    private int imageId;
    private boolean modified;

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }
}
