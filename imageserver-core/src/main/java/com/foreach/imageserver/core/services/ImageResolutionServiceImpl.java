package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Crop;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageModification;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.managers.ImageResolutionManager;
import com.foreach.imageserver.math.AspectRatio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author rvd
 * @since 11/08/16.
 */
@Service
public class ImageResolutionServiceImpl implements ImageResolutionService {

	private ImageResolutionManager imageResolutionManager;

	@Override
	public Crop findBestMatchingCropBasedOnResolution( Dimensions dimensions, List<ImageModification> modificationList ) {
		AspectRatio requestedRatio = new AspectRatio( dimensions.getHeight(), dimensions.getWidth() );
		for ( ImageModification modification : modificationList ) {
			ImageResolution imageResolution = imageResolutionManager.getById( modification.getResolutionId() );
			// check dimensions
			AspectRatio image = new AspectRatio( imageResolution.getHeight(), imageResolution.getWidth() );
			if ( requestedRatio.equals( image ) ) {
				// same ratio
				if ( imageResolution.getHeight() >= dimensions.getHeight() && imageResolution.getWidth() >= dimensions.getWidth() ) {
					return modification.getCrop();
				}
			}
		}
		return null;
	}

	//
	// getters and setters
	//

	@Autowired
	public void setImageResolutionManager( ImageResolutionManager imageResolutionManager ) {
		this.imageResolutionManager = imageResolutionManager;
	}

}
