package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Crop;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageModification;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.managers.ImageResolutionManager;
import com.foreach.imageserver.math.AspectRatio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author rvd
 * @since 11/08/16.
 */
@Service
public class ImageResolutionServiceImpl implements ImageResolutionService
{
	private ImageResolutionManager imageResolutionManager;

	@Override
	public Crop findBestMatchingCropBasedOnResolution( Dimensions dimensions, List<ImageModification> modificationList ) {
		AspectRatio requestedRatio = new AspectRatio( dimensions.getHeight(), dimensions.getWidth() );

		Map<Crop, ImageResolution> cropsThatFit = new HashMap<>();

		for ( ImageModification modification : modificationList ) {
			imageResolutionManager.getById( modification.getResolutionId() )
			                      .ifPresent( imageResolution -> {
				                      // check dimensions
				                      AspectRatio image = new AspectRatio( imageResolution.getHeight(), imageResolution.getWidth() );
				                      if ( requestedRatio.equals( image ) ) {
					                      // collect crops with the same ratio
					                      cropsThatFit.put( modification.getCrop(), imageResolution );
				                      }
			                      } );

		}

		// when there are no matching crops, return null
		if ( cropsThatFit.isEmpty() ) {
			return null;
		}

		// Convert the map to a 'List', order by ascending resolution
		List<Map.Entry<Crop, ImageResolution>> sorted = cropsThatFit.entrySet().stream().sorted( Map.Entry.comparingByValue(
				( a, b ) -> a.getHeight() * a.getWidth() - b.getHeight() * b.getWidth() ) )
		                                                            .collect( Collectors.toList() );

		// return the first crop that has a higher resolution then requested
		// if not possible, return the crop with the highest resolution
		return sorted.stream().filter( e -> e.getValue().getHeight() >= dimensions.getHeight() || e.getValue().getWidth() >= dimensions.getWidth() )
		             .findFirst().orElse( sorted.get( sorted.size() - 1 ) ).getKey();
	}

	//
	// getters and setters
	//

	@Autowired
	public void setImageResolutionManager( ImageResolutionManager imageResolutionManager ) {
		this.imageResolutionManager = imageResolutionManager;
	}

}
