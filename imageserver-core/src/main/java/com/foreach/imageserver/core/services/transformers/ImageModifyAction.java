package com.foreach.imageserver.core.services.transformers;

import com.foreach.imageserver.core.business.ImageFile;
import com.foreach.imageserver.core.business.ImageModification;

public class ImageModifyAction extends ImageTransformerAction<ImageFile> {
    private final ImageModification variant;

    public ImageModifyAction(ImageFile original, ImageModification variant) {
        super(original);
        this.variant = variant;
    }

    public ImageModification getVariant() {
        return variant;
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

        if (variant != null ? !variant.equals(that.variant) : that.variant != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (variant != null ? variant.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ImageModifyAction{" +
                "original=" + getImageFile() +
                ", variant=" + variant +
                '}';
    }
}
