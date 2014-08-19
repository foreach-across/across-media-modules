package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.ImageContext;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.managers.ImageContextManager;
import com.foreach.imageserver.core.managers.ImageResolutionManager;
import com.foreach.imageserver.dto.ImageContextDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class ImageContextServiceImpl implements ImageContextService
{
	private static final Logger LOG = LoggerFactory.getLogger( ImageContextServiceImpl.class );

	// default image resolution width/height proportion
	public static final double DEFAULT_ASPECT_RATIO = 3.0 / 2.0;

	@Autowired
	private ImageContextManager contextManager;

	@Autowired
	private ImageResolutionManager imageResolutionManager;

	@Override
	public ImageContext getByCode( String contextCode ) {
		return contextManager.getByCode( contextCode );
	}

	/**
	 * Provides an image resolution that exactly matches the given dimensions, if it exists.
	 *
	 * @param contextId The id of the context
	 * @param width     The wanted image resolution width
	 * @param height    The wanted image resolution height
	 * @return An image resolution
	 */
	@Override
	public ImageResolution getImageResolution( long contextId, int width, int height ) {
		if ( width < 0 || height < 0 ) {
			return null;
		}

		List<ImageResolution> imageResolutions = imageResolutionManager.getForContext( contextId );
		for ( ImageResolution imageResolution : imageResolutions ) {
			// Exact fit will return immediately
			if ( imageResolution.getWidth() == width && imageResolution.getHeight() == height ) {
				return imageResolution;
			}
		}

		// no matching image resolution was found
		return null;
	}

	@Override
	public List<ImageResolution> getImageResolutions( long contextId ) {
		return new ArrayList<>( imageResolutionManager.getForContext( contextId ) );
	}

	@Override
	public Collection<ImageContext> getAllContexts() {
		return new ArrayList<>( contextManager.getAllContexts() );
	}

	@Override
	public void save( ImageContextDto contextDto ) {
		contextManager.save( contextDto );
	}
}
