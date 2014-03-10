package com.foreach.imageserver.core.web.dto;

import com.foreach.imageserver.core.business.ImageType;

public class ImageVariantDto {

    private ImageType imageType;

    public ImageType getImageType() {
        return imageType;
    }

    public void setImageType(ImageType imageType) {
        this.imageType = imageType;
    }
}
