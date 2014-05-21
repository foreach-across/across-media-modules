package com.foreach.imageserver.core.business;

import com.foreach.imageserver.dto.ImageModificationDto;

/**
 * Specific default modification attached to an image profile.
 */
public class ImageProfileModification {
    private int id;
    private int imageResolutionId;
    private int imageProfileId;
    private int imageContextId;
    private ImageModificationDto modificationDto = new ImageModificationDto();

    public int getImageResolutionId() {
        return imageResolutionId;
    }

    public void setImageResolutionId(int imageResolutionId) {
        this.imageResolutionId = imageResolutionId;
    }

    public int getImageProfileId() {
        return imageProfileId;
    }

    public void setImageProfileId(int imageProfileId) {
        this.imageProfileId = imageProfileId;
    }

    public int getImageContextId() {
        return imageContextId;
    }

    public void setImageContextId(int imageContextId) {
        this.imageContextId = imageContextId;
    }

    public ImageModificationDto getModificationDto() {
        return modificationDto;
    }

    public void setModificationDto(ImageModificationDto modificationDto) {
        this.modificationDto = modificationDto;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImageProfileModification that = (ImageProfileModification) o;

        if (id != that.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
