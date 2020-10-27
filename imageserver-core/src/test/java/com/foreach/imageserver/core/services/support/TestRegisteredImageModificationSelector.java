package com.foreach.imageserver.core.services.support;

import com.foreach.imageserver.core.business.ImageModification;
import org.junit.jupiter.api.Test;

/**
 * @author Arne Vandamme
 */
public class TestRegisteredImageModificationSelector extends AbstractImageModficationSelectorTest
{
	@Override
	protected ImageModificationSelector createSelector() {
		return new RegisteredImageModificationSelector();
	}

	@Test
	public void noModificationRegistered() {
		assertSelected( null );
	}

	@Test
	public void modificationLinkedToResolutionId() {
		modification( resolution( 2L, 800, 600 ) );
		ImageModification expected = modification( resolution( 1L, 200, 300 ) );

		requested.setId( 1L );

		assertSelected( expected );
	}

	@Test
	public void firstModificationForDimensionBasedResolution() {
		ImageModification expected = modification( resolution( 2L, 800, 600 ) );
		modification( resolution( 1L, 200, 300 ) );
		modification( resolution( 3L, 800, 600 ) );

		assertSelected( expected );
	}

	@Test
	public void noMatchingModifications() {
		modification( resolution( 2L, 1200, 600 ) );
		modification( resolution( 1L, 200, 300 ) );
		modification( resolution( 3L, 800, 400 ) );

		assertSelected( null );
	}
}
