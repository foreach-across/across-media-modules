package test.transformers.imagemagick;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.transformers.ImageAttributes;
import com.foreach.imageserver.core.transformers.ImageSource;
import com.foreach.imageserver.core.transformers.ImageTransformCommand;
import com.foreach.imageserver.core.transformers.StreamImageSource;
import com.foreach.imageserver.core.transformers.imagemagick.ImageMagickTransformCommandExecutor;
import com.foreach.imageserver.dto.CropDto;
import com.foreach.imageserver.dto.ImageTransformDto;
import com.foreach.imageserver.dto.ImageTypeDto;
import lombok.SneakyThrows;
import org.im4java.process.ProcessStarter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.InputStream;

import static org.junit.Assert.assertTrue;
import static support.ImageUtils.*;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
@ContextConfiguration(classes = TestImageMagickTransformCommandExecutor.Config.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TestImageMagickTransformCommandExecutor
{
	@Autowired
	private ImageMagickTransformCommandExecutor executor;

	@Test
	@SneakyThrows
	public void cropPngToPng() {
		ImageTransformDto transformDto = ImageTransformDto.builder()
		                                                  .output( ImageTypeDto.PNG )
		                                                  .width( 270 )
		                                                  .height( 580 )
		                                                  .crop( CropDto.builder().x( 1000 ).y( 140 ).width( 270 ).height( 580 ).build() )
		                                                  .build();
		ImageSource image = image( "images/cropCorrectness.png" );
		ImageAttributes attributes = new ImageAttributes( ImageType.PNG, new Dimensions( 2000, 1000 ), 1 );

		ImageTransformCommand command = ImageTransformCommand
				.builder()
				.originalImage( image )
				.originalImageAttributes( attributes )
				.transform( transformDto )
				.build();

		executor.execute( command );

		assertImage( "images/cropPngToPng.png", command.getExecutionResult() );
	}

	@SneakyThrows
	private void assertImage( String expected, ImageSource actual ) {
		assertTrue( imagesAreEqual( bufferedImage( actual.getImageStream() ), bufferedImageFromClassPath( expected ) ) );
	}

	private ImageSource image( String path ) {
		InputStream imageStream = getClass().getClassLoader().getResourceAsStream( path );
		return new StreamImageSource( null, imageStream );
	}

	@Configuration
	@PropertySource("classpath:integrationtests.properties")
	static class Config
	{
		@Bean
		public ImageMagickTransformCommandExecutor imageMagickTransformCommandExecutor( Environment environment ) {
			ProcessStarter.setGlobalSearchPath( new File( environment.getProperty( "transformer.imagemagick.path" ) ).getAbsolutePath() );
			System.setProperty( "im4java.useGM", "true" );

			return new ImageMagickTransformCommandExecutor();
		}
	}
}
