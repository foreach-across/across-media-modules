package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageContext;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.services.support.ImageModificationSelector;
import com.foreach.imageserver.dto.CropDto;
import com.foreach.imageserver.dto.ImageModificationDto;
import com.foreach.imageserver.dto.ImageResolutionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;

/**
 * @author Arne Vandamme
 */
@ExtendWith(MockitoExtension.class)
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

	@BeforeEach
	public void before() {
		ImageModificationResolverImpl r = new ImageModificationResolverImpl();
		r.setCropGeneratorUtil( cropGeneratorUtil );
//
//		doAnswer( invocationOnMock -> {
//			ImageResolution resolution = invocationOnMock.getArgument( 1 );
//			return new Dimensions( resolution.getWidth(), resolution.getHeight() );
//		} ).when( cropGeneratorUtil ).applyResolution( any( Image.class ), any( ImageResolution.class ) );

		resolver = r;

		image = new Image();
		image.setId( 1L );
		image.setDimensions( new Dimensions( 4000, 2000 ) );

		context = new ImageContext();
		context.setId( 1L );

		modification = null;
	}

	@Disabled
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
		resolution.setId( 1L );
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
