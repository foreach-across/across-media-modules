package com.foreach.imageserver.services.crop;

import com.foreach.imageserver.business.geometry.Size;
import com.foreach.imageserver.business.image.Crop;
import com.foreach.imageserver.business.math.Fraction;

import java.util.Set;

public interface CropMatcher
{
    Crop bestCropFrom( Set<Crop> crops, int versionRequested, Size sizeRequested );

    Crop bestCropFrom( Set<Crop> crops, int version, Fraction aspectRatio, int width  );
}
