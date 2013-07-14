package com.foreach.imageserver.dao.selectors;

import com.foreach.imageserver.business.Fraction;

public class CropSelector extends AbstractSelector {

    private Long imageId;
    private Fraction aspectRatio;
    private Integer targetWidth;
    private Integer version;

    public final Long getImageId() {
        return imageId;
    }

    public final void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    public final Fraction getAspectRatio() {
        return aspectRatio;
    }

    public final void setAspectRatio(Fraction aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public final Integer getTargetWidth() {
        return targetWidth;
    }

    public final void setTargetWidth(Integer targetWidth) {
        this.targetWidth = targetWidth;
    }

    public final Integer getVersion() {
        return version;
    }

    public final void setVersion(Integer version) {
        this.version = version;
    }

    public static final CropSelector uniqueCrop( long imageId, Fraction aspectRatio, int targetWidth, int version )
    {
        CropSelector selector = new CropSelector();
        selector.setImageId( imageId );
        selector.setAspectRatio( aspectRatio );
        selector.setTargetWidth( targetWidth );
        selector.setVersion( version );
        return selector;
    }

	public static final CropSelector onImageId( long imageId )
	{
	    CropSelector selector = new CropSelector();
	    selector.setImageId( imageId );
	    return selector;
	}


    @Override
    public final boolean equals( Object o )
    {
        if ( this == o ) {
            return true;
        }
        if ( o == null || getClass() != o.getClass() ) {
            return false;
        }

        CropSelector other = (CropSelector) o;

        if ( !nullSafeCompare( imageId, other.imageId ) ) {
            return false;
        }
        if ( !nullSafeCompare( aspectRatio, other.aspectRatio ) ) {
            return false;
        }
        if ( !nullSafeCompare( targetWidth, other.targetWidth ) ) {
            return false;
        }
        if ( !nullSafeCompare( version, other.version ) ) {
            return false;
        }

        return true;
    }

    @Override
    public final int hashCode()
    {
        int result = ( imageId != null ) ? imageId.hashCode() : 0;
        result = addMultiplyHash(result, aspectRatio);
        result = addMultiplyHash(result, targetWidth);
        result = addMultiplyHash(result, version);
        return result;
    }

}
