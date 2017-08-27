package com.foreach.imageserver.core.services.support;

import com.foreach.imageserver.core.business.Crop;
import com.foreach.imageserver.core.business.ImageModification;
import org.junit.Test;

/**
 * @author Arne Vandamme
 */
public class TestAdaptingImageModificationSelector extends AbstractImageModficationSelectorTest
{
	@Override
	protected ImageModificationSelector createSelector() {
		return new AdaptingImageModificationSelector();
	}

	@Test
	public void noModificationRegistered() {
		assertSelected( null );
	}

	@Test
	public void extendedCropGoesOutsideBox() {
		modification( resolution( 2, 800, 400 ), new Crop( 0, 0, 800, 400 ) );
		modification( resolution( 3, 800, 560 ), new Crop( 0, 0, 800, 560 ) );

		assertSelected( null );
	}

	@Test
	public void extendCrop() {
		modification( resolution( 2, 800, 400 ), new Crop( 0, 0, 800, 400 ) );
		modification( resolution( 3, 800, 560 ), new Crop( 0, 100, 800, 560 ) );

		ImageModification expected = new ImageModification();
		expected.setResolutionId( 3 );
		expected.setCrop( new Crop( 0, 80, 800, 600 ) );

		assertSelected( expected );
	}

	@Test
	public void shrinkCrop() {
		modification( resolution( 2, 800, 900 ), new Crop( 0, 0, 800, 900 ) );
		modification( resolution( 3, 800, 640 ), new Crop( 0, 0, 800, 640 ) );

		ImageModification expected = new ImageModification();
		expected.setResolutionId( 3 );
		expected.setCrop( new Crop( 0, 20, 800, 600 ) );

		assertSelected( expected );
	}

	@Test
	public void smallerCropDoesNotHaveEnoughDatapoints() {
		modification( resolution( 2, 800, 900 ), new Crop( 0, 0, 400, 450 ) );
		modification( resolution( 3, 800, 640 ), new Crop( 0, 0, 400, 320 ) );

		assertSelected( null );
	}
}
