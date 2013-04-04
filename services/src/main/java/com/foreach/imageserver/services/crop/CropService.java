package com.foreach.imageserver.services.crop;

import com.foreach.imageserver.business.image.Crop;
import com.foreach.imageserver.business.math.Fraction;

public interface CropService
{
    Crop getCropById( long id );

    // These criteria match 0 or 1 crop (unique constraint Crop.AK_crop)
    Crop getCrop(long imageid, Fraction ratio, int targetWidth, int version);

    void saveCrop( Crop crop );

	void deleteCrop( Crop crop );

    Integer getMaxVersion( long imageId );
}
