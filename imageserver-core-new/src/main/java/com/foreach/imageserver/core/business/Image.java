package com.foreach.imageserver.core.business;

import java.util.Date;

/**
 * An original source image from which transformations are created.
 * <p/>
 * Note that we expect original images to be re-retrievable should they be removed from the filesystem. For this
 * purpose, we store the identifier of the ImageRepository that was used to retrieve the material. We expect an
 * ImageRepository plugin to do its own bookkeeping so that it can retrieve the source image based on the image id and
 * application id.
 */
public class Image {
    private int imageId;
    private int applicationId;
    private Dimensions dimensions;
    private ImageType imageType;
    private Date dateCreated;
    private String repositoryCode;

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

    public Dimensions getDimensions() {
        return dimensions;
    }

    public void setDimensions(Dimensions dimensions) {
        this.dimensions = dimensions;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public ImageType getImageType() {
        return imageType;
    }

    public void setImageType(ImageType imageType) {
        this.imageType = imageType;
    }

    public String getRepositoryCode() {
        return repositoryCode;
    }

    public void setRepositoryCode(String repositoryCode) {
        this.repositoryCode = repositoryCode;
    }
}