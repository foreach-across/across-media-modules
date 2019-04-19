package test.controllers;

import com.foreach.imageserver.core.controllers.ImageDeleteController;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.dto.JsonResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 3.5
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestImageDeleteController.Config.class)
public class TestImageDeleteController
{
	@Autowired
	private ImageService imageService;

	@Autowired
	private ImageDeleteController imageDeleteController;

	@Before
	public void resetMocks() {
		reset( imageService );
	}

	@Test
	public void invalidToken() {
		JsonResponse response = imageDeleteController.deleteImage( "invalid_token", "image" );

		assertFalse( response.isSuccess() );
		assertEquals( "Access denied.", response.getErrorMessage() );
		verify( imageService, never() ).deleteImage( anyString() );
	}

	@Test
	@SuppressWarnings("unchecked")
	public void deleteNotExistingImage() {
		JsonResponse response = imageDeleteController.deleteImage( "token", "notExisting" );

		assertTrue( response.isSuccess() );
		assertEquals( false, ( (Map<String, Object>) response.getResult() ).get( "deleted" ) );
		verify( imageService ).deleteImage( "notExisting" );
	}

	@Test
	@SuppressWarnings("unchecked")
	public void deleteExistingImage() {
		when( imageService.deleteImage( "existing" ) ).thenReturn( true );

		JsonResponse response = imageDeleteController.deleteImage( "token", "existing" );
		assertTrue( response.isSuccess() );
		assertEquals( true, ( (Map<String, Object>) response.getResult() ).get( "deleted" ) );
	}

	@Configuration
	public static class Config
	{
		@Bean
		public ImageDeleteController imageDeleteController() {
			return new ImageDeleteController( "token" );
		}

		@Bean
		public ImageService imageService() {
			return mock( ImageService.class );
		}
	}
}
