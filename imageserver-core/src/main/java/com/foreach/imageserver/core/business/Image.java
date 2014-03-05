package com.foreach.imageserver.core.business;

import java.util.Date;

public class Image {
    private int id, applicationId;
    private String key, filePath;
    private Dimensions dimensions = new Dimensions();
    private long fileSize;
    private ImageType imageType;
    private Date dateCreated = new Date();
    private Date dateUpdated;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Dimensions getDimensions() {
        return dimensions;
    }

    public void setDimensions(Dimensions dimensions) {
        this.dimensions = dimensions;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
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

    public ImageType getImageType() {
        return imageType;
    }

    public void setImageType(ImageType imageType) {
        this.imageType = imageType;
    }

    public String toString() {
        return "Image[id=" + id + ",aid=" + applicationId + "]";
    }

    public boolean equals(Object other) {
        if (other instanceof Image) {
            Image otherImage = (Image) other;
            return getId() == otherImage.getId() && getApplicationId() == otherImage.getApplicationId();
        } else {
            return false;
        }
    }
}
