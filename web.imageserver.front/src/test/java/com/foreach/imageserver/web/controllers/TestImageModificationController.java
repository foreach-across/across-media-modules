package com.foreach.imageserver.web.controllers;

import com.foreach.imageserver.business.Application;
import com.foreach.imageserver.business.Dimensions;
import com.foreach.imageserver.business.Image;
import com.foreach.imageserver.business.ImageModifier;
import com.foreach.imageserver.services.ApplicationService;
import com.foreach.imageserver.services.ImageService;
import com.foreach.imageserver.services.exceptions.ImageModificationException;
import com.foreach.imageserver.web.exceptions.ApplicationDeniedException;
import com.foreach.imageserver.web.exceptions.ImageNotFoundException;
import com.foreach.test.MockedLoader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = TestImageModificationController.TestConfig.class, loader = MockedLoader.class)
public class TestImageModificationController
{
	@Autowired
	private ImageModificationController modificationController;

	@Autowired
	private ApplicationService applicationService;

	@Autowired
	private ImageService imageService;

	@Test
	public void unknownApplicationReturnsPermissionDenied() {
		boolean exceptionWasThrown = false;

		try {
			modificationController.register( 1, UUID.randomUUID().toString(), "somekey", new ImageModifier(),
			                                 new Dimensions() );
		}
		catch ( ApplicationDeniedException ade ) {
			exceptionWasThrown = true;
		}

		assertTrue( exceptionWasThrown );
		verify( applicationService ).getApplicationById( 1 );
	}

	@Test
	public void ifApplicationManagementNotAllowedThenPermissionDenied() {
		boolean exceptionWasThrown = false;

		Application application = mock( Application.class );

		when( applicationService.getApplicationById( anyInt() ) ).thenReturn( application );
		when( application.canBeManaged( anyString() ) ).thenReturn( false );

		try {
			modificationController.register( 1, UUID.randomUUID().toString(), "somekey", new ImageModifier(),
			                                 new Dimensions() );
		}
		catch ( ApplicationDeniedException ade ) {
			exceptionWasThrown = true;
		}

		assertTrue( exceptionWasThrown );
	}

	@Test(expected = ApplicationDeniedException.class)
	public void requestInactiveApplication() {
		Application inactive = new Application();
		inactive.setCode( UUID.randomUUID().toString() );
		inactive.setActive( false );

		when( applicationService.getApplicationById( 1 ) ).thenReturn( inactive );

		modificationController.register( 1, inactive.getCode(), "somekey", new ImageModifier(), new Dimensions() );
	}

	@Test(expected = ImageNotFoundException.class)
	public void requestUnknownImageForApplication() {
		Application application = mock( Application.class );

		when( applicationService.getApplicationById( 1 ) ).thenReturn( application );
		when( application.canBeManaged( anyString() ) ).thenReturn( true );

		modificationController.register( 1, UUID.randomUUID().toString(), "somekey", new ImageModifier(),
		                                 new Dimensions( 800, 600 ) );
	}

	@Test(expected = ImageModificationException.class)
	public void emptyDimensionsAreNotAllowed() {
		Application application = mock( Application.class );

		when( applicationService.getApplicationById( 1 ) ).thenReturn( application );
		when( application.canBeManaged( anyString() ) ).thenReturn( true );

		modificationController.register( 1, UUID.randomUUID().toString(), "somekey", new ImageModifier(),
		                                 new Dimensions() );
	}

	@Test
	public void validDimensionsAndModifier() {
		Application application = mock( Application.class );
		when( application.getId() ).thenReturn( 1 );

		when( applicationService.getApplicationById( 1 ) ).thenReturn( application );
		when( application.canBeManaged( anyString() ) ).thenReturn( true );

		Image image = mock( Image.class );
		ImageModifier modifier = mock( ImageModifier.class );

		when( imageService.getImageByKey( "somekey", 1 ) ).thenReturn( image );

		modificationController.register( 1, UUID.randomUUID().toString(), "somekey", modifier,
		                                 new Dimensions( 800, 0 ) );

		verify( imageService, times( 1 ) ).registerModification( image, new Dimensions( 800, 0 ), modifier );
	}

	@Configuration
	public static class TestConfig
	{
		@Bean
		public ImageModificationController imageModificationController() {
			return new ImageModificationController();
		}
	}
}
