//package test.transformers.imagemagick;
//
//import com.foreach.imageserver.core.business.Dimensions;
//import com.foreach.imageserver.core.business.ImageType;
//import com.foreach.imageserver.core.transformers.ImageAttributes;
//import com.foreach.imageserver.core.transformers.ImageSource;
//import com.foreach.imageserver.core.transformers.ImageTransformCommand;
//import com.foreach.imageserver.core.transformers.SimpleImageSource;
//import com.foreach.imageserver.core.transformers.imagemagick.ImageMagickTransformCommandExecutor;
//import com.foreach.imageserver.dto.*;
//import lombok.SneakyThrows;
//import com.github.geko444.im4java.core.IMOperation;
//import com.github.geko444.im4java.process.ProcessStarter;
//import org.junit.Ignore;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.PropertySource;
//import org.springframework.core.env.Environment;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.testcontainers.containers.GenericContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//import java.io.File;
//import java.io.InputStream;
//import java.util.function.Consumer;
//import java.util.function.Function;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static support.ImageUtils.*;
//
///**
// * @author Arne Vandamme
// * @since 5.0.0
// */
//@ContextConfiguration(classes = TestImageMagickTransformCommandExecutor.Config.class)
//@ExtendWith(SpringExtension.class)
//@Testcontainers
//@Ignore
//public class TestImageMagickTransformCommandExecutor
//{
//	@Container
//	public static GenericContainer imageserverContainer = ImageServerTestContainer.CONTAINER;
//
//	private final Function<Consumer<ImageTransformDto.ImageTransformDtoBuilder>, ImageTransformCommand> cropTestImage = transform ->
//			createTransformCommand( image( "images/cropCorrectness.png" ),
//			                        new ImageAttributes( ImageType.PNG, new Dimensions( 2000, 1000 ), 1 ),
//			                        transform );
//
//	private final Function<Consumer<ImageTransformDto.ImageTransformDtoBuilder>, ImageTransformCommand> transparencyTestImage = transform ->
//			createTransformCommand( image( "images/transparency.png" ),
//			                        new ImageAttributes( ImageType.PNG, new Dimensions( 100, 100 ), 1 ),
//			                        transform );
//
//	private final Function<Consumer<ImageTransformDto.ImageTransformDtoBuilder>, ImageTransformCommand> pdfTestImage = transform ->
//			createTransformCommand( image( "images/sample-pdf.pdf" ),
//			                        new ImageAttributes( ImageType.PDF, new Dimensions( 612, 792 ), 5 ),
//			                        transform );
//
//	private final Function<Consumer<ImageTransformDto.ImageTransformDtoBuilder>, ImageTransformCommand> svgTestImage = transform ->
//			createTransformCommand( image( "images/across-logo.svg" ),
//			                        new ImageAttributes( ImageType.SVG, new Dimensions( 82, 95 ), 1 ),
//			                        transform );
//
//	@Autowired
//	private ImageMagickTransformCommandExecutor executor;
//
//	private static ImageTransformCommand createTransformCommand( ImageSource image,
//	                                                             ImageAttributes attributes,
//	                                                             Consumer<ImageTransformDto.ImageTransformDtoBuilder> transformBuilder ) {
//		ImageTransformDto.ImageTransformDtoBuilder transformDtoBuilder = ImageTransformDto.builder();
//		transformBuilder.accept( transformDtoBuilder );
//
//		return ImageTransformCommand
//				.builder()
//				.originalImage( image )
//				.originalImageAttributes( attributes )
//				.transform( transformDtoBuilder.build() )
//				.build();
//	}
//
//	@Test
//	public void transparentPngToTransparentGrayscalePng() {
//		ImageTransformCommand convertToGrayscale = transparencyTestImage.apply( t -> t.colorSpace( ColorSpaceDto.GRAYSCALE ) );
//		assertArguments( "-filter Box - -colorspace gray +profile * -strip -quality 85.0 png:-", convertToGrayscale );
//		assertImage( "images/transparentPngToGrayscale.png", convertToGrayscale );
//	}
//
//	@Test
//	public void transparentPngToMonochromePng() {
//		ImageTransformCommand convertToMonochrome = transparencyTestImage.apply( t -> t.colorSpace( ColorSpaceDto.MONOCHROME ) );
//		assertArguments( "-filter Box - -monochrome +profile * -strip -quality 85.0 png:-", convertToMonochrome );
//		assertImage( "images/transparentPngToMonochrome.png", convertToMonochrome );
//	}
//
//	@Test
//	public void transparentPngToFlattenedPng() {
//		ImageTransformCommand applyWhiteBackground = transparencyTestImage.apply( t -> t.backgroundColor( ColorDto.WHITE ) );
//		assertArguments( "-filter Box - -background #ffffff -extent 0x0 +matte +profile * -strip -quality 85.0 png:-", applyWhiteBackground );
//		assertImage( "images/transparentPngToPngFlat.png", applyWhiteBackground );
//	}
//
//	@Test
//	public void transparentPngToNonTransparentJpeg() {
//		assertArguments(
//				"-filter Box - -background white -extent 0x0 +matte +profile * -strip -quality 85.0 jpeg:-",
//				transparencyTestImage.apply( t -> t.outputType( ImageTypeDto.JPEG ) )
//		);
//	}
//
//	@Test
//	public void transparentPngBackgroundColorFill() {
//		ImageTransformCommand applyCustomBackground = transparencyTestImage.apply( t -> t.backgroundColor( ColorDto.from( "#aabbff" ) ) );
//		assertArguments( "-filter Box - -background #aabbff -extent 0x0 +matte +profile * -strip -quality 85.0 png:-", applyCustomBackground );
//		assertImage( "images/transparentPngToPngBackground.png", applyCustomBackground );
//	}
//
//	@Test
//	public void transparentPngBackgroundColorFillAndGrayscale() {
//		ImageTransformCommand applyBackgroundToGrayscale
//				= transparencyTestImage.apply( t -> t.backgroundColor( ColorDto.from( "#aabbff" ) ).colorSpace( ColorSpaceDto.GRAYSCALE ) );
//		assertArguments( "-filter Box - -background #aabbff -extent 0x0 +matte -colorspace gray +profile * -strip -quality 85.0 png:-",
//		                 applyBackgroundToGrayscale );
//		assertImage( "images/transparentPngToPngBackgroundGrayscale.png", applyBackgroundToGrayscale );
//	}
//
//	@Test
//	public void cropPngWithoutOutputDimensions() {
//		ImageTransformCommand createCrop = cropTestImage.apply( t -> t.crop( CropDto.builder().x( 1000 ).y( 140 ).width( 270 ).height( 580 ).build() ) );
//		assertArguments( "-filter Box - -crop 270x580+1000+140 +profile * -strip -quality 85.0 png:-", createCrop );
//		assertImage( "images/cropPngToPng.png", createCrop );
//	}
//
//	@Test
//	public void cropPngWithOutputDimensionsAndConvertToGrayscale() {
//		ImageTransformCommand createCrop = cropTestImage.apply( t -> t
//				.width( 135 )
//				.height( 290 )
//				.crop( CropDto.builder().x( 1000 ).y( 140 ).width( 270 ).height( 580 ).build() )
//				.colorSpace( ColorSpaceDto.GRAYSCALE )
//		);
//
//		assertArguments( "-filter Box - -crop 270x580+1000+140 -colorspace gray -resize 135x290! +profile * -strip -quality 85.0 png:-", createCrop );
//		assertImage( "images/cropPngToSmallerGrayscale.png", createCrop );
//	}
//
//	@Test
//	public void makeColorTransparent() {
//		ImageTransformCommand createCrop = cropTestImage.apply( t -> t
//				.crop( CropDto.builder().x( 1000 ).y( 140 ).width( 270 ).height( 580 ).build() )
//				.alphaColor( ColorDto.from( "#e32e2e" ) )
//		);
//
//		assertArguments( "-filter Box - -crop 270x580+1000+140 -transparent #e32e2e -colorspace TRANSPARENT +profile * -strip -quality 85.0 png:-",
//		                 createCrop );
//		assertImage( "images/cropPngToTransparent.png", createCrop );
//	}
//
//	@Test
//	public void replaceColorBySettingTransparentAndBackground() {
//		ImageTransformCommand createCrop = cropTestImage.apply( t -> t
//				.crop( CropDto.builder().x( 1000 ).y( 140 ).width( 270 ).height( 580 ).build() )
//				.alphaColor( ColorDto.from( "#e32e2e" ) )
//				.backgroundColor( ColorDto.from( "#0000ff" ) )
//		);
//
//		assertArguments(
//				"-filter Box - -crop 270x580+1000+140 -transparent #e32e2e -background #0000ff -extent 0x0 +matte -colorspace TRANSPARENT +profile * -strip -quality 85.0 png:-",
//				createCrop );
//		assertImage( "images/cropPngAndReplaceRed.png", createCrop );
//	}
//
//	@Test
//	public void replaceFirstPdfPageBackgroundColor() {
//		ImageTransformCommand createCrop = pdfTestImage.apply( t -> t
//				.alphaColor( ColorDto.WHITE )
//				.backgroundColor( ColorDto.from( "yellow" ) )
//				.outputType( ImageTypeDto.PNG )
//		);
//
//		assertArguments(
//				"-filter Box -density 300x300 -[0] -transparent #ffffff -background yellow -extent 0x0 +matte -colorspace TRANSPARENT -resize 612x792 +profile * -strip -quality 85.0 png:-",
//				createCrop );
//		assertImage( "images/pdfFirstPageBackground.png", createCrop );
//	}
//
//	@Test
//	public void secondPdfPageTransparent() {
//		ImageTransformCommand createCrop = pdfTestImage.apply( t -> t
//				.alphaColor( ColorDto.WHITE )
//				.scene( 1 )
//				.outputType( ImageTypeDto.PNG )
//				.width( 1224 )
//				.height( 1584 )
//				.dpi( 150 )
//		);
//
//		assertArguments(
//				"-filter Box -density 150x150 -[1] -transparent #ffffff -colorspace TRANSPARENT -resize 1224x1584! +profile * -strip -quality 85.0 png:-",
//				createCrop );
//		assertImage( "images/pdfSecondPageTransparent.png", createCrop );
//	}
//
//	@Test
//	public void defaultDpiIsDeterminedBasedOnOutput() {
//		// image width is 612 on 72dpi - 2250 pixels corresponds with the 300 dpi default - above that a higher dpi should be auto-selected
//		assertArguments(
//				"-filter Box -density 300x300 -[0] -resize 612x792 +profile * -strip -quality 85.0 png:-",
//				pdfTestImage.apply( t -> t.outputType( ImageTypeDto.PNG ) )
//		);
//		assertArguments(
//				"-filter Box -density 300x300 -[0] -resize 2250x +profile * -strip -quality 85.0 png:-",
//				pdfTestImage.apply( t -> t.width( 2250 ).outputType( ImageTypeDto.PNG ) )
//		);
//		assertArguments(
//				"-filter Box -density 600x600 -[0] -resize 3000x +profile * -strip -quality 85.0 png:-",
//				pdfTestImage.apply( t -> t.width( 3000 ).outputType( ImageTypeDto.PNG ) )
//		);
//		assertArguments(
//				"-filter Box -density 600x600 -[0] -resize x4000 +profile * -strip -quality 85.0 png:-",
//				pdfTestImage.apply( t -> t.height( 4000 ).outputType( ImageTypeDto.PNG ) )
//		);
//	}
//
//	@Test
//	public void cropCoordinatesAreAdjustedToDpi() {
//		assertArguments(
//				"-filter Box -density 72x72 - -crop 10x20+50+100 -resize 200x +profile * -strip -quality 85.0 pdf:-",
//				pdfTestImage.apply( t -> t.crop( CropDto.builder().width( 10 ).height( 20 ).x( 50 ).y( 100 ).build() ).dpi( 72 ).width( 200 ) )
//		);
//		assertArguments(
//				"-filter Box -density 144x144 - -crop 20x40+100+200 -resize 200x +profile * -strip -quality 85.0 pdf:-",
//				pdfTestImage.apply( t -> t.crop( CropDto.builder().width( 10 ).height( 20 ).x( 50 ).y( 100 ).build() ).dpi( 144 ).width( 200 ) )
//		);
//		assertArguments(
//				"-filter Box -density 300x300 - -crop 41x83+208+416 -resize 200x +profile * -strip -quality 85.0 pdf:-",
//				pdfTestImage.apply( t -> t.crop( CropDto.builder().width( 10 ).height( 20 ).x( 50 ).y( 100 ).build() ).width( 200 ) )
//		);
//		assertArguments(
//				"-filter Box -density 600x600 - -crop 83x166+416+833 -resize 200x +profile * -strip -quality 85.0 pdf:-",
//				pdfTestImage.apply( t -> t.crop( CropDto.builder().width( 10 ).height( 20 ).x( 50 ).y( 100 ).build() ).dpi( 600 ).width( 200 ) )
//		);
//	}
//
//	@Test
//	public void cropFromPdfPageAndMakeTransparent() {
//		ImageTransformCommand createCrop = pdfTestImage.apply( t -> t
//				.alphaColor( ColorDto.WHITE )
//				.outputType( ImageTypeDto.PNG )
//				.crop( CropDto.builder().width( 512 ).height( 592 ).x( 50 ).y( 100 ).build() )
//				.width( 512 )
//				.dpi( 144 )
//		);
//
//		assertArguments(
//				"-filter Box -density 144x144 -[0] -crop 1024x1184+100+200 -transparent #ffffff -colorspace TRANSPARENT -resize 512x +profile * -strip -quality 85.0 png:-",
//				createCrop
//		);
//		assertImage( "images/pdfCropSegment.png", createCrop );
//	}
//
//	@Test
//	public void svgBackgroundIsTransparent() {
//		ImageTransformCommand renderSvg = svgTestImage.apply( t -> t.outputType( ImageTypeDto.PNG ) );
//
//		assertArguments(
//				"-filter Box -background transparent - +profile * -strip -quality 85.0 png:-",
//				renderSvg
//		);
//		assertImage( "images/svgToTransparent.png", renderSvg );
//	}
//
//	@Test
//	public void svgBackgroundColoring() {
//		ImageTransformCommand renderSvg = svgTestImage.apply( t -> t.outputType( ImageTypeDto.PNG ).backgroundColor( ColorDto.from( "yellow" ) ) );
//
//		assertArguments(
//				"-filter Box -background transparent - -background yellow -extent 0x0 +matte +profile * -strip -quality 85.0 png:-",
//				renderSvg
//		);
//		assertImage( "images/svgWithBackground.png", renderSvg );
//	}
//
//	private void assertArguments( String expected, ImageTransformCommand command ) {
//		IMOperation operation = executor.createIMOperation( command );
//		assertThat( operation ).isNotNull();
//		assertThat( operation.toString().trim() ).isEqualTo( expected );
//	}
//
//	@SneakyThrows
//	private void assertImage( String expected, ImageTransformCommand command ) {
//		executor.execute( command );
//		try (InputStream is = command.getExecutionResult().getImageStream()) {
//			assertTrue( imagesAreEqual( bufferedImage( is ), bufferedImageFromClassPath( expected ) ) );
//		}
//	}
//
//	private ImageSource image( String path ) {
//		return new SimpleImageSource( null, () -> getClass().getClassLoader().getResourceAsStream( path ) );
//	}
//
//	@Configuration
//	@PropertySource("classpath:integrationtests.properties")
//	static class Config
//	{
//		@Bean
//		public ImageMagickTransformCommandExecutor imageMagickTransformCommandExecutor( Environment environment ) {
//			ProcessStarter.setGlobalSearchPath( new File( environment.getProperty( "transformers.imageMagick.path" ) ).getAbsolutePath() );
//			System.setProperty( "im4java.useGM", "true" );
//
//			return new ImageMagickTransformCommandExecutor();
//		}
//	}
//}
