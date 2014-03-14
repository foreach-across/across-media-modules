package com.foreach.imageserver.core.business;

import com.foreach.imageserver.core.services.WebOriginalImageRepository;

public class WebOriginalImage implements OriginalImage {
    private final WebOriginalImageParameters parameters;

    public WebOriginalImage(WebOriginalImageParameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public int getId() {
        return parameters.getId();
    }

    @Override
    public String getRepositoryCode() {
        return WebOriginalImageRepository.CODE;
    }

    @Override
    public ImageType getImageType() {
        return parameters.getImageType();
    }

    @Override
    public Dimensions getDimensions() {
        return parameters.getDimensions();
    }

    @Override
    public String getUniqueFileName() {
        return parameters.getId() + "." + getImageType().getExtension();
    }
}
