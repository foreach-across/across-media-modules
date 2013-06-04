package com.foreach.imageserver.admin.controllers;

import com.foreach.imageserver.business.Application;
import com.foreach.imageserver.services.ApplicationService;
import com.foreach.imageserver.services.repositories.ImageLookupRepository;
import com.foreach.imageserver.services.repositories.RepositoryLookupResult;
import com.foreach.imageserver.services.repositories.RepositoryLookupStatus;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = TestImageLoadController.TestConfig.class)
public class TestImageLoadController
{
	private final Random RANDOM = new Random( System.currentTimeMillis() );

	@Autowired
	private ImageLoadController loadController;

	@Autowired
	private ApplicationService applicationService;

	@Autowired
	private ImageLookupRepository imageLookupRepository;

	@Test
	public void unknownApplicationReturnsPermissionDenied() {
		boolean exceptionWasThrown = false;

		try {
			loadController.load( 1, UUID.randomUUID(), "http://someimageurl" );
		}
		catch ( ImageLoadController.ApplicationDeniedException ade ) {
			exceptionWasThrown = true;
		}

		assertTrue( exceptionWasThrown );
		verify( applicationService ).getApplicationById( 1 );
	}

	@Test
	public void ifApplicationManagementNotAllowedThenPermissionDenied() {
		boolean exceptionWasThrown = false;

		Application application = createApplication( true );
		when( applicationService.getApplicationById( anyInt() ) ).thenReturn( application );

		UUID code = UUID.randomUUID();
		assertFalse( "Precondition on test data failed", application.canBeManaged( code ) );

		try {
			loadController.load( application.getId(), code, "http://someimageurl" );
		}
		catch ( ImageLoadController.ApplicationDeniedException ade ) {
			exceptionWasThrown = true;
		}

		assertTrue( exceptionWasThrown );
		verify( applicationService ).getApplicationById( application.getId() );
	}

	@Test
	public void validApplicationWillResultInRepositoryLookup() {
		lookupWithStatus( RepositoryLookupStatus.SUCCESS );
	}

	@Test(expected = ImageLoadController.ImageNotFoundException.class)
	public void lookupNotFound() {
		lookupWithStatus( RepositoryLookupStatus.NOT_FOUND );
	}

	@Test(expected = ImageLoadController.ImageForbiddenException.class)
	public void lookupPermissionDenied() {
		lookupWithStatus( RepositoryLookupStatus.ACCESS_DENIED );
	}

	@Test(expected = ImageLoadController.ImageLookupException.class)
	public void lookupError() {
		lookupWithStatus( RepositoryLookupStatus.ERROR );
	}

	private void lookupWithStatus( RepositoryLookupStatus status ) {
		Application application = prepareValidApplication();
		String imageURI = RandomStringUtils.random( 30 );

		RepositoryLookupResult lookupResult = new RepositoryLookupResult();
		lookupResult.setStatus( status );
		when( imageLookupRepository.fetchImage( imageURI ) ).thenReturn( lookupResult );

		loadController.load( application.getId(), application.getCode(), imageURI );

		verify( imageLookupRepository ).fetchImage( imageURI );
	}

	@Test
	public void newImageWillBeAddedWithDefaultKey() {

	}

	@Test
	public void newImageWillBeAddedWithCustomKey() {

	}

	@Test
	public void existingImageWillBeUpdatedWithDefaultKey() {

	}

	@Test
	public void existingImageWillBeUpdatedWithCustomKey() {

	}

	private Application prepareValidApplication() {
		Application application = createApplication( true );
		when( applicationService.getApplicationById( anyInt() ) ).thenReturn( application );
		assertTrue( "Precondition on test data failed", application.canBeManaged( application.getCode() ) );

		return application;
	}

	private Application createApplication( boolean active ) {
		Application application = new Application();
		application.setId( RANDOM.nextInt() );
		application.setCode( UUID.randomUUID() );
		application.setActive( active );

		return application;
	}

	@Configuration
	public static class TestConfig
	{
		@Bean
		public ImageLoadController imageLoadController() {
			return new ImageLoadController();
		}

		@Bean
		public ApplicationService applicationService() {
			return mock( ApplicationService.class );
		}

		@Bean
		public ImageLookupRepository imageLookupRepository() {
			return mock( ImageLookupRepository.class );
		}
	}
}
