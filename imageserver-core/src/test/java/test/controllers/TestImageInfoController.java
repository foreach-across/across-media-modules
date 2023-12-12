package test.controllers;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.controllers.ImageInfoController;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.dto.ImageInfoDto;
import com.foreach.imageserver.dto.JsonResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Gunther Van Geetsom
 * @since 5.0.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestImageInfoController.Config.class)
public class TestImageInfoController
{
	@Autowired
	private ImageService imageService;

	@Autowired
	private ImageInfoController imageInfoController;

	@BeforeEach
	public void resetMocks() {
		reset( imageService );
	}

	@Test
	public void shouldReturnInfoAboutImage() {
		// Arrange
		Image mockImage = new Image();
		mockImage.setDimensions( new Dimensions( 100, 200 ) );
		mockImage.setImageType( ImageType.BMP );

		when( imageService.loadImageData( any() ) ).thenReturn( mockImage );

		// Act
		JsonResponse response = imageInfoController.infoForUploadedImage( "token", new byte[] { 1 } );

		// Assert
		assertTrue( response.isSuccess() );

		ImageInfoDto result = (ImageInfoDto) response.getResult();

		assertEquals( 100, result.getDimensionsDto().getWidth() );
		assertEquals( 200, result.getDimensionsDto().getHeight() );
		assertEquals( ImageType.BMP.getExtension(), result.getImageType().getExtension() );
		assertFalse( result.isExisting() );
	}

	@Configuration
	public static class Config
	{
		@Bean
		public ImageInfoController imageInfoController() {
			return new ImageInfoController( "token" );
		}

		@Bean
		public ImageService imageService() {
			return mock( ImageService.class );
		}
	}
}
