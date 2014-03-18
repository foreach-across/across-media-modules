package com.foreach.imageserver.core.business;

import com.foreach.imageserver.core.services.WebImageRepository;

public class WebImageParameters implements OriginalImage {
    private int id;
    private String url;
    private Dimensions dimensions;
    private ImageType imageType;

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String getRepositoryCode() {
        return WebImageRepository.CODE;
    }

    @Override
    public ImageType getImageType() {
        return imageType;
    }

    public void setImageType(ImageType imageType) {
        this.imageType = imageType;
    }

    @Override
    public Dimensions getDimensions() {
        return dimensions;
    }

    public void setDimensions(Dimensions dimensions) {
        this.dimensions = dimensions;
    }

    @Override
    public String getUniqueFileName() {
        return getId() + "." + getImageType().getExtension();
    }
}
