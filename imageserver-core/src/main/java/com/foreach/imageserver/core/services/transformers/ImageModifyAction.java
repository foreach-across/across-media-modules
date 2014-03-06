package com.foreach.imageserver.core.services.transformers;

import com.foreach.imageserver.core.business.ImageFile;
import com.foreach.imageserver.core.business.ImageModification;

public class ImageModifyAction extends ImageTransformerAction<ImageFile> {
    private final ImageModification modification;

    public ImageModifyAction(ImageFile original, ImageModification modification) {
        super(original);
        this.modification = modification;
    }

    public ImageModification getModification() {
        return modification;
    }

    @SuppressWarnings("all")
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ImageModifyAction that = (ImageModifyAction) o;

        if (modification != null ? !modification.equals(that.modification) : that.modification != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (modification != null ? modification.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ImageModifyAction{" +
                "original=" + getImageFile() +
                ", variant=" + modification +
                '}';
    }
}
