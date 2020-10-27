package test.controllers;

import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.controllers.ImageStreamingController;
import com.foreach.imageserver.core.rest.request.ViewImageRequest;
import com.foreach.imageserver.core.rest.response.ViewImageResponse;
import com.foreach.imageserver.core.rest.services.ImageRestService;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.core.transformers.SimpleImageSource;
import com.foreach.imageserver.dto.ImageModificationDto;
import com.foreach.imageserver.dto.ImageVariantDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = TestImageStreamingController.Config.class)
@ExtendWith(SpringExtension.class)
public class TestImageStreamingController
{
	@Autowired
	private ImageStreamingController controller;
	@Autowired
	private ImageRestService imageRestService;

	@BeforeEach
	public void resetMocks() {
		reset( imageRestService );
	}

	@Test
	public void testThatIOExceptionWithoutMessageDoesNotNullPointer() throws Exception {
		ViewImageResponse viewImageResponse = new ViewImageResponse();
		SimpleImageSource simpleImageSource = new SimpleImageSource( ImageType.JPEG, new byte[] { 1 } );
		HttpServletResponse response = mock( HttpServletResponse.class );
		when( response.getOutputStream() ).thenThrow( new IOException() );
		viewImageResponse.setImageSource( simpleImageSource );
		when( imageRestService.renderImage( any( ViewImageRequest.class ) ) ).thenReturn( viewImageResponse );
		      controller.render( "abc", "id", mock( ImageModificationDto.class ), mock(
				      ImageVariantDto.class ), response );
		verify( response ).setContentType( "text/plain" );
		verify( response ).setHeader( "Cache-Control", "no-cache" );
		verify( response ).setHeader( ImageStreamingController.AKAMAI_EDGE_CONTROL_HEADER,
		                              ImageStreamingController.AKAMAI_NO_STORE );
	}

	@Test
	public void testThatExpiresHeaderIsCorrect() throws Exception {
		ViewImageResponse viewImageResponse = new ViewImageResponse(  );
		SimpleImageSource simpleImageSource = new SimpleImageSource( ImageType.JPEG, new byte[] { 1 } );
		MockHttpServletResponse response = new MockHttpServletResponse();
		viewImageResponse.setImageSource( simpleImageSource );
		when( imageRestService.renderImage( any( ViewImageRequest.class ) ) ).thenReturn( viewImageResponse );
		controller.render( "abc", "id", mock( ImageModificationDto.class ), mock(
				ImageVariantDto.class ), response );
		assertEquals( "max-age=30", response.getHeader( "Cache-Control" ) );
		assertNull( response.getHeader( ImageStreamingController.AKAMAI_EDGE_CONTROL_HEADER ) );
		String expiresHeader = response.getHeader( "Expires" );

		assertTrue( expiresHeader.contains( "GMT" ) );
	}


	@Configuration
	static class Config
	{
		@Bean
		public ImageStreamingController imageStreamingController() {
			return new ImageStreamingController( "abc", false );
		}

		@Bean
		public ImageService imageService() {
			return mock( ImageService.class );
		}

		@Bean
		public ImageRestService imageRestService() {
			return mock( ImageRestService.class );
		}
	}
}
