package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.managers.ImageResolutionManager;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ImageContextServiceTest
{

	public static final int CONTEXT_ID = 2;
	private ImageContextService contextService;

	@Before
	public void setUp() {
		contextService = new ImageContextServiceImpl();
		ImageResolutionManager imageResolutionManager = mock( ImageResolutionManager.class );

		List<ImageResolution> imageResolutions = new ArrayList<ImageResolution>();
		imageResolutions.add( createImageResolution( 1, 10, 20 ) );
		imageResolutions.add( createImageResolution( 2, 20, 20 ) );
		imageResolutions.add( createImageResolution( 3, 30, 20 ) );
		imageResolutions.add( createImageResolution( 4, 40, 20 ) );
		imageResolutions.add( createImageResolution( 5, 50, 20 ) );

		imageResolutions.add( createImageResolution( 6, 10, 50 ) );
		imageResolutions.add( createImageResolution( 7, 20, 50 ) );
		imageResolutions.add( createImageResolution( 8, 30, 50 ) );
		imageResolutions.add( createImageResolution( 9, 40, 50 ) );

		imageResolutions.add( createImageResolution( 10, 50, 0 ) );

		imageResolutions.add( createImageResolution( 11, 10, 70 ) );
		imageResolutions.add( createImageResolution( 12, 20, 70 ) );
		imageResolutions.add( createImageResolution( 13, 30, 70 ) );
		imageResolutions.add( createImageResolution( 14, 40, 70 ) );
		imageResolutions.add( createImageResolution( 15, 50, 70 ) );

		imageResolutions.add( createImageResolution( 16, 45, 0 ) );
		imageResolutions.add( createImageResolution( 17, 0, 45 ) );

		doReturn( imageResolutions ).when( imageResolutionManager ).getForContext( CONTEXT_ID );
		ReflectionTestUtils.setField( contextService, "imageResolutionManager", imageResolutionManager );
	}

	@Test
	public void getImageResolution_forWidthHeight() {
		ImageResolution imageResolution = contextService.getImageResolution( CONTEXT_ID, 28, 48 ); //-> 30,50
		assertNull( imageResolution );
	}

	@Test
	public void getImageResolution_ExactWidthHeight() {
		ImageResolution imageResolution = contextService.getImageResolution( CONTEXT_ID, 30, 48 ); //-> null
		assertNull( imageResolution );
	}

	@Test
	public void getImageResolution_ExactWidthExactHeight() {
		ImageResolution imageResolution = contextService.getImageResolution( CONTEXT_ID, 30, 50 ); //-> 30x50
		assertNotNull( imageResolution );
		assertEquals( 8, (int) imageResolution.getId() );
	}

	@Test
	public void getImageResolution_TooBigWidthHeight() {
		ImageResolution imageResolution = contextService.getImageResolution( CONTEXT_ID, 70, 48 ); //-> null
		assertNull( imageResolution );
	}

	@Test
	public void getImageResolution_TooBigWidthExactHeight() {
		ImageResolution imageResolution = contextService.getImageResolution( CONTEXT_ID, 70, 50 ); //-> null
		assertNull( imageResolution );
	}

	@Test
	public void getImageResolution_WidthAndTooBigHeight() {
		ImageResolution imageResolution = contextService.getImageResolution( CONTEXT_ID, 30, 80 ); //-> null
		assertNull( imageResolution );
	}

	@Test
	public void getImageResolution_ExactWidthAndTooBigHeight() {
		ImageResolution imageResolution = contextService.getImageResolution( CONTEXT_ID, 30, 80 ); //-> null
		assertNull( imageResolution );
	}

	@Test
	public void getImageResolution_WidthNoHeightNonExistent() {
		ImageResolution imageResolution = contextService.getImageResolution( CONTEXT_ID, 48, 0 ); //-> null
		assertNull( imageResolution );
	}

	@Test
	public void getImageResolution_ExactWidthNoHeight() {
		ImageResolution imageResolution = contextService.getImageResolution( CONTEXT_ID, 50, 0 ); //-> 50,0
		assertNotNull( imageResolution );
		assertEquals( 10, (int) imageResolution.getId() );
	}

	@Test
	public void getImageResolution_WidthNoHeightAllowed() {
		ImageResolution imageResolution = contextService.getImageResolution( CONTEXT_ID, 45, 0 ); //-> 45,0
		assertNotNull( imageResolution );
		assertEquals( 16, (int) imageResolution.getId() );
	}

	@Test
	public void getImageResolution_HeightNoWidthAllowed() {
		ImageResolution imageResolution = contextService.getImageResolution( CONTEXT_ID, 0, 45 ); //-> 0,45
		assertNotNull( imageResolution );
		assertEquals( 17, (int) imageResolution.getId() );
	}

	@Test
	public void getImageResolution_NoWidthNoHeight() {
		ImageResolution imageResolution = contextService.getImageResolution( CONTEXT_ID, 0, 0 );
		assertNull( imageResolution );
	}

	private ImageResolution createImageResolution( int id, int width, int height ) {
		ImageResolution imageResolution = new ImageResolution();
		imageResolution.setId( id );
		imageResolution.setWidth( width );
		imageResolution.setHeight( height );
		return imageResolution;
	}
}
