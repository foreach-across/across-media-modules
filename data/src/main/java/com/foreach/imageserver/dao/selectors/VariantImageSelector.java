package com.foreach.imageserver.dao.selectors;

import java.util.Date;

public class VariantImageSelector extends AbstractSelector {

    private Long imageId;
    private Integer width, height, version, formatId;
    private Date calledAfterThisDate;

    public final Long getImageId() {
        return imageId;
    }

    public final void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    public final Integer getWidth() {
        return width;
    }

    public final void setWidth(Integer width) {
        this.width = width;
    }

    public final Integer getHeight() {
        return height;
    }

    public final void setHeight(Integer height) {
        this.height = height;
    }

    public final Integer getVersion() {
        return version;
    }

    public final void setVersion(Integer version) {
        this.version = version;
    }

    public final Date getCalledAfterThisDate() {
        return calledAfterThisDate;
    }

    public final void setCalledAfterThisDate(Date calledAfterThisDate) {
        this.calledAfterThisDate = calledAfterThisDate;
    }

    public final Integer getFormatId() {
        return formatId;
    }

    public final void setFormatId(Integer formatId) {
        this.formatId = formatId;
    }

    public static final VariantImageSelector onNothing() {
        return new VariantImageSelector();
    }

    public static final VariantImageSelector onImageIdAndWidthAndHeightAndVersion(Long imageId, Integer width, Integer height, Integer version) {
        VariantImageSelector selector = new VariantImageSelector();
        selector.setImageId(imageId);
        selector.setHeight(height);
        selector.setWidth(width);
        selector.setVersion(version);
        return selector;
    }

    public static final VariantImageSelector onCalledAfterThisDate(Date calledAfterThisDate) {
        VariantImageSelector selector = new VariantImageSelector();
        selector.setCalledAfterThisDate(calledAfterThisDate);
        return selector;
    }

    public static final VariantImageSelector onFormatId(Integer formatId) {
        VariantImageSelector selector = new VariantImageSelector();
        selector.setFormatId(formatId);
        return selector;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        VariantImageSelector other = (VariantImageSelector) o;

        if (nullSafeCompare(imageId, other.imageId)) {
            return false;
        }
        if (nullSafeCompare(width, other.width)) {
            return false;
        }
        if (nullSafeCompare(height, other.height)) {
            return false;
        }
        if (nullSafeCompare(version, other.version)) {
            return false;
        }
        if (nullSafeCompare(calledAfterThisDate, other.calledAfterThisDate)) {
            return false;
        }

        return true;
    }

    @Override
    @SuppressWarnings("all")
    public final int hashCode() {
        Integer result = addMultiplyHash(0, imageId);
        result = addMultiplyHash(result, width == null ? 0 : width.hashCode());
        result = addMultiplyHash(result, height == null ? 0 : height.hashCode());
        result = addMultiplyHash(result, version == null ? 0 : version.hashCode());
        result = addMultiplyHash(result, calledAfterThisDate == null ? 0 : calledAfterThisDate.hashCode());
        return result;
    }

}
