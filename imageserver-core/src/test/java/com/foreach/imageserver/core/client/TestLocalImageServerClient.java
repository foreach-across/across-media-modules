package com.foreach.imageserver.core.client;

import be.mediafin.imageserver.client.ImageServerClient;
import be.mediafin.imageserver.client.ImageServerException;
import com.foreach.imageserver.core.rest.request.ListResolutionsRequest;
import com.foreach.imageserver.core.rest.response.ListResolutionsResponse;
import com.foreach.imageserver.core.rest.services.ResolutionRestService;
import com.foreach.imageserver.dto.ImageResolutionDto;
import com.foreach.imageserver.dto.ImageTypeDto;
import com.foreach.test.MockedLoader;
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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(loader = MockedLoader.class, classes = TestLocalImageServerClient.Config.class)
public class TestLocalImageServerClient
{
	@Autowired
	private ResolutionRestService resolutionRestService;

	@Autowired
	private ImageServerClient imageServerClient = new LocalImageServerClient( "http://localhost:8078/" );

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
	public void listResolutionsContextNotFoundThrowsException() {
		ListResolutionsResponse response = new ListResolutionsResponse();
		response.setContextDoesNotExist( true );

		when( resolutionRestService.listResolutions( any( ListResolutionsRequest.class ) ) ).thenReturn( response );

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
		request.setContextCode( "digital" );
		request.setConfigurableOnly( false );

		List<ImageResolutionDto> resolutions = new ArrayList<>();
		ListResolutionsResponse response = new ListResolutionsResponse();
		response.setImageResolutions( resolutions );

		when( resolutionRestService.listResolutions( request ) ).thenReturn( response );

		assertSame( resolutions, imageServerClient.listAllowedResolutions( "digital" ) );
	}

	@Test
	public void listConfigurableResolutions() {
		ListResolutionsRequest request = new ListResolutionsRequest();
		request.setContextCode( "digital" );
		request.setConfigurableOnly( true );

		List<ImageResolutionDto> resolutions = new ArrayList<>();
		ListResolutionsResponse response = new ListResolutionsResponse();
		response.setImageResolutions( resolutions );

		when( resolutionRestService.listResolutions( request ) ).thenReturn( response );

		assertSame( resolutions, imageServerClient.listConfigurableResolutions( "digital" ) );
	}

	@Configuration
	protected static class Config
	{
		@Bean
		public ImageServerClient imageServerClient() {
			return new LocalImageServerClient( "http://localhost:8078/" );
		}
	}
}
