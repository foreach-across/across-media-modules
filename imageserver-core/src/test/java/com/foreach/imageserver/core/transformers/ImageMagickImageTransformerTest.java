package com.foreach.imageserver.core.transformers;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.services.ImageContextService;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.core.services.ImageStoreService;
import com.foreach.imageserver.core.services.ImageStoreServiceImpl;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import static com.foreach.imageserver.core.utils.ImageUtils.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

@ContextConfiguration(classes = ImageMagickImageTransformerTest.Config.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ImageMagickImageTransformerTest
{

	@Autowired
	private ImageMagickImageTransformer imageTransformer;

	@Autowired
	private ImageStoreService imageStoreService;

	@Test
	public void canExecute() {
		for ( ImageType imageType : ImageType.values() ) {
			//ImageTransformerPriority expectedPriority = (imageType == ImageType.EPS || imageType == ImageType.PDF) ? ImageTransformerPriority.UNABLE : ImageTransformerPriority.PREFERRED;
			ImageTransformerPriority expectedPriority = ImageTransformerPriority.PREFERRED;
			assertEquals( expectedPriority, imageTransformer.canExecute( calculateDimensionsAction( imageType ) ) );
			assertEquals( expectedPriority, imageTransformer.canExecute( modifyAction( imageType ) ) );
		}

		assertEquals( ImageTransformerPriority.PREFERRED, imageTransformer.canExecute( getImageAttributesAction() ) );
	}

	@Test
	public void calculateDimensionsJpeg() {
		Dimensions dimensions =
				imageTransformer.execute( calculateDimensionsAction( ImageType.JPEG, "images/cropCorrectness.jpeg" ) );
		assertEquals( 2000, dimensions.getWidth() );
		assertEquals( 1000, dimensions.getHeight() );
	}

	@Test
	public void calculateDimensionsPng() {
		Dimensions dimensions =
				imageTransformer.execute( calculateDimensionsAction( ImageType.PNG, "images/cropCorrectness.png" ) );
		assertEquals( 2000, dimensions.getWidth() );
		assertEquals( 1000, dimensions.getHeight() );
	}

	@Test
	public void getImageAttributesJpeg() {
		ImageAttributes attributes =
				imageTransformer.execute( getImageAttributesAction( "images/cropCorrectness.jpeg" ) );
		assertEquals( ImageType.JPEG, attributes.getType() );
		assertEquals( 2000, attributes.getDimensions().getWidth() );
		assertEquals( 1000, attributes.getDimensions().getHeight() );
	}

	@Test
	public void getImageAttributesPng() {
		ImageAttributes attributes =
				imageTransformer.execute( getImageAttributesAction( "images/cropCorrectness.png" ) );
		assertEquals( ImageType.PNG, attributes.getType() );
		assertEquals( 2000, attributes.getDimensions().getWidth() );
		assertEquals( 1000, attributes.getDimensions().getHeight() );
	}

	@Test
	public void getImageAttributesGif() {
		ImageAttributes attributes = imageTransformer.execute( getImageAttributesAction( "images/getAttributes.gif" ) );
		assertEquals( ImageType.GIF, attributes.getType() );
		assertEquals( 640, attributes.getDimensions().getWidth() );
		assertEquals( 125, attributes.getDimensions().getHeight() );
	}

	@Test
	public void getImageAttributesSvg() {
		ImageAttributes attributes = imageTransformer.execute( getImageAttributesAction( "images/getAttributes.svg" ) );
		assertEquals( ImageType.SVG, attributes.getType() );
		assertEquals( 600, attributes.getDimensions().getWidth() );
		assertEquals( 600, attributes.getDimensions().getHeight() );
	}

	@Test
	public void getImageAttributesEps() {
		ImageAttributes attributes = imageTransformer.execute( getImageAttributesAction( "images/getAttributes.eps" ) );
		assertEquals( ImageType.EPS, attributes.getType() );
		assertEquals( 641, attributes.getDimensions().getWidth() );
		assertEquals( 126, attributes.getDimensions().getHeight() );
	}

	@Test
	public void getImageAttributesPdf() {
		ImageAttributes attributes = imageTransformer.execute( getImageAttributesAction( "images/getAttributes.pdf" ) );
		assertEquals( ImageType.PDF, attributes.getType() );
		assertEquals( 640, attributes.getDimensions().getWidth() );
		assertEquals( 125, attributes.getDimensions().getHeight() );
	}

	@Test
	public void getImageAttributesTiff() {
		ImageAttributes attributes =
				imageTransformer.execute( getImageAttributesAction( "images/getAttributes.tiff" ) );
		assertEquals( ImageType.TIFF, attributes.getType() );
		assertEquals( 640, attributes.getDimensions().getWidth() );
		assertEquals( 125, attributes.getDimensions().getHeight() );
	}

	@Test(expected = ImageModificationException.class)
	public void getImageAttributesForUnrecognizedByteStream() {
		imageTransformer.execute(
				new GetImageAttributesAction( new ByteArrayInputStream( "This is not an image.".getBytes() ) ) );
	}

	@Test
	@Ignore
	public void cropJpgToJpg() throws Exception {
		ImageModifyAction action = modifyAction(
				ImageType.JPEG,
				"images/cropCorrectness.jpeg",
				270,
				580,
				1000,
				140,
				270,
				580,
				ImageType.JPEG );
		InMemoryImageSource result = imageTransformer.execute( action );
		assertNotNull( result );
		assertNotNull( result.getImageBytes() );
		assertTrue( imagesAreEqual( bufferedImage( result.getImageBytes() ),
		                            bufferedImageFromClassPath( "images/cropJpgToJpg.jpeg" ) ) );
	}

	@Test
	public void cropPngToPng() throws Exception {
		ImageModifyAction action = modifyAction(
				ImageType.PNG,
				"images/cropCorrectness.png",
				270,
				580,
				1000,
				140,
				270,
				580,
				ImageType.PNG );
		InMemoryImageSource result = imageTransformer.execute( action );
		assertNotNull( result );
		assertNotNull( result.getImageBytes() );
		assertTrue( imagesAreEqual( bufferedImage( result.getImageBytes() ),
		                            bufferedImageFromClassPath( "images/cropPngToPng.png" ) ) );
	}

	@Test
	public void cropJpgToPng() throws Exception {
		ImageModifyAction action = modifyAction(
				ImageType.JPEG,
				"images/cropCorrectness.jpeg",
				270,
				580,
				1000,
				140,
				270,
				580,
				ImageType.PNG );
		InMemoryImageSource result = imageTransformer.execute( action );
		assertNotNull( result );
		assertNotNull( result.getImageBytes() );
		assertTrue( imagesAreEqual( bufferedImage( result.getImageBytes() ),
		                            bufferedImageFromClassPath( "images/cropJpgToPng.png" ) ) );
	}

	@Test
	@Ignore
	public void cropPngToJpg() throws Exception {
		ImageModifyAction action = modifyAction(
				ImageType.PNG,
				"images/cropCorrectness.png",
				270,
				580,
				1000,
				140,
				270,
				580,
				ImageType.JPEG );
		InMemoryImageSource result = imageTransformer.execute( action );
		assertNotNull( result );
		assertNotNull( result.getImageBytes() );
		assertTrue( imagesAreEqual( bufferedImage( result.getImageBytes() ),
		                            bufferedImageFromClassPath( "images/cropPngToJpg.jpeg" ) ) );
	}

	@Test
	public void transparentPngToPng() throws Exception {
		ImageModifyAction action = modifyAction(
				ImageType.PNG,
				"images/transparency.png",
				100,
				100,
				0,
				0,
				100,
				100,
				ImageType.PNG );
		InMemoryImageSource result = imageTransformer.execute( action );
		assertNotNull( result );
		assertNotNull( result.getImageBytes() );
		assertTrue( imagesAreEqual( bufferedImage( result.getImageBytes() ),
		                            bufferedImageFromClassPath( "images/transparentPngToPng.png" ) ) );
	}

	@Test
	@Ignore
	public void transparentPngToJpg() throws Exception {
		ImageModifyAction action = modifyAction(
				ImageType.PNG,
				"images/transparency.png",
				100,
				100,
				0,
				0,
				100,
				100,
				ImageType.JPEG );
		InMemoryImageSource result = imageTransformer.execute( action );
		assertNotNull( result );
		assertNotNull( result.getImageBytes() );
		assertTrue( imagesAreEqual( bufferedImage( result.getImageBytes() ),
		                            bufferedImageFromClassPath( "images/transparentPngToJpg.jpg" ) ) );
	}

	@Test
	public void getOrder() throws Exception {
		assertEquals( 3, imageTransformer.getOrder() );
	}

	@Test
	public void animated_gif_to_gif() throws Exception {
		ImageModifyAction action = modifyAction(
				ImageType.GIF,
				"images/animated.gif",
				250,
				250,
				50,
				50,
				250,
				250,
				ImageType.GIF );
		InMemoryImageSource result = imageTransformer.execute( action );
		assertNotNull( result );
		assertNotNull( result.getImageBytes() );

		Image image = new Image();
		image.setImageType( ImageType.GIF );
		image.setOriginalPath( "bla" );
		image.setId( 0L );
		imageStoreService.storeOriginalImage( image, result.getImageBytes() );
	}

	@Test
	public void animated_gif_to_jpeg() throws Exception {
		ImageModifyAction action = modifyAction(
				ImageType.GIF,
				"images/animated.gif",
				250,
				250,
				50,
				50,
				250,
				250,
				ImageType.JPEG );
		InMemoryImageSource result = imageTransformer.execute( action );
		assertNotNull( result );
		assertNotNull( result.getImageBytes() );

		Image image = new Image();
		image.setImageType( ImageType.JPEG );
		image.setOriginalPath( "bla" );
		image.setId( 1L );
		imageStoreService.storeOriginalImage( image, result.getImageBytes() );
	}

	private ImageCalculateDimensionsAction calculateDimensionsAction( ImageType imageType ) {
		return new ImageCalculateDimensionsAction( new StreamImageSource( imageType, (InputStream) null ) );
	}

	private GetImageAttributesAction getImageAttributesAction() {
		return new GetImageAttributesAction( null );
	}

	private ImageModifyAction modifyAction( ImageType sourceType,
	                                        String classPath,
	                                        int outputWidth,
	                                        int outputHeight,
	                                        int cropX,
	                                        int cropY,
	                                        int cropWidth,
	                                        int cropHeight,
	                                        ImageType outputType ) {
		InputStream imageStream = getClass().getClassLoader().getResourceAsStream( classPath );
		return new ImageModifyAction(
				new StreamImageSource( sourceType, imageStream ),
				outputWidth,
				outputHeight,
				cropX,
				cropY,
				cropWidth,
				cropHeight,
				0,
				0,
				outputType );
	}

	private ImageModifyAction modifyAction( ImageType imageType ) {
		return new ImageModifyAction( new StreamImageSource( imageType, (InputStream) null ), 0, 0, 0, 0, 0, 0, 0, 0,
		                              null );
	}

	private ImageCalculateDimensionsAction calculateDimensionsAction( ImageType imageType, String classPath ) {
		InputStream imageStream = getClass().getClassLoader().getResourceAsStream( classPath );
		return new ImageCalculateDimensionsAction( new StreamImageSource( imageType, imageStream ) );
	}

	private GetImageAttributesAction getImageAttributesAction( String classPath ) {
		InputStream imageStream = getClass().getClassLoader().getResourceAsStream( classPath );
		return new GetImageAttributesAction( imageStream );
	}

	@Configuration
	@PropertySource( "classpath:integrationtests.properties" )
	static class Config
	{
		@Bean
		public ImageMagickImageTransformer imageMagickImageTransformer( @Value("${transformer.imagemagick.path}") String imageMagickPath ) {
			return new ImageMagickImageTransformer( 3, imageMagickPath, true, true );
		}

		@Bean
		public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
			return new PropertySourcesPlaceholderConfigurer();
		}

		@Bean
		public ImageStoreService imageStoreService( @Value("${imagestore.folder}") String storeFolder ) throws IOException {
			return new ImageStoreServiceImpl( new File( storeFolder ).toPath(), "", "" );
		}

		@Bean
		public ImageContextService imageContextService() {
			return mock( ImageContextService.class );
		}

		@Bean
		public ImageService imageService() {
			return mock( ImageService.class );
		}
	}
}
