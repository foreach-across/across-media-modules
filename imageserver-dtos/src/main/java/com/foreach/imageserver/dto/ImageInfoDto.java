package com.foreach.imageserver.dto;

import java.util.Date;

public class ImageInfoDto {
    private boolean existing;
    private String externalId;
    private Date created;
    private ImageTypeDto imageType;
    private DimensionsDto dimensionsDto;

    public ImageInfoDto() {
    }

    public boolean isExisting() {
        return existing;
    }

    public void setExisting(boolean existing) {
        this.existing = existing;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public ImageTypeDto getImageType() {
        return imageType;
    }

    public void setImageType(ImageTypeDto imageType) {
        this.imageType = imageType;
    }

    public DimensionsDto getDimensionsDto() {
        return dimensionsDto;
    }

    public void setDimensionsDto(DimensionsDto dimensionsDto) {
        this.dimensionsDto = dimensionsDto;
    }
}
