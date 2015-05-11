package com.foreach.imageserver.core.controllers;

import com.foreach.common.test.MockedLoader;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.rest.request.ViewImageRequest;
import com.foreach.imageserver.core.rest.response.ViewImageResponse;
import com.foreach.imageserver.core.rest.services.ImageRestService;
import com.foreach.imageserver.core.transformers.StreamImageSource;
import com.foreach.imageserver.dto.ImageModificationDto;
import com.foreach.imageserver.dto.ImageVariantDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

@ContextConfiguration(loader = MockedLoader.class, classes = TestImageStreamingController.Config.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TestImageStreamingController
{
	@Autowired
	private ImageStreamingController controller;
	@Autowired
	private ImageRestService imageRestService;

	@Before
	public void resetMocks() {
		reset( imageRestService );
	}

	@Test
	public void testThatIOExceptionWithoutMessageDoesNotNullPointer() throws Exception {
		ViewImageResponse viewImageResponse = new ViewImageResponse(  );
		StreamImageSource streamImageSource = new StreamImageSource( ImageType.JPEG, new byte[] {1} );
		HttpServletResponse response = mock( HttpServletResponse.class );
		when( response.getOutputStream() ).thenThrow( new IOException() );
		viewImageResponse.setImageSource( streamImageSource );
		when( imageRestService.renderImage( (ViewImageRequest) anyObject() ) ).thenReturn( viewImageResponse );
		      controller.render( "abc", "id", mock( ImageModificationDto.class ), mock(
				      ImageVariantDto.class ), response );
		verify( response ).setContentType( "text/plain" );
		verify( response ).setHeader( "Cache-Control", "no-cache" );
		verify( response ).setHeader( ImageStreamingController.AKAMAI_EDGE_CONTROL_HEADER,
		                              ImageStreamingController.AKAMAI_NO_STORE );
	}

	@Configuration
	static class Config
	{
		@Bean
		public ImageStreamingController imageStreamingController() {
			return new ImageStreamingController( "abc" );
		}
	}
}
