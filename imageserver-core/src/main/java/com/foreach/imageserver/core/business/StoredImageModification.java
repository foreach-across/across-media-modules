package com.foreach.imageserver.core.business;

import java.util.Date;

public class StoredImageModification {

    private int imageId;
    private ImageModification modification = new ImageModification();
    private Date dateCreated;
    private Date dateUpdated;

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public ImageModification getModification() {
        return modification;
    }

    public void setModification(ImageModification modification) {
        this.modification = modification;
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
