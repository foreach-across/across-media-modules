//package it;
//
//import com.foreach.across.config.AcrossContextConfigurer;
//import com.foreach.across.core.AcrossContext;
//import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
//import com.foreach.across.modules.filemanager.FileManagerModule;
//import com.foreach.across.modules.properties.PropertiesModule;
//import com.foreach.across.test.AcrossTestConfiguration;
//import com.foreach.imageserver.client.ImageServerClient;
//import com.foreach.imageserver.core.ImageServerCoreModule;
//import com.foreach.imageserver.core.business.ImageResolution;
//import com.foreach.imageserver.core.business.ImageType;
//import com.foreach.imageserver.core.services.ImageContextService;
//import com.foreach.imageserver.core.services.ImageService;
//import com.foreach.imageserver.core.services.exceptions.ImageStoreException;
//import com.foreach.imageserver.dto.*;
//import lombok.SneakyThrows;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.NoSuchBeanDefinitionException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.PropertySource;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.test.context.web.WebAppConfiguration;
//import org.testcontainers.containers.GenericContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//import java.awt.image.BufferedImage;
//import java.io.ByteArrayInputStream;
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.text.ParseException;
//import java.util.Collection;
//import java.util.Date;
//import java.util.List;
//
//import static com.foreach.imageserver.core.ImageServerCoreModule.NAME;
//import static com.foreach.imageserver.core.ImageServerCoreModuleSettings.*;
//import static com.foreach.imageserver.core.config.WebConfiguration.IMAGE_REQUEST_HASH_BUILDER;
//import static com.foreach.imageserver.dto.ColorDto.from;
//import static com.foreach.imageserver.dto.ColorSpaceDto.GRAYSCALE;
//import static com.foreach.imageserver.dto.ImageConvertDto.builder;
//import static com.foreach.imageserver.dto.ImageTypeDto.*;
//import static java.lang.System.getProperty;
//import static java.util.Arrays.asList;
//import static java.util.Collections.singleton;
//import static java.util.Collections.singletonList;
//import static java.util.EnumSet.allOf;
//import static java.util.UUID.randomUUID;
//import static javax.imageio.ImageIO.read;
//import static org.apache.commons.io.IOUtils.toByteArray;
//import static org.apache.commons.lang3.time.DateUtils.parseDate;
//import static org.junit.jupiter.api.Assertions.*;
//import static test.transformers.imagemagick.ImageServerTestContainer.CONTAINER;
//
///**
// * @author Arne Vandamme
// */
//@ExtendWith(SpringExtension.class)
//@DirtiesContext
//@WebAppConfiguration
//@ContextConfiguration(classes = ITLocalImageServerClient.Config.class)
//@TestPropertySource(properties = { "spring.jpa.show-sql=true" })
//@Testcontainers
//public class ITLocalImageServerClient
//{
//	@Container
//	private static final GenericContainer imageserverContainer = CONTAINER;
//
//	@Autowired(required = false)
//	private ImageServerClient imageServerClient;
//
//	@Autowired
//	private ImageService imageService;
//
//	@Autowired
//	private ImageContextService imageContextService;
//
//	@Autowired
//	private AcrossContextBeanRegistry beanRegistry;
//
//	@Test
//	public void noImageRequestHashBuilderShouldBeCreated() {
//		assertThrows( NoSuchBeanDefinitionException.class, () -> {
//			assertNull( beanRegistry.getBeanFromModule( NAME,
//			                                            IMAGE_REQUEST_HASH_BUILDER ) );
//		} );
//	}
//
//	@BeforeEach
//	public void registerResolutions() {
//		registerResolution( 640, 480 );
//		registerResolution( 320, 240 );
//	}
//
//	private void registerResolution( int width, int height ) {
//		ImageResolution resolution = imageService.getResolution( width, height );
//
//		if ( resolution == null ) {
//			resolution = new ImageResolution();
//			resolution.setWidth( width );
//			resolution.setHeight( height );
//			resolution.setConfigurable( true );
//			resolution.setContexts( singleton( imageContextService.getByCode( "default" ) ) );
//			resolution.setAllowedOutputTypes( allOf( ImageType.class ) );
//
//			imageService.saveImageResolution( resolution );
//		}
//	}
//
//	@Test
//	public void clientShouldBeCreated() {
//		assertNotNull( imageServerClient );
//		assertEquals( "http://somehost/img", imageServerClient.getImageServerUrl() );
//	}
//
//	@Test
//	public void uploadingKnownResourceImage() throws Exception {
//		String externalId = randomUUID().toString();
//		byte[] imageData = image( "images/poppy_flower_nature.jpg" );
//		Date date = parseDate( "2013-05-14 13:33:22", "yyyy-MM-dd HH:mm:ss" );
//
//		ImageInfoDto fetchedInfo = imageServerClient.imageInfo( externalId );
//		assertFalse( fetchedInfo.isExisting() );
//		assertEquals( externalId, fetchedInfo.getExternalId() );
//
//		ImageInfoDto createdInfo = imageServerClient.loadImage( externalId, imageData, date );
//
//		assertTrue( createdInfo.isExisting() );
//		assertEquals( externalId, createdInfo.getExternalId() );
//		assertEquals( date, createdInfo.getCreated() );
//		assertEquals( new DimensionsDto( 1920, 1080 ), createdInfo.getDimensionsDto() );
//		assertEquals( JPEG, createdInfo.getImageType() );
//
//		fetchedInfo = imageServerClient.imageInfo( externalId );
//		assertEquals( createdInfo, fetchedInfo );
//
//		try (InputStream inputStream = imageServerClient.imageStream( externalId, new ImageModificationDto(), new ImageVariantDto( JPEG ) )) {
//			byte[] originalSizeData = toByteArray( inputStream );
//
//			ImageInfoDto modifiedUpload = imageServerClient.loadImage( randomUUID().toString(), originalSizeData );
//			assertEquals( new DimensionsDto( 1920, 1080 ), modifiedUpload.getDimensionsDto() );
//			assertEquals( JPEG, modifiedUpload.getImageType() );
//		}
//
//		try (InputStream inputStream = imageServerClient.imageStream( externalId, "default", 640, 480, PNG )) {
//			byte[] scaledDate = toByteArray( inputStream );
//
//			ImageInfoDto modifiedUpload = imageServerClient.loadImage( randomUUID().toString(), scaledDate );
//			assertEquals( new DimensionsDto( 640, 480 ), modifiedUpload.getDimensionsDto() );
//			assertEquals( PNG, modifiedUpload.getImageType() );
//		}
//
//		// Delete existing
//		assertTrue( imageServerClient.deleteImage( externalId ) );
//		assertFalse( imageServerClient.imageInfo( externalId ).isExisting() );
//		assertFalse( imageServerClient.imageExists( externalId ) );
//
//		assertFalse( imageServerClient.deleteImage( externalId ) );
//
//		imageServerClient.loadImage( externalId, imageData, date );
//		assertTrue( imageServerClient.imageExists( externalId ) );
//	}
//
//	@Test
//	public void replacingImage() throws Exception {
//		String externalId = randomUUID().toString();
//		byte[] imageOne =
//				image( "images/poppy_flower_nature.jpg" );
//		byte[] imageTwo =
//				image( "images/transparentPngToPng.png" );
//
//		ImageInfoDto createdInfo = imageServerClient.loadImage( externalId, imageOne );
//		assertTrue( createdInfo.isExisting() );
//		assertEquals( externalId, createdInfo.getExternalId() );
//		assertEquals( new DimensionsDto( 1920, 1080 ), createdInfo.getDimensionsDto() );
//		assertEquals( JPEG, createdInfo.getImageType() );
//
//		ImageInfoDto replaced;
//		boolean failed = false;
//
//		try {
//			imageServerClient.loadImage( externalId, imageTwo );
//		}
//		catch ( ImageStoreException ise ) {
//			failed = true;
//		}
//
//		assertTrue( failed );
//
//		replaced = imageServerClient.loadImage( externalId, imageTwo, true );
//		assertNotNull( replaced );
//		assertTrue( replaced.isExisting() );
//		assertEquals( externalId, replaced.getExternalId() );
//		assertEquals( new DimensionsDto( 100, 100 ), replaced.getDimensionsDto() );
//		assertEquals( PNG, replaced.getImageType() );
//
//		ImageInfoDto fetched = imageServerClient.imageInfo( externalId );
//		assertNotNull( fetched );
//		assertTrue( fetched.isExisting() );
//		assertEquals( externalId, fetched.getExternalId() );
//		assertEquals( new DimensionsDto( 100, 100 ), fetched.getDimensionsDto() );
//	}
//
//	@Test
//	public void registerModification() throws ParseException, IOException {
//		String externalId = randomUUID().toString();
//		byte[] imageData =
//				image( "images/poppy_flower_nature.jpg" );
//
//		ImageInfoDto uploaded = imageServerClient.loadImage( externalId, imageData );
//		assertTrue( uploaded.isExisting() );
//
//		Collection<ImageModificationDto> modifications = imageServerClient.listModifications( externalId, "default" );
//		assertTrue( modifications.isEmpty() );
//
//		ImageModificationDto modificationDto = new ImageModificationDto( 640, 480 );
//		modificationDto.setCrop( new CropDto( 10, 10, 400, 300 ) );
//		modificationDto.setDensity( new DimensionsDto( 300, 300 ) );
//
//		imageServerClient.registerImageModification( externalId, "default", modificationDto );
//
//		modifications = imageServerClient.listModifications( externalId, "default" );
//		assertEquals( 1, modifications.size() );
//
//		ImageModificationDto dtoWithSource = new ImageModificationDto( modificationDto );
//		dtoWithSource.getCrop().setSource( new DimensionsDto( 1920, 1080 ) );
//
//		assertEquals( dtoWithSource, modifications.iterator().next() );
//	}
//
//	private byte[] image( String s ) throws IOException {
//		return toByteArray(
//				getClass().getClassLoader().getResourceAsStream( s ) );
//	}
//
//	@Test
//	public void registerModifications() throws ParseException, IOException {
//		String externalId = randomUUID().toString();
//		byte[] imageData =
//				image( "images/poppy_flower_nature.jpg" );
//
//		ImageInfoDto uploaded = imageServerClient.loadImage( externalId, imageData );
//		assertTrue( uploaded.isExisting() );
//
//		Collection<ImageModificationDto> modifications = imageServerClient.listModifications( externalId, "default" );
//		assertTrue( modifications.isEmpty() );
//
//		ImageModificationDto modificationDto = new ImageModificationDto( 640, 480 );
//		modificationDto.setCrop( new CropDto( 10, 10, 400, 300 ) );
//		modificationDto.setDensity( new DimensionsDto( 300, 300 ) );
//
//		ImageModificationDto modificationDto2 = new ImageModificationDto( 320, 240 );
//		modificationDto.setCrop( new CropDto( 10, 10, 100, 150 ) );
//		modificationDto.setDensity( new DimensionsDto( 100, 100 ) );
//
//		List<ImageModificationDto> imageModificationDtos = asList( modificationDto, modificationDto2 );
//
//		imageServerClient.registerImageModifications( externalId, "default", imageModificationDtos );
//
//		modifications = imageServerClient.listModifications( externalId, "default" );
//		assertEquals( 2, modifications.size() );
//
//		ImageModificationDto dtoWithSource = new ImageModificationDto( modificationDto );
//		dtoWithSource.getCrop().setSource( new DimensionsDto( 1920, 1080 ) );
//
//		assertEquals( dtoWithSource, modifications.iterator().next() );
//	}
//
//	@Test
//	public void renderProvidedImage() throws IOException {
//		byte[] imageData = image( "images/poppy_flower_nature.jpg" );
//
//		InputStream renderedImage = imageServerClient.imageStream( imageData, new ImageModificationDto( 100, 100 ),
//		                                                           new ImageVariantDto( PNG ) );
//
//		byte[] scaledDate = toByteArray( renderedImage );
//
//		ImageInfoDto modifiedUpload = imageServerClient.loadImage( randomUUID().toString(), scaledDate );
//		assertEquals( new DimensionsDto( 100, 100 ), modifiedUpload.getDimensionsDto() );
//		assertEquals( PNG, modifiedUpload.getImageType() );
//	}
//
//	@Test
//	public void imageInfoForJpeg() throws IOException {
//		byte[] imageData = image( "images/poppy_flower_nature.jpg" );
//
//		ImageInfoDto imageInfoDto = imageServerClient.imageInfo( imageData );
//		assertEquals( 1, imageInfoDto.getSceneCount() );
//		assertEquals( new DimensionsDto( 1920, 1080 ), imageInfoDto.getDimensionsDto() );
//		assertEquals( JPEG, imageInfoDto.getImageType() );
//	}
//
//	@Test
//	@SneakyThrows
//	public void imageInfoForPdf() {
//		byte[] pdfData = image( "images/sample-pdf.pdf" );
//
//		ImageInfoDto imageInfoDto = imageServerClient.imageInfo( pdfData );
//		assertEquals( 5, imageInfoDto.getSceneCount() );
//		assertEquals( new DimensionsDto( 612, 792 ), imageInfoDto.getDimensionsDto() );
//		assertEquals( PDF, imageInfoDto.getImageType() );
//	}
//
//	@Test
//	@SneakyThrows
//	public void sceneCountIsPersisted() {
//		String externalId = randomUUID().toString();
//		byte[] imageData = image( "images/sample-pdf.pdf" );
//
//		ImageInfoDto fetchedInfo = imageServerClient.imageInfo( externalId );
//		assertFalse( fetchedInfo.isExisting() );
//		assertEquals( 0, fetchedInfo.getSceneCount() );
//		assertEquals( externalId, fetchedInfo.getExternalId() );
//
//		ImageInfoDto createdInfo = imageServerClient.loadImage( externalId, imageData );
//
//		assertTrue( createdInfo.isExisting() );
//		assertEquals( externalId, createdInfo.getExternalId() );
//		assertEquals( 5, createdInfo.getSceneCount() );
//		assertEquals( new DimensionsDto( 612, 792 ), createdInfo.getDimensionsDto() );
//		assertEquals( PDF, createdInfo.getImageType() );
//
//		fetchedInfo = imageServerClient.imageInfo( externalId );
//		assertEquals( new DimensionsDto( 612, 792 ), fetchedInfo.getDimensionsDto() );
//		assertEquals( createdInfo.getSceneCount(), fetchedInfo.getSceneCount() );
//		assertEquals( PDF, createdInfo.getImageType() );
//	}
//
//	@Test
//	@SneakyThrows
//	public void convertImage() {
//		ImageConvertDto imageConvertDto = builder()
//				.image( image( "images/poppy_flower_nature.jpg" ) )
//				.transformation( "flower-*", singletonList( ImageTransformDto.builder()
//				                                                             .dpi( 300 )
//				                                                             .colorSpace(
//						                                                             GRAYSCALE )
//				                                                             .outputType(
//						                                                             PNG )
//				                                                             .build() ) )
//				.build();
//
//		ImageConvertResultDto imageConvertResultDto = imageServerClient.convertImage( imageConvertDto );
//
//		assertEquals( 1, imageConvertResultDto.getTotal() );
//
//		assertEquals( 1, imageConvertResultDto.getKeys().size() );
//
//		String key = "flower-0";
//
//		assertEquals( key, imageConvertResultDto.getKeys().toArray()[0] );
//
//		assertEquals( 1, imageConvertResultDto.getPages().size() );
//		assertEquals( 0, imageConvertResultDto.getPages().toArray()[0] );
//
//		assertEquals( 1, imageConvertResultDto.getTransforms().size() );
//		ImageDto transformation = imageConvertResultDto.getTransforms().get( key );
//		assertEquals( PNG, transformation.getFormat() );
//	}
//
//	@Test
//	@SneakyThrows
//	public void convertSingleImage() {
//		ImageDto result = imageServerClient.convertImage( image( "images/poppy_flower_nature.jpg" ),
//		                                                  singletonList(
//				                                                  ImageTransformDto.builder()
//				                                                                   .dpi( 300 )
//				                                                                   .colorSpace(
//						                                                                   GRAYSCALE )
//				                                                                   .outputType(
//						                                                                   PNG )
//				                                                                   .build() ) );
//
//		assertEquals( PNG, result.getFormat() );
//		assertTrue( result.getImage().length > 0 );
//	}
//
//	@Test
//	@SneakyThrows
//	public void convertSingleRegisteredImage() {
//		String imageId = randomUUID().toString();
//		imageServerClient.loadImage( imageId, image( "images/poppy_flower_nature.jpg" ) );
//		ImageDto result = imageServerClient.convertImage( imageId,
//		                                                  singletonList(
//				                                                  ImageTransformDto.builder()
//				                                                                   .dpi( 300 )
//				                                                                   .colorSpace(
//						                                                                   GRAYSCALE )
//				                                                                   .outputType(
//						                                                                   PNG )
//				                                                                   .build() ) );
//
//		assertEquals( PNG, result.getFormat() );
//		assertTrue( result.getImage().length > 0 );
//	}
//
//	@Test
//	@SneakyThrows
//	public void convertImagePdf() {
//		ImageConvertDto imageConvertDto = builder()
//				.image( image( "images/sample-pdf.pdf" ) )
//				.pages( "1,3-6" )
//				.transformation( "sample-*", singletonList( ImageTransformDto.builder()
//				                                                             .dpi( 300 )
//				                                                             .colorSpace(
//						                                                             GRAYSCALE )
//				                                                             .outputType(
//						                                                             PNG )
//				                                                             .build() ) )
//				.build();
//
//		ImageConvertResultDto imageConvertResultDto = imageServerClient.convertImage( imageConvertDto );
//
//		assertEquals( 4, imageConvertResultDto.getTotal() );
//
//		assertEquals( 4, imageConvertResultDto.getKeys().size() );
//		assertTrue( imageConvertResultDto.getKeys().contains( "sample-1" ) );
//		assertTrue( imageConvertResultDto.getKeys().contains( "sample-3" ) );
//		assertTrue( imageConvertResultDto.getKeys().contains( "sample-4" ) );
//		assertTrue( imageConvertResultDto.getKeys().contains( "sample-5" ) );
//
//		assertEquals( 4, imageConvertResultDto.getPages().size() );
//		assertTrue( imageConvertResultDto.getPages().contains( 1 ) );
//		assertTrue( imageConvertResultDto.getPages().contains( 3 ) );
//		assertTrue( imageConvertResultDto.getPages().contains( 4 ) );
//		assertTrue( imageConvertResultDto.getPages().contains( 5 ) );
//
//		assertEquals( 4, imageConvertResultDto.getTransforms().size() );
//
//		ImageDto transformation = imageConvertResultDto.getTransforms().get( "sample-1" );
//		assertEquals( PNG, transformation.getFormat() );
//
//		transformation = imageConvertResultDto.getTransforms().get( "sample-3" );
//		assertEquals( PNG, transformation.getFormat() );
//
//		transformation = imageConvertResultDto.getTransforms().get( "sample-4" );
//		assertEquals( PNG, transformation.getFormat() );
//
//		transformation = imageConvertResultDto.getTransforms().get( "sample-5" );
//		assertEquals( PNG, transformation.getFormat() );
//	}
//
//	@Test
//	@SneakyThrows
//	public void convertResizeEpsRetina() {
//		ImageDto result = imageServerClient.convertImage( image( "images/kaaimangrootkleur.eps" ),
//		                                                  singletonList(
//				                                                  ImageTransformDto.builder()
//				                                                                   .height( 1536 )
//				                                                                   .quality( 100 )
//				                                                                   .outputType( PNG )
//				                                                                   .build() ) );
//
//		try (InputStream i = new ByteArrayInputStream( result.getImage() )) {
//			BufferedImage bimg = read( i );
//			assertEquals( 1536, bimg.getHeight() );
//		}
//	}
//
//	@Test
//	@SneakyThrows
//	public void convertImagePdfEchoBackgroundColor() {
//		ImageConvertDto imageConvertDto = builder()
//				.image( image( "images/55980.pdf" ) )
//				.pages( "0" )
//				.transformation( "echo-*", singletonList( ImageTransformDto.builder()
//				                                                           .height( 560 )
//				                                                           .backgroundColor(
//						                                                           from(
//								                                                           "#fff1e0" ) )
//				                                                           .dpi( 300 )
//				                                                           .quality( 100 )
//				                                                           .outputType( PNG )
//				                                                           .build() ) )
//		                                                 .build();
//
//		ImageConvertResultDto imageConvertResultDto = imageServerClient.convertImage( imageConvertDto );
//
//		assertEquals( 1, imageConvertResultDto.getTotal() );
//
//		assertEquals( 1, imageConvertResultDto.getKeys().size() );
//		assertTrue( imageConvertResultDto.getKeys().contains( "echo-0" ) );
//
//		assertEquals( 1, imageConvertResultDto.getPages().size() );
//		assertTrue( imageConvertResultDto.getPages().contains( 0 ) );
//
//		assertEquals( 1, imageConvertResultDto.getTransforms().size() );
//
//		ImageDto transformation = imageConvertResultDto.getTransforms().get( "echo-0" );
//		assertEquals( PNG, transformation.getFormat() );
//
//		try (InputStream i = new ByteArrayInputStream( transformation.getImage() )) {
//			BufferedImage bimg = read( i );
//			assertEquals( 560, bimg.getHeight() );
//		}
//	}
//
//	@Configuration
//	@AcrossTestConfiguration(modules = { FileManagerModule.NAME, PropertiesModule.NAME })
//	@PropertySource("classpath:integrationtests.properties")
//	protected static class Config implements AcrossContextConfigurer
//	{
//		@Value("${transformers.imageMagick.path}")
//		String imageMagickPath;
//
//		@Override
//		public void configure( AcrossContext context ) {
//			context.addModule( imageServerCoreModule() );
//		}
//
//		private ImageServerCoreModule imageServerCoreModule() {
//			ImageServerCoreModule imageServerCoreModule = new ImageServerCoreModule();
//			imageServerCoreModule.setProperty( IMAGE_STORE_FOLDER,
//			                                   new File( getProperty( "java.io.tmpdir" ), randomUUID().toString() ) );
//			imageServerCoreModule.setProperty( ROOT_PATH, "/imgsrvr" );
//			imageServerCoreModule.setProperty( PROVIDE_STACKTRACE, true );
//			imageServerCoreModule.setProperty( IMAGEMAGICK_ENABLED, true );
//			imageServerCoreModule.setProperty( IMAGEMAGICK_USE_GRAPHICSMAGICK, true );
//			imageServerCoreModule.setProperty( IMAGEMAGICK_PATH,
//			                                   imageMagickPath );
//			imageServerCoreModule.setProperty( CREATE_LOCAL_CLIENT, true );
//			imageServerCoreModule.setProperty( IMAGE_SERVER_URL, "http://somehost/img" );
//
//			return imageServerCoreModule;
//		}
//	}
//
//}
