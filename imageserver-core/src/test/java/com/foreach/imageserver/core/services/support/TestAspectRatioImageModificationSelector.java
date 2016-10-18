package com.foreach.imageserver.core.services.support;

import com.foreach.imageserver.core.business.Crop;
import com.foreach.imageserver.core.business.ImageModification;
import org.junit.Test;

/**
 * @author Arne Vandamme
 */
public class TestAspectRatioImageModificationSelector extends AbstractImageModficationSelectorTest
{
	@Override
	protected ImageModificationSelector createSelector() {
		return new AspectRatioImageModificationSelector();
	}

	@Test
	public void noModificationRegistered() {
		assertSelected( null );
	}

	@Test
	public void noMatchingAspectRatioModifications() {
		modification( resolution( 2, 1200, 600 ) );
		modification( resolution( 1, 200, 300 ) );
		modification( resolution( 3, 800, 400 ) );

		assertSelected( null );
	}

	@Test
	public void singleLargerModificationWithSameAspectRatio() {
		modification( resolution( 2, 1200, 600 ) );
		ImageModification expected = modification( resolution( 3, 1600, 1200 ) );

		assertSelected( expected );
	}

	@Test
	public void smallestLargerModificationIsReturned() {
		modification( resolution( 2, 1200, 600 ) );
		modification( resolution( 4, 3200, 2400 ) );
		ImageModification expected = modification( resolution( 3, 1600, 1200 ) );

		assertSelected( expected );

		before();

		modification( resolution( 2, 1200, 600 ) );
		expected = modification( resolution( 4, 1200, 900 ) );
		modification( resolution( 3, 1600, 1200 ) );

		assertSelected( expected );
	}

	@Test
	public void largestSmallerModificationWithEnoughDatapointsIsReturned() {
		modification( resolution( 2, 1200, 600 ) );
		modification( resolution( 3, 600, 300 ) );
		modification( resolution( 4, 200, 150 ), new Crop( 0, 0, 8000, 6000 ) );
		ImageModification expected = modification( resolution( 5, 400, 300 ), new Crop( 0, 0, 8000, 6000 ) );

		assertSelected( expected );

		before();

		modification( resolution( 2, 1200, 600 ) );
		modification( resolution( 3, 600, 300 ) );
		expected = modification( resolution( 4, 200, 150 ), new Crop( 0, 0, 8000, 6000 ) );
		modification( resolution( 5, 400, 300 ), new Crop( 0, 0, 400, 400 ) );

		assertSelected( expected );
	}

	@Test
	public void noSmallerModificationWithEnoughDatapoints() {
		modification( resolution( 2, 1200, 600 ) );
		modification( resolution( 3, 600, 300 ) );
		modification( resolution( 4, 200, 150 ), new Crop( 0, 0, 400, 400 ) );
		modification( resolution( 5, 400, 300 ), new Crop( 0, 0, 400, 400 ) );

		assertSelected( null );
	}
}
