package com.foreach.imageserver.core.business;

import java.util.Date;

/**
 * TODO Re-document this after I'm through refactoring.
 */
public class Image {
    private int imageId;
    private Date dateCreated;
    private String repositoryCode;

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getRepositoryCode() {
        return repositoryCode;
    }

    public void setRepositoryCode(String repositoryCode) {
        this.repositoryCode = repositoryCode;
    }
}