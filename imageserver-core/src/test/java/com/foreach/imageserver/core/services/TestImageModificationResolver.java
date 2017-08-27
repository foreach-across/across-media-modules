package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageContext;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.services.support.ImageModificationSelector;
import com.foreach.imageserver.dto.CropDto;
import com.foreach.imageserver.dto.ImageModificationDto;
import com.foreach.imageserver.dto.ImageResolutionDto;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

/**
 * @author Arne Vandamme
 */
@RunWith(MockitoJUnitRunner.class)
public class TestImageModificationResolver
{
	private ImageModificationResolver resolver;
	private Image image;
	private ImageContext context;
	private ImageModificationDto modification;

	@Mock
	private CropGeneratorUtil cropGeneratorUtil;

	@Mock
	private ImageModificationSelector one;

	@Mock
	private ImageModificationSelector two;

	@Before
	public void before() {
		ImageModificationResolverImpl r = new ImageModificationResolverImpl();
		r.setCropGeneratorUtil( cropGeneratorUtil );

		doAnswer( invocationOnMock -> {
			ImageResolution resolution = invocationOnMock.getArgumentAt( 1, ImageResolution.class );
			return new Dimensions( resolution.getWidth(), resolution.getHeight() );
		} ).when( cropGeneratorUtil ).applyResolution( any( Image.class ), any( ImageResolution.class ) );

		resolver = r;

		image = new Image();
		image.setId( 1 );
		image.setDimensions( new Dimensions( 4000, 2000 ) );

		context = new ImageContext();
		context.setId( 1 );

		modification = null;
	}

	@Ignore
	@Test
	public void defaultModificationIsNormalizedWithoutCrop() {
		requestModification( 4000, 1000 );
		assertResolution( 4000, 1000 );
		assertNull( modification.getCrop() );
	}

	@Test
	public void modificationLinkedToResolutionIsReturned() {

	}

	@Test
	public void modificationLinkedToResolutionTakesPrecedence() {

	}

	private void requestModification( int width, int height ) {
		ImageResolution resolution = new ImageResolution();
		resolution.setId( 1 );
		resolution.setWidth( width );
		resolution.setHeight( height );

		this.modification = resolver.resolveModification( image, context, resolution );

		verify( cropGeneratorUtil ).normalizeModificationDto( image, modification );
	}

	private void assertCrop( int x, int y, int width, int height ) {
		CropDto crop = new CropDto( x, y, width, height );
		assertEquals( crop, modification.getCrop() );
	}

	private void assertResolution( int width, int height ) {
		assertEquals( new ImageResolutionDto( width, height ), modification.getResolution() );
	}
}
