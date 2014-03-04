package com.foreach.imageserver.core.business;

import java.util.Date;

public class StoredImageVariant {

    private int imageId;
    private ImageVariant variant = new ImageVariant();
    private Date dateCreated;
    private Date dateUpdated;

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public ImageVariant getVariant() {
        return variant;
    }

    public void setVariant(ImageVariant variant) {
        this.variant = variant;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }
}
