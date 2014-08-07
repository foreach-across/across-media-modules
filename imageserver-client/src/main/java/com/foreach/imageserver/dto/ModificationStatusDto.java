package com.foreach.imageserver.dto;

public class ModificationStatusDto {
    private String imageId;
    private boolean modified;

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }
}
