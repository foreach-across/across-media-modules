package test.transformers.imagemagick;

import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.transformers.ImageAttributes;
import com.foreach.imageserver.core.transformers.ImageAttributesCommand;
import com.foreach.imageserver.core.transformers.ImageModificationException;
import com.foreach.imageserver.core.transformers.imagemagick.ImageMagickAttributesCommandExecutor;
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

import java.io.ByteArrayInputStream;
import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
@ContextConfiguration(classes = TestImageMagickAttributesCommandExecutor.Config.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class TestImageMagickAttributesCommandExecutor
{
	@Autowired
	private ImageMagickAttributesCommandExecutor executor;

	@Test
	public void getImageAttributesJpeg() {
		ImageAttributes attributes = fetchAttributes( "images/cropCorrectness.jpeg" );
		assertEquals( 1, attributes.getSceneCount() );
		assertEquals( ImageType.JPEG, attributes.getType() );
		assertEquals( 2000, attributes.getDimensions().getWidth() );
		assertEquals( 1000, attributes.getDimensions().getHeight() );
	}

	@Test
	public void getImageAttributesPng() {
		ImageAttributes attributes = fetchAttributes( "images/cropCorrectness.png" );
		assertEquals( 1, attributes.getSceneCount() );
		assertEquals( ImageType.PNG, attributes.getType() );
		assertEquals( 2000, attributes.getDimensions().getWidth() );
		assertEquals( 1000, attributes.getDimensions().getHeight() );
	}

	@Test
	public void getImageAttributesGif() {
		ImageAttributes attributes = fetchAttributes( "images/getAttributes.gif" );
		assertEquals( 1, attributes.getSceneCount() );
		assertEquals( ImageType.GIF, attributes.getType() );
		assertEquals( 640, attributes.getDimensions().getWidth() );
		assertEquals( 125, attributes.getDimensions().getHeight() );
	}

	@Test
	public void getImageAttributesSvg() {
		ImageAttributes attributes = fetchAttributes( "images/getAttributes.svg" );
		assertEquals( 1, attributes.getSceneCount() );
		assertEquals( ImageType.SVG, attributes.getType() );
		assertEquals( 600, attributes.getDimensions().getWidth() );
		assertEquals( 600, attributes.getDimensions().getHeight() );
	}

	@Test
	public void getImageAttributesEps() {
		ImageAttributes attributes = fetchAttributes( "images/getAttributes.eps" );
		assertEquals( 1, attributes.getSceneCount() );
		assertEquals( ImageType.EPS, attributes.getType() );
		assertEquals( 641, attributes.getDimensions().getWidth() );
		assertEquals( 126, attributes.getDimensions().getHeight() );
	}

	@Test
	public void getImageAttributesPdf() {
		ImageAttributes attributes = fetchAttributes( "images/sample-pdf.pdf" );
		assertEquals( 5, attributes.getSceneCount() );
		assertEquals( ImageType.PDF, attributes.getType() );
		assertEquals( 612, attributes.getDimensions().getWidth() );
		assertEquals( 792, attributes.getDimensions().getHeight() );
	}

	@Test
	public void getImageAttributesTiff() {
		ImageAttributes attributes = fetchAttributes( "images/getAttributes.tiff" );
		assertEquals( 1, attributes.getSceneCount() );
		assertEquals( ImageType.TIFF, attributes.getType() );
		assertEquals( 640, attributes.getDimensions().getWidth() );
		assertEquals( 125, attributes.getDimensions().getHeight() );
	}

	@Test(expected = ImageModificationException.class)
	public void getImageAttributesForUnrecognizedByteStream() {
		ImageAttributesCommand command = ImageAttributesCommand.builder()
		                                                       .imageStream( new ByteArrayInputStream( "This is not an image.".getBytes() ) )
		                                                       .build();
		executor.execute( command );
	}

	private ImageAttributes fetchAttributes( String imageResource ) {
		ImageAttributesCommand command = ImageAttributesCommand.builder()
		                                                       .imageStream( getClass().getClassLoader().getResourceAsStream( imageResource ) )
		                                                       .build();
		executor.execute( command );

		assertThat( command.isCompleted() ).isTrue();
		assertThat( command.getExecutionResult() ).isNotNull();

		return command.getExecutionResult();
	}

	@Configuration
	@PropertySource("classpath:integrationtests.properties")
	static class Config
	{
		@Bean
		public ImageMagickAttributesCommandExecutor imageMagickAttributesCommandExecutor( Environment environment ) {
			ProcessStarter.setGlobalSearchPath( new File( environment.getProperty( "transformer.imagemagick.path" ) ).getAbsolutePath() );
			System.setProperty( "im4java.useGM", "true" );

			return new ImageMagickAttributesCommandExecutor();
		}
	}
}