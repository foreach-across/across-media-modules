package com.foreach.imageserver.core.services.repositories;

import com.foreach.imageserver.core.business.ImageType;

import java.io.InputStream;

public final class RepositoryLookupResult {
    private ImageType imageType;

    private RepositoryLookupStatus status;
    private InputStream content;

    public RepositoryLookupStatus getStatus() {
        return status;
    }

    public void setStatus(RepositoryLookupStatus status) {
        this.status = status;
    }

    public ImageType getImageType() {
        return imageType;
    }

    public void setImageType(ImageType imageType) {
        this.imageType = imageType;
    }

    public InputStream getContent() {
        return content;
    }

    public void setContent(InputStream content) {
        this.content = content;
    }

    public boolean isSuccess() {
        return getStatus() == RepositoryLookupStatus.SUCCESS;
    }
}
