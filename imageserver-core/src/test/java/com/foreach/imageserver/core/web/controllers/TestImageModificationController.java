package com.foreach.imageserver.core.web.controllers;

import com.foreach.imageserver.core.business.Application;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.services.ApplicationService;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.core.services.exceptions.ImageModificationException;
import com.foreach.imageserver.core.web.exceptions.ApplicationDeniedException;
import com.foreach.imageserver.core.web.exceptions.ImageNotFoundException;
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
			modificationController.register( 1, UUID.randomUUID().toString(), "somekey", createModifier() );
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
			modificationController.register( 1, UUID.randomUUID().toString(), "somekey", createModifier() );
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

		modificationController.register( 1, inactive.getCode(), "somekey", createModifier() );
	}

	@Test(expected = ImageNotFoundException.class)
	public void requestUnknownImageForApplication() {
		Application application = mock( Application.class );

		when( applicationService.getApplicationById( 1 ) ).thenReturn( application );
		when( application.canBeManaged( anyString() ) ).thenReturn( true );

		modificationController.register( 1, UUID.randomUUID().toString(), "somekey", createModifier() );
	}

	@Test(expected = ImageModificationException.class)
	public void requestEmptyTargetDimensions() {
		Application application = mock( Application.class );

		when( applicationService.getApplicationById( 1 ) ).thenReturn( application );
		when( application.canBeManaged( anyString() ) ).thenReturn( true );

		modificationController.register( 1, UUID.randomUUID().toString(), "somekey",
		                                 createModifier( new Dimensions() ) );
	}

	@Test
	public void validDimensionsAndModifier() {
		Application application = mock( Application.class );
		when( application.getId() ).thenReturn( 1 );

		when( applicationService.getApplicationById( 1 ) ).thenReturn( application );
		when( application.canBeManaged( anyString() ) ).thenReturn( true );

		Image image = mock( Image.class );
		Dimensions dimensions = new Dimensions( 800, 0 );

		ImageModificationController.ModifierWithTargetDimensions modifier = createModifier( dimensions );

		when( imageService.getImageByKey( "somekey", 1 ) ).thenReturn( image );

		modificationController.register( 1, UUID.randomUUID().toString(), "somekey", modifier );

		verify( imageService, times( 1 ) ).registerModification( image, dimensions, modifier );
	}

	private ImageModificationController.ModifierWithTargetDimensions createModifier() {
		return createModifier( new Dimensions( 800, 600 ) );
	}

	private ImageModificationController.ModifierWithTargetDimensions createModifier( Dimensions dimensions ) {
		ImageModificationController.ModifierWithTargetDimensions mod =
				new ImageModificationController.ModifierWithTargetDimensions();
		mod.setTarget( dimensions );

		return mod;
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
