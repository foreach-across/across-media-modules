package com.foreach.imageserver.core.services.support;

import com.foreach.imageserver.core.business.Crop;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageModification;
import com.foreach.imageserver.dto.ImageResolutionDto;
import org.junit.Before;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

/**
 * @author Arne Vandamme
 */
abstract class AbstractImageModficationSelectorTest
{
	protected Image image;
	protected ImageModificationSelector selector;

	protected ImageResolutionDto requested;
	protected Map<ImageModification, ImageResolutionDto> candidates;

	@Before
	public void before() {
		image = new Image();
		image.setDimensions( new Dimensions( 1600, 1200 ) );

		selector = createSelector();
		candidates = new LinkedHashMap<>();
		requested = new ImageResolutionDto( 800, 600 );
	}

	protected abstract ImageModificationSelector createSelector();

	protected void assertSelected( ImageModification modification ) {
		assertEquals( Optional.ofNullable( modification ),
		              selector.selectImageModification( image, candidates, requested ) );
	}

	protected ImageResolutionDto resolution( int resolutionId, int width, int height ) {
		ImageResolutionDto r = new ImageResolutionDto();
		r.setWidth( width );
		r.setHeight( height );
		r.setId( resolutionId );

		return r;
	}

	protected ImageModification modification( ImageResolutionDto resolution ) {
		return modification( resolution, null );
	}

	protected ImageModification modification( ImageResolutionDto resolution, Crop crop ) {
		ImageModification modification = new ImageModification();
		modification.setResolutionId( resolution.getId() );
		modification.setCrop( crop );

		candidates.put( modification, resolution );

		return modification;
	}
}
