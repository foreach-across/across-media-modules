package com.foreach.imageserver.core.business;

import java.util.Date;

/**
 * TODO Re-document this after I'm through refactoring.
 */
public class Image {
    private int imageId;
    private int applicationId;
    private Date dateCreated;
    private String repositoryCode;
    private int originalImageId;

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
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

    public int getOriginalImageId() {
        return originalImageId;
    }

    public void setOriginalImageId(int originalImageId) {
        this.originalImageId = originalImageId;
    }
}