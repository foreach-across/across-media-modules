package test.client;

import com.foreach.imageserver.client.ImageRequestHashBuilder;
import com.foreach.imageserver.client.ImageServerClient;
import com.foreach.imageserver.client.ImageServerException;
import com.foreach.imageserver.core.client.LocalImageServerClient;
import com.foreach.imageserver.core.rest.request.ListResolutionsRequest;
import com.foreach.imageserver.core.rest.response.ListResolutionsResponse;
import com.foreach.imageserver.core.rest.services.ImageRestService;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.dto.DimensionsDto;
import com.foreach.imageserver.dto.ImageResolutionDto;
import com.foreach.imageserver.dto.ImageTypeDto;
import com.foreach.imageserver.dto.ImageVariantDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = TestLocalImageServerClient.Config.class)
public class TestLocalImageServerClient
{
	@Autowired
	private ImageRestService imageRestService;

	@Autowired
	private LocalImageServerClient imageServerClient = new LocalImageServerClient( "http://localhost:8078/" );

	@Before
	public void before() {
		imageServerClient.setImageRequestHashBuilder( null );
	}

	@Test
	public void imageUrlWithoutHash() {
		String url = imageServerClient.imageUrl( "10", "ONLINE", 1000, 2000, ImageTypeDto.TIFF );
		assertEquals(
				"http://localhost:8078/view?iid=10&context=ONLINE&width=1000&height=2000&imageType=TIFF",
				url );

		url = imageServerClient.imageUrl( "10", "DIGITAL", 0, 2000, ImageTypeDto.TIFF );
		assertEquals(
				"http://localhost:8078/view?iid=10&context=DIGITAL&height=2000&imageType=TIFF",
				url );

		url = imageServerClient.imageUrl( "someid", "SITE", 1000, 0, ImageTypeDto.TIFF );
		assertEquals(
				"http://localhost:8078/view?iid=someid&context=SITE&width=1000&imageType=TIFF",
				url );

		url = imageServerClient.imageUrl( "internal:1", "TABLET", "3/2", 200, ImageTypeDto.JPEG, 100, 1000 );
		assertEquals(
				"http://localhost:8078/view?iid=internal:1&context=TABLET&ratio=3/2&width=200&imageType=JPEG&boundaries.width=100&boundaries.height=1000",
				url );
	}

	@Test
	public void hashIsAppendedIfHashBuilderAdded() {
		ImageRequestHashBuilder hashBuilder = mock( ImageRequestHashBuilder.class );
		imageServerClient.setImageRequestHashBuilder( hashBuilder );

		when(
				hashBuilder.calculateHash( "ONLINE",
				                           null,
				                           new ImageResolutionDto( 1000, 2000 ),
				                           new ImageVariantDto( ImageTypeDto.TIFF ) )
		).thenReturn( "one" );
		String url = imageServerClient.imageUrl( "10", "ONLINE", 1000, 2000, ImageTypeDto.TIFF );
		assertEquals(
				"http://localhost:8078/view?iid=10&context=ONLINE&width=1000&height=2000&imageType=TIFF&hash=one",
				url );

		when(
				hashBuilder.calculateHash( "DIGITAL",
				                           null,
				                           new ImageResolutionDto( 0, 2000 ),
				                           new ImageVariantDto( ImageTypeDto.TIFF ) )
		).thenReturn( "two" );
		url = imageServerClient.imageUrl( "10", "DIGITAL", 0, 2000, ImageTypeDto.TIFF );
		assertEquals(
				"http://localhost:8078/view?iid=10&context=DIGITAL&height=2000&imageType=TIFF&hash=two",
				url );

		when(
				hashBuilder.calculateHash( "SITE",
				                           null,
				                           new ImageResolutionDto( 1000, 0 ),
				                           new ImageVariantDto( ImageTypeDto.TIFF ) )
		).thenReturn( "three" );
		url = imageServerClient.imageUrl( "someid", "SITE", 1000, 0, ImageTypeDto.TIFF );
		assertEquals(
				"http://localhost:8078/view?iid=someid&context=SITE&width=1000&imageType=TIFF&hash=three",
				url );

		ImageVariantDto variant = new ImageVariantDto( ImageTypeDto.JPEG );
		variant.setBoundaries( new DimensionsDto( 100, 1000 ) );
		when(
				hashBuilder.calculateHash( "TABLET",
				                           "3/2",
				                           new ImageResolutionDto( 200, 0 ),
				                           variant )
		).thenReturn( "four" );
		url = imageServerClient.imageUrl( "internal:1", "TABLET", "3/2", 200, ImageTypeDto.JPEG, 100, 1000 );
		assertEquals(
				"http://localhost:8078/view?iid=internal:1&context=TABLET&ratio=3/2&width=200&imageType=JPEG&boundaries.width=100&boundaries.height=1000&hash=four",
				url );
	}

	@Test
	public void imageUrlWithOutImageTypeWillReturnImageTypeOfOriginal() {
		String url = imageServerClient.imageUrl( "10", "ONLINE", 1000, 2000 );
		assertEquals(
				"http://localhost:8078/view?iid=10&context=ONLINE&width=1000&height=2000",
				url );
	}

	@Test
	public void listResolutionsContextNotFoundThrowsException() {
		ListResolutionsResponse response = new ListResolutionsResponse();
		response.setContextDoesNotExist( true );

		when( imageRestService.listResolutions( any( ListResolutionsRequest.class ) ) ).thenReturn( response );

		boolean failed = false;

		try {
			imageServerClient.listAllowedResolutions( "online" );
		}
		catch ( ImageServerException ise ) {
			failed = true;
		}

		assertTrue( failed );

		failed = false;

		try {
			imageServerClient.listConfigurableResolutions( "online" );
		}
		catch ( ImageServerException ise ) {
			failed = true;
		}

		assertTrue( failed );
	}

	@Test
	public void listAllowedResolutions() {
		ListResolutionsRequest request = new ListResolutionsRequest();
		request.setContext( "digital" );
		request.setConfigurableOnly( false );

		List<ImageResolutionDto> resolutions = new ArrayList<>();
		ListResolutionsResponse response = new ListResolutionsResponse();
		response.setImageResolutions( resolutions );

		when( imageRestService.listResolutions( request ) ).thenReturn( response );

		assertSame( resolutions, imageServerClient.listAllowedResolutions( "digital" ) );
	}

	@Test
	public void listConfigurableResolutions() {
		ListResolutionsRequest request = new ListResolutionsRequest();
		request.setContext( "digital" );
		request.setConfigurableOnly( true );

		List<ImageResolutionDto> resolutions = new ArrayList<>();
		ListResolutionsResponse response = new ListResolutionsResponse();
		response.setImageResolutions( resolutions );

		when( imageRestService.listResolutions( request ) ).thenReturn( response );

		assertSame( resolutions, imageServerClient.listConfigurableResolutions( "digital" ) );
	}

	@Configuration
	protected static class Config
	{
		@Bean
		public ImageServerClient imageServerClient() {
			return new LocalImageServerClient( "http://localhost:8078/" );
		}

		@Bean
		public ImageRestService imageRestService() {
			return mock( ImageRestService.class );
		}

		@Bean
		public ImageService imageService() {
			return mock( ImageService.class );
		}
	}
}
