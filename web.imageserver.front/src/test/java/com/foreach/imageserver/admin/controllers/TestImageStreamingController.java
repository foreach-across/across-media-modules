package com.foreach.imageserver.admin.controllers;

import com.foreach.imageserver.business.Application;
import com.foreach.imageserver.business.Image;
import com.foreach.imageserver.business.ImageFile;
import com.foreach.imageserver.business.ImageType;
import com.foreach.imageserver.controllers.ImageStreamingController;
import com.foreach.imageserver.controllers.exception.ImageNotFoundException;
import com.foreach.imageserver.services.ApplicationService;
import com.foreach.imageserver.services.ImageService;
import com.foreach.test.MockedLoader;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = TestImageStreamingController.TestConfig.class, loader = MockedLoader.class)
public class TestImageStreamingController
{
	@Autowired
	private ImageStreamingController streamingController;

	@Autowired
	private ApplicationService applicationService;

	@Autowired
	private ImageService imageService;

	@Test(expected = ImageNotFoundException.class)
	public void requestUnknownApplication() {
		streamingController.view( 1, RandomStringUtils.randomAlphanumeric( 50 ), new MockHttpServletResponse() );
	}

	@Test(expected = ImageNotFoundException.class)
	public void requestInactiveApplication() {
		Application inactive = new Application();
		inactive.setActive( false );

		when( applicationService.getApplicationById( 1 ) ).thenReturn( inactive );

		streamingController.view( 1, RandomStringUtils.randomAlphanumeric( 50 ), new MockHttpServletResponse() );
	}

	@Test(expected = ImageNotFoundException.class)
	public void requestUnknownImageForApplication() {
		Application application = new Application();
		application.setActive( true );

		when( applicationService.getApplicationById( 1 ) ).thenReturn( application );

		streamingController.view( 1, RandomStringUtils.randomAlphanumeric( 50 ), new MockHttpServletResponse() );
	}

	@Test
	public void requestValidOriginal() throws Exception {
		Application application = new Application();
		application.setId( 1 );
		application.setActive( true );

		Image image = new Image();

		byte[] contentBytes = new byte[] { 'A', 'B', 'C' };

		ImageFile imageFile = new ImageFile( ImageType.PNG, 123456, new ByteInputStream( contentBytes, 3 ) );

		when( applicationService.getApplicationById( 1 ) ).thenReturn( application );
		when( imageService.getImageByKey( "myimagekey", 1 ) ).thenReturn( image );
		when( imageService.fetchImageFile( image ) ).thenReturn( imageFile );

		MockHttpServletResponse mockResponse = new MockHttpServletResponse();

		streamingController.view( 1, "myimagekey", mockResponse );

		assertEquals( HttpStatus.OK.value(), mockResponse.getStatus() );
		assertEquals( imageFile.getImageType().getContentType(), mockResponse.getContentType() );
		assertEquals( imageFile.getFileSize(), mockResponse.getContentLength() );
		assertArrayEquals( contentBytes, mockResponse.getContentAsByteArray() );
	}

	public void requestValidCustomImage() {

	}

	public void requestInvalidCustomImage() {

	}

	@Configuration
	public static class TestConfig
	{
		@Bean
		public ImageStreamingController imageStreamingController() {
			return new ImageStreamingController();
		}
	}
}
