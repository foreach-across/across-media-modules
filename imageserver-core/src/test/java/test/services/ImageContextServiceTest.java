package test.services;

import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.managers.ImageResolutionManager;
import com.foreach.imageserver.core.services.ImageContextService;
import com.foreach.imageserver.core.services.ImageContextServiceImpl;
import com.foreach.imageserver.math.AspectRatio;
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
		imageResolutions.add( createImageResolution( 1L, 10, 20 ) );
		imageResolutions.add( createImageResolution( 2L, 20, 20 ) );
		imageResolutions.add( createImageResolution( 3L, 30, 20 ) );
		imageResolutions.add( createImageResolution( 4L, 40, 20 ) );
		imageResolutions.add( createImageResolution( 5L, 50, 20 ) );

		imageResolutions.add( createImageResolution( 6L, 10, 50 ) );
		imageResolutions.add( createImageResolution( 7L, 20, 50 ) );
		imageResolutions.add( createImageResolution( 8L, 30, 50 ) );
		imageResolutions.add( createImageResolution( 9L, 40, 50 ) );

		imageResolutions.add( createImageResolution( 10L, 50, 0 ) );

		imageResolutions.add( createImageResolution( 11L, 10, 70 ) );
		imageResolutions.add( createImageResolution( 12L, 20, 70 ) );
		imageResolutions.add( createImageResolution( 13L, 30, 70 ) );
		imageResolutions.add( createImageResolution( 14L, 40, 70 ) );
		imageResolutions.add( createImageResolution( 15L, 50, 70 ) );

		imageResolutions.add( createImageResolution( 16L, 45, 0 ) );
		imageResolutions.add( createImageResolution( 17L, 0, 45 ) );

		imageResolutions.add( createImageResolution( 18L, 200, 400 ) );
		imageResolutions.add( createImageResolution( 19L, 400, 800 ) );
		imageResolutions.add( createImageResolution( 20L, 800, 1600 ) );

		imageResolutions.add( createImageResolution( 21L, 640, 480 ) );

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
		assertEquals( 8L, imageResolution.getId().longValue() );
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
		assertEquals( 10L, imageResolution.getId().longValue() );
	}

	@Test
	public void getImageResolution_WidthNoHeightAllowed() {
		ImageResolution imageResolution = contextService.getImageResolution( CONTEXT_ID, 45, 0 ); //-> 45,0
		assertNotNull( imageResolution );
		assertEquals( 16L, imageResolution.getId().longValue() );
	}

	@Test
	public void getImageResolution_HeightNoWidthAllowed() {
		ImageResolution imageResolution = contextService.getImageResolution( CONTEXT_ID, 0, 45 ); //-> 0,45
		assertNotNull( imageResolution );
		assertEquals( 17L, imageResolution.getId().longValue() );
	}

	@Test
	public void getImageResolution_NoWidthNoHeight() {
		ImageResolution imageResolution = contextService.getImageResolution( CONTEXT_ID, 0, 0 );
		assertNull( imageResolution );
	}

	@Test
	public void getImageResolution_ForUnknownRatio() {
		ImageResolution imageResolution =
				contextService.getImageResolution( CONTEXT_ID, new AspectRatio( "16/3" ), 200 );
		assertNull( imageResolution );
	}

	@Test
	public void getImageResolution_ForKnownRatio() {
		AspectRatio half = new AspectRatio( "1/2" );
		ImageResolution imageResolution = contextService.getImageResolution( CONTEXT_ID, half, 100 );
		assertEquals( 18L, imageResolution.getId().longValue() );
		imageResolution = contextService.getImageResolution( CONTEXT_ID, half, 400 );
		assertEquals( 19L, imageResolution.getId().longValue() );
		imageResolution = contextService.getImageResolution( CONTEXT_ID, half, 401 );
		assertEquals( 20L, imageResolution.getId().longValue() );
		imageResolution = contextService.getImageResolution( CONTEXT_ID, half, 9000 );
		assertEquals( 20L, imageResolution.getId().longValue() );
		imageResolution = contextService.getImageResolution( CONTEXT_ID, new AspectRatio( "4/3" ), 400 );
		assertEquals( 21L, imageResolution.getId().longValue() );
	}

	private ImageResolution createImageResolution( Long id, int width, int height ) {
		ImageResolution imageResolution = new ImageResolution();
		imageResolution.setId( id );
		imageResolution.setWidth( width );
		imageResolution.setHeight( height );
		return imageResolution;
	}
}
