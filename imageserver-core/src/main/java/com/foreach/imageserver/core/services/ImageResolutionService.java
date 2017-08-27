package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Crop;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageModification;

import java.util.List;

/**
 * @author rvd
 * @since 11/08/16.
 */
public interface ImageResolutionService {

	Crop findBestMatchingCropBasedOnResolution( Dimensions dimensions, List<ImageModification> modificationList );

}
