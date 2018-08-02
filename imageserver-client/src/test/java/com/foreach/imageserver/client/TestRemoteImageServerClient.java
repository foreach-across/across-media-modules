package com.foreach.imageserver.client;

import com.foreach.imageserver.dto.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * @author Arne Vandamme
 */
public class TestRemoteImageServerClient
{
	private RemoteImageServerClient imageServerClient =
			new RemoteImageServerClient( "http://localhost:8078", "standalone-access-token" );
	private RestTemplate restTemplate = new RestTemplate();
	private MockRestServiceServer mockRestServiceServer;

	@Before
	public void setUp() {
		mockRestServiceServer = MockRestServiceServer.createServer( restTemplate );
		ReflectionTestUtils.setField( imageServerClient, "restTemplate", restTemplate );
	}

	@Test
	public void customRestTemplateCanBeProvided() throws Exception {
		String imageId = "abc";
		String token = "standalone-access-token";
		String endpoint = "https://localhost:8078";
		String url = endpoint + "/api/image/details?token=" + token + "&iid=" + imageId;
		URI uri = new URI( url );
		String responseJson = "{\"result\":{\"existing\":\"true\"}, \"success\":\"true\"}";

		CloseableHttpClient httpClient = HttpClients.custom()
		                                            .build();

		HttpComponentsClientHttpRequestFactory requestFactory = mock( HttpComponentsClientHttpRequestFactory.class );
		when( requestFactory.getHttpClient() ).thenReturn( httpClient );

		MockClientHttpRequest mockClientHttpRequest = new MockClientHttpRequest( HttpMethod.GET, uri );
		MockClientHttpResponse clientHttpResponse =
				new MockClientHttpResponse( responseJson.getBytes(), HttpStatus.OK );
		clientHttpResponse.getHeaders().add( "Content-Type", MediaType.APPLICATION_JSON_VALUE );
		mockClientHttpRequest.setResponse( clientHttpResponse );

		when( requestFactory.createRequest( uri, HttpMethod.GET ) ).thenReturn( mockClientHttpRequest );

		//set a custom requestFactory on the resttemplate
		RestTemplate customRestTemplate = new RestTemplate( requestFactory );

		imageServerClient = new RemoteImageServerClient( endpoint, token, customRestTemplate );

		assertTrue( imageServerClient.imageExists( imageId ) );

		//check that the custom requestFacotry is used, than we know our custom resttemplate is used
		verify( requestFactory, times( 1 ) ).createRequest( uri, HttpMethod.GET );
	}

	@Test
	public void imageUrl() {
		String url = imageServerClient.imageUrl( "10", "ONLINE", 1000, 2000, ImageTypeDto.TIFF );
		assertEquals( "http://localhost:8078/view?iid=10&context=ONLINE&width=1000&height=2000&imageType=TIFF", url );

		url = imageServerClient.imageUrl( "10", "DIGITAL", 0, 2000, ImageTypeDto.TIFF );
		assertEquals( "http://localhost:8078/view?iid=10&context=DIGITAL&height=2000&imageType=TIFF", url );

		url = imageServerClient.imageUrl( "someid", "SITE", 1000, 0, ImageTypeDto.TIFF );
		assertEquals( "http://localhost:8078/view?iid=someid&context=SITE&width=1000&imageType=TIFF", url );
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
		assertEquals( "http://localhost:8078/view?iid=10&context=ONLINE&width=1000&height=2000", url );
	}

	@Test
	public void imageUrlWithZeroHeight() {
		String url = imageServerClient.imageUrl( "10", "ONLINE", 1000, 0, ImageTypeDto.TIFF );
		assertEquals( "http://localhost:8078/view?iid=10&context=ONLINE&width=1000&imageType=TIFF", url );
	}

	@Test
	public void imageUrlWithZeroWidth() {
		String url = imageServerClient.imageUrl( "10", "ONLINE", 0, 2000 );
		assertEquals( "http://localhost:8078/view?iid=10&context=ONLINE&height=2000", url );
	}

	@Test(expected = IllegalArgumentException.class)
	public void imageUrlNoSizeNoImageResolution() {
		ImageVariantDto variant = new ImageVariantDto();
		variant.setImageType( ImageTypeDto.JPEG );
		String url = imageServerClient.imageUrl( "10", "ONLINE", null, variant, null );
		assertEquals( "http://localhost:8078/view?iid=10&context=ONLINE&height=2000", url );
	}

	@Test
	public void imageUrlWithOneSize() {
		ImageVariantDto variant = new ImageVariantDto();
		variant.setImageType( ImageTypeDto.JPEG );
		String url = imageServerClient.imageUrl( "10", "ONLINE", "100x200" );
		assertEquals( "http://localhost:8078/view?iid=10&context=ONLINE&size=100x200", url );
	}

	@Test
	public void imageUrlWithManySizes() {
		ImageVariantDto variant = new ImageVariantDto();
		variant.setImageType( ImageTypeDto.JPEG );
		String url = imageServerClient.imageUrl( "10", "ONLINE", "100x200", "200x400", "400x800" );
		assertEquals( "http://localhost:8078/view?iid=10&context=ONLINE&size=100x200,200x400,400x800", url );
	}

	@Test
	public void imageUrlWithBoundaries() {
		String url = imageServerClient.imageUrl( "10", "ONLINE", 1000, 2000,
		                                         new ImageVariantDto( new DimensionsDto( 400, 800 ) ) );
		assertEquals(
				"http://localhost:8078/view?iid=10&context=ONLINE&width=1000&height=2000&boundaries.width=400&boundaries.height=800",
				url );
	}

	@Test
	public void registerImageModificationsUsesPost() {
		List<ImageModificationDto> imageModificationDtoList = new ArrayList<>();

		{
			ImageModificationDto item = new ImageModificationDto( 100, 101 );

			ImageResolutionDto resolution = new ImageResolutionDto( 110, 111 );
			item.setResolution( resolution );

			CropDto crop = new CropDto( 125, 126, 120, 121 );
			item.setCrop( crop );

			DimensionsDto boundaries = new DimensionsDto( 130, 131 );
			item.setBoundaries( boundaries );

			DimensionsDto density = new DimensionsDto( 140, 141 );
			item.setDensity( density );

			imageModificationDtoList.add( item );
		}

		{
			ImageModificationDto item = new ImageModificationDto( 200, 201 );

			ImageResolutionDto resolution = new ImageResolutionDto( 210, 211 );
			item.setResolution( resolution );

			CropDto crop = new CropDto( 225, 226, 220, 221 );
			item.setCrop( crop );

			DimensionsDto boundaries = new DimensionsDto( 230, 231 );
			item.setBoundaries( boundaries );

			DimensionsDto density = new DimensionsDto( 240, 241 );
			item.setDensity( density );

			imageModificationDtoList.add( item );
		}

		mockRestServiceServer.expect( requestTo(
				"http://localhost:8078/api/modification/registerlist?token=standalone-access-token&iid=4938&context=ONLINE" ) )
		                     .andExpect( method( HttpMethod.POST ) )
		                     .andExpect( content().string(
				                     "resolution.width=110&resolution.width=210&resolution.height=111&resolution.height=211&crop.x=125&crop.x=225&crop.y=126&crop.y=226&crop.width=120&crop.width=220&crop.height=121&crop.height=221&crop.source.width=0&crop.source.width=0&crop.source.height=0&crop.source.height=0&crop.box.width=0&crop.box.width=0&crop.box.height=0&crop.box.height=0&density.width=140&density.width=240&density.height=141&density.height=241&boundaries.width=130&boundaries.width=230&boundaries.height=131&boundaries.height=231" ) )
		                     .andRespond( withSuccess( "{\"result\":\"ok\", \"success\":\"true\"}",
		                                               MediaType.APPLICATION_JSON ) );

		imageServerClient.registerImageModifications( "4938", "ONLINE", imageModificationDtoList );
	}
}
