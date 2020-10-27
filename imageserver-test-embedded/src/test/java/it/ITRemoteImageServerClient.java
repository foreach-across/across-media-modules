package it;

import com.foreach.imageserver.client.ImageServerClient;
import com.foreach.imageserver.client.ImageServerException;
import com.foreach.imageserver.client.RemoteImageServerClient;
import com.foreach.imageserver.dto.*;
import com.foreach.imageserver.test.embedded.ImageServerTestEmbeddedApplication;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.*;

import static com.foreach.imageserver.dto.ImageTypeDto.PNG;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Arne Vandamme
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles("it")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ImageServerTestEmbeddedApplication.class)
public class ITRemoteImageServerClient
{
	private ImageServerClient imageServerClient;

	@Value("${local.server.port}")
	private int port;

	@BeforeEach
	public void createClient() {
		String url = "http://localhost:" + port + "/resources/images";
		String accessToken = "standalone-access-token";

		imageServerClient = new RemoteImageServerClient( url, accessToken );
	}

	@Test
	public void uploadKnownResourceImage() throws ParseException, IOException {
		String externalId = UUID.randomUUID().toString();
		byte[] imageData = image( "poppy_flower_nature.jpg" );
		Date date = DateUtils.parseDate( "2013-05-14 13:33:22", "yyyy-MM-dd HH:mm:ss" );

		ImageInfoDto fetchedInfo = imageServerClient.imageInfo( externalId );
		assertFalse( fetchedInfo.isExisting() );
		assertEquals( externalId, fetchedInfo.getExternalId() );

		ImageInfoDto createdInfo = imageServerClient.loadImage( externalId, imageData, date );

		assertTrue( createdInfo.isExisting() );
		assertEquals( externalId, createdInfo.getExternalId() );
		assertEquals( date, createdInfo.getCreated() );
		assertEquals( new DimensionsDto( 1920, 1080 ), createdInfo.getDimensionsDto() );
		assertEquals( ImageTypeDto.JPEG, createdInfo.getImageType() );

		fetchedInfo = imageServerClient.imageInfo( externalId );
		assertEquals( createdInfo, fetchedInfo );

		try (InputStream inputStream = imageServerClient.imageStream( externalId, new ImageModificationDto(),
		                                                              new ImageVariantDto( ImageTypeDto.JPEG ) )) {
			byte[] originalSizeData = IOUtils.toByteArray( inputStream );

			ImageInfoDto modifiedUpload = imageServerClient.loadImage( UUID.randomUUID().toString(), originalSizeData );
			assertEquals( new DimensionsDto( 1920, 1080 ), modifiedUpload.getDimensionsDto() );
			assertEquals( ImageTypeDto.JPEG, modifiedUpload.getImageType() );
		}

		try (InputStream inputStream = imageServerClient.imageStream( externalId, "website", 640, 480, ImageTypeDto.PNG )) {
			byte[] scaledDate = IOUtils.toByteArray( inputStream );

			ImageInfoDto modifiedUpload = imageServerClient.loadImage( UUID.randomUUID().toString(), scaledDate );
			assertEquals( new DimensionsDto( 640, 480 ), modifiedUpload.getDimensionsDto() );
			assertEquals( ImageTypeDto.PNG, modifiedUpload.getImageType() );
		}

		// Delete existing
		assertTrue( imageServerClient.deleteImage( externalId ) );
		assertFalse( imageServerClient.imageInfo( externalId ).isExisting() );
		assertFalse( imageServerClient.imageExists( externalId ) );

		assertFalse( imageServerClient.deleteImage( externalId ) );

		imageServerClient.loadImage( externalId, imageData, date );
		assertTrue( imageServerClient.imageExists( externalId ) );
	}

	@Test
	public void replacingImage() throws Exception {
		String externalId = UUID.randomUUID().toString();
		byte[] imageOne = image( "poppy_flower_nature.jpg" );
		byte[] imageTwo = image( "transparentPngToPng.png" );

		ImageInfoDto createdInfo = imageServerClient.loadImage( externalId, imageOne );
		assertTrue( createdInfo.isExisting() );
		assertEquals( externalId, createdInfo.getExternalId() );
		assertEquals( new DimensionsDto( 1920, 1080 ), createdInfo.getDimensionsDto() );
		assertEquals( ImageTypeDto.JPEG, createdInfo.getImageType() );

		ImageInfoDto replaced;
		boolean failed = false;

		try {
			imageServerClient.loadImage( externalId, imageTwo );
		}
		catch ( ImageServerException ise ) {
			failed = true;
		}

		assertTrue( failed );

		replaced = imageServerClient.loadImage( externalId, imageTwo, true );
		assertNotNull( replaced );
		assertTrue( replaced.isExisting() );
		assertEquals( externalId, replaced.getExternalId() );
		assertEquals( new DimensionsDto( 100, 100 ), replaced.getDimensionsDto() );
		assertEquals( ImageTypeDto.PNG, replaced.getImageType() );

		ImageInfoDto fetched = imageServerClient.imageInfo( externalId );
		assertNotNull( fetched );
		assertTrue( fetched.isExisting() );
		assertEquals( externalId, fetched.getExternalId() );
		assertEquals( new DimensionsDto( 100, 100 ), fetched.getDimensionsDto() );
	}

	@Test
	public void registerModifications() throws ParseException, IOException {
		String externalId = UUID.randomUUID().toString();
		byte[] imageData =
				image( "poppy_flower_nature.jpg" );

		ImageInfoDto uploaded = imageServerClient.loadImage( externalId, imageData );
		assertTrue( uploaded.isExisting() );

		Collection<ImageModificationDto> modifications = imageServerClient.listModifications( externalId, "website" );
		assertTrue( modifications.isEmpty() );

		ImageModificationDto modificationDto = new ImageModificationDto( 640, 480 );
		modificationDto.setCrop( new CropDto( 10, 10, 400, 300 ) );
		modificationDto.setDensity( new DimensionsDto( 300, 300 ) );

		imageServerClient.registerImageModification( externalId, "website", modificationDto );

		modifications = imageServerClient.listModifications( externalId, "website" );
		assertEquals( 1, modifications.size() );

		ImageModificationDto dtoWithSource = new ImageModificationDto( modificationDto );
		dtoWithSource.getCrop().setSource( new DimensionsDto( 1920, 1080 ) );

		assertEquals( dtoWithSource, modifications.iterator().next() );

		assertEquals( dtoWithSource, modifications.iterator().next() );
	}

	@Test
	public void listResolutions() {
		List<ImageResolutionDto> resolutions = imageServerClient.listAllowedResolutions( "website" );
		assertEquals( 4, resolutions.size() );
		assertTrue( hasResolution( resolutions, 640, 480, false ) );
		assertFalse( hasResolution( resolutions, 800, 600, true ) );
		assertTrue( hasResolution( resolutions, 1024, 768, true ) );

		resolutions = imageServerClient.listAllowedResolutions( "tablet" );
		assertEquals( 4, resolutions.size() );
		assertFalse( hasResolution( resolutions, 640, 480, false ) );
		assertTrue( hasResolution( resolutions, 800, 600, true ) );
		assertTrue( hasResolution( resolutions, 1024, 768, true ) );

		resolutions = imageServerClient.listAllowedResolutions( null );
		assertEquals( 8, resolutions.size() );
		assertTrue( hasResolution( resolutions, 0, 0, false ) );
		assertTrue( hasResolution( resolutions, 640, 480, false ) );
		assertTrue( hasResolution( resolutions, 800, 600, true ) );
		assertTrue( hasResolution( resolutions, 1024, 768, true ) );

		resolutions = imageServerClient.listConfigurableResolutions( "website" );
		assertEquals( 1, resolutions.size() );
		assertFalse( hasResolution( resolutions, 640, 480, false ) );
		assertFalse( hasResolution( resolutions, 800, 600, true ) );
		assertTrue( hasResolution( resolutions, 1024, 768, true ) );

		resolutions = imageServerClient.listConfigurableResolutions( "tablet" );
		assertEquals( 4, resolutions.size() );
		assertFalse( hasResolution( resolutions, 640, 480, false ) );
		assertTrue( hasResolution( resolutions, 800, 600, true ) );
		assertTrue( hasResolution( resolutions, 1024, 768, true ) );

		resolutions = imageServerClient.listConfigurableResolutions( null );
		assertEquals( 4, resolutions.size() );
		assertFalse( hasResolution( resolutions, 640, 480, false ) );
		assertTrue( hasResolution( resolutions, 800, 600, true ) );
		assertTrue( hasResolution( resolutions, 1024, 768, true ) );
	}

	private boolean hasResolution( List<ImageResolutionDto> list, int width, int height, boolean configurable ) {
		for ( ImageResolutionDto resolution : list ) {
			if ( resolution.getWidth() == width && resolution.getHeight() == height && resolution.isConfigurable() == configurable ) {
				return true;
			}
		}

		return false;
	}

	@Test
	public void pregenerateVariants() throws IOException {
		String externalId = UUID.randomUUID().toString();
		byte[] imageData = image( "poppy_flower_nature.jpg" );

		ImageInfoDto uploaded = imageServerClient.loadImage( externalId, imageData );
		assertTrue( uploaded.isExisting() );

		List<ImageResolutionDto> resolutions = imageServerClient.pregenerateResolutions( externalId );
		assertEquals( 7, resolutions.size() );
		assertTrue( hasResolution( resolutions, 640, 480, false ) );
		assertTrue( hasResolution( resolutions, 800, 600, true ) );
		assertTrue( hasResolution( resolutions, 1024, 768, true ) );

		assertTrue( consumeImage( externalId, "website", 640, 480 ) );
		assertTrue( consumeImage( externalId, "tablet", 800, 600 ) );
		assertTrue( consumeImage( externalId, "website", 1024, 768 ) );
	}

	private boolean consumeImage( String imageId, String context, int width, int height ) throws IOException {
		InputStream is = imageServerClient.imageStream( imageId, context, width, height, ImageTypeDto.JPEG );
		byte[] data = IOUtils.toByteArray( is );
		return data.length > 0;
	}

	@Test
	public void renderProvidedImage() throws IOException {
		byte[] imageData = image( "poppy_flower_nature.jpg" );

		InputStream renderedImage = imageServerClient.imageStream( imageData, new ImageModificationDto( 100, 100 ),
		                                                           new ImageVariantDto( ImageTypeDto.PNG ) );

		byte[] scaledDate = IOUtils.toByteArray( renderedImage );

		ImageInfoDto modifiedUpload = imageServerClient.loadImage( UUID.randomUUID().toString(), scaledDate );
		assertEquals( new DimensionsDto( 100, 100 ), modifiedUpload.getDimensionsDto() );
		assertEquals( ImageTypeDto.PNG, modifiedUpload.getImageType() );
	}

	@Test
	public void imageInfoForGivenImage() throws IOException {
		byte[] imageData = image( "poppy_flower_nature.jpg" );

		ImageInfoDto imageInfoDto = imageServerClient.imageInfo( imageData );
		assertEquals( new DimensionsDto( 1920, 1080 ), imageInfoDto.getDimensionsDto() );
		assertEquals( ImageTypeDto.JPEG, imageInfoDto.getImageType() );
	}

	@Test
	@SneakyThrows
	public void imageInfoForPdf() {
		byte[] pdfData = image( "sample-pdf.pdf" );

		ImageInfoDto imageInfoDto = imageServerClient.imageInfo( pdfData );
		assertEquals( 5, imageInfoDto.getSceneCount() );
		assertEquals( new DimensionsDto( 612, 792 ), imageInfoDto.getDimensionsDto() );
		assertEquals( ImageTypeDto.PDF, imageInfoDto.getImageType() );
	}

	@Test
	@SneakyThrows
	public void sceneCountIsPersisted() {
		String externalId = UUID.randomUUID().toString();
		byte[] imageData = image( "sample-pdf.pdf" );

		ImageInfoDto fetchedInfo = imageServerClient.imageInfo( externalId );
		assertFalse( fetchedInfo.isExisting() );
		assertEquals( 0, fetchedInfo.getSceneCount() );
		assertEquals( externalId, fetchedInfo.getExternalId() );

		ImageInfoDto createdInfo = imageServerClient.loadImage( externalId, imageData );

		assertTrue( createdInfo.isExisting() );
		assertEquals( externalId, createdInfo.getExternalId() );
		assertEquals( 5, createdInfo.getSceneCount() );
		assertEquals( new DimensionsDto( 612, 792 ), createdInfo.getDimensionsDto() );
		assertEquals( ImageTypeDto.PDF, createdInfo.getImageType() );

		fetchedInfo = imageServerClient.imageInfo( externalId );
		assertEquals( new DimensionsDto( 612, 792 ), fetchedInfo.getDimensionsDto() );
		assertEquals( createdInfo.getSceneCount(), fetchedInfo.getSceneCount() );
		assertEquals( ImageTypeDto.PDF, createdInfo.getImageType() );
	}

	@Test
	@SneakyThrows
	public void convertImage() {
		ImageConvertDto imageConvertDto = ImageConvertDto.builder()
		                                                 .image( image( "poppy_flower_nature.jpg" ) )
		                                                 .transformation( "flower-*", Collections.singletonList( ImageTransformDto.builder()
		                                                                                                                          .dpi( 300 )
		                                                                                                                          .colorSpace(
				                                                                                                                          ColorSpaceDto.GRAYSCALE )
		                                                                                                                          .outputType(
				                                                                                                                          ImageTypeDto.PNG )
		                                                                                                                          .build() ) )
		                                                 .build();

		ImageConvertResultDto imageConvertResultDto = imageServerClient.convertImage( imageConvertDto );

		assertEquals( 1, imageConvertResultDto.getTotal() );

		assertEquals( 1, imageConvertResultDto.getKeys().size() );

		String key = "flower-0";

		assertEquals( key, imageConvertResultDto.getKeys().toArray()[0] );

		assertEquals( 1, imageConvertResultDto.getPages().size() );
		assertEquals( 0, imageConvertResultDto.getPages().toArray()[0] );

		assertEquals( 1, imageConvertResultDto.getTransforms().size() );
		ImageDto transformation = imageConvertResultDto.getTransforms().get( key );
		assertEquals( ImageTypeDto.PNG, transformation.getFormat() );
	}

	@Test
	@SneakyThrows
	public void convertImagePdf() {
		ImageConvertDto imageConvertDto = ImageConvertDto.builder()
		                                                 .image( image( "sample-pdf.pdf" ) )
		                                                 .pages( "1,3-6" )
		                                                 .transformation( "sample-*", Collections.singletonList( ImageTransformDto.builder()
		                                                                                                                          .dpi( 300 )
		                                                                                                                          .colorSpace(
				                                                                                                                          ColorSpaceDto.GRAYSCALE )
		                                                                                                                          .outputType(
				                                                                                                                          ImageTypeDto.PNG )
		                                                                                                                          .build() ) )
		                                                 .build();

		ImageConvertResultDto imageConvertResultDto = imageServerClient.convertImage( imageConvertDto );

		assertEquals( 4, imageConvertResultDto.getTotal() );

		assertEquals( 4, imageConvertResultDto.getKeys().size() );
		assertTrue( imageConvertResultDto.getKeys().contains( "sample-1" ) );
		assertTrue( imageConvertResultDto.getKeys().contains( "sample-3" ) );
		assertTrue( imageConvertResultDto.getKeys().contains( "sample-4" ) );
		assertTrue( imageConvertResultDto.getKeys().contains( "sample-5" ) );

		assertEquals( 4, imageConvertResultDto.getPages().size() );
		assertTrue( imageConvertResultDto.getPages().contains( 1 ) );
		assertTrue( imageConvertResultDto.getPages().contains( 3 ) );
		assertTrue( imageConvertResultDto.getPages().contains( 4 ) );
		assertTrue( imageConvertResultDto.getPages().contains( 5 ) );

		assertEquals( 4, imageConvertResultDto.getTransforms().size() );

		ImageDto transformation = imageConvertResultDto.getTransforms().get( "sample-1" );
		assertEquals( ImageTypeDto.PNG, transformation.getFormat() );

		transformation = imageConvertResultDto.getTransforms().get( "sample-3" );
		assertEquals( ImageTypeDto.PNG, transformation.getFormat() );

		transformation = imageConvertResultDto.getTransforms().get( "sample-4" );
		assertEquals( ImageTypeDto.PNG, transformation.getFormat() );

		transformation = imageConvertResultDto.getTransforms().get( "sample-5" );
		assertEquals( ImageTypeDto.PNG, transformation.getFormat() );
	}

	@Test
	@SneakyThrows
	public void convertSingleImage() {
		ImageDto result = imageServerClient.convertImage( image( "poppy_flower_nature.jpg" ),
		                                                  Collections.singletonList(
				                                                  ImageTransformDto.builder()
				                                                                   .dpi( 300 )
				                                                                   .colorSpace( ColorSpaceDto.GRAYSCALE )
				                                                                   .outputType( ImageTypeDto.PNG )
				                                                                   .build() ) );

		assertEquals( ImageTypeDto.PNG, result.getFormat() );
		assertTrue( result.getImage().length > 0 );
	}

	@Test
	@SneakyThrows
	public void convertSingleRegisteredImage() {
		String imageId = UUID.randomUUID().toString();
		ImageInfoDto imageInfoDto = imageServerClient.loadImage( imageId, image( "poppy_flower_nature.jpg" ) );
		assertNotNull( imageInfoDto );

		ImageDto result = imageServerClient.convertImage( imageId,
		                                                  Collections.singletonList(
				                                                  ImageTransformDto.builder()
				                                                                   .dpi( 300 )
				                                                                   .colorSpace( ColorSpaceDto.GRAYSCALE )
				                                                                   .outputType( ImageTypeDto.PNG )
				                                                                   .build() ) );

		assertEquals( ImageTypeDto.PNG, result.getFormat() );
		assertTrue( result.getImage().length > 0 );
	}

	@Test
	@SneakyThrows
	public void convertResizeEpsRetina() {
		ImageDto result = imageServerClient.convertImage( image( "kaaimangrootkleur.eps" ),
		                                                  Collections.singletonList(
				                                                  ImageTransformDto.builder()
				                                                                   .height( 1536 )
				                                                                   .quality( 100 )
				                                                                   .outputType( ImageTypeDto.PNG )
				                                                                   .build() ) );

		try (InputStream i = new ByteArrayInputStream( result.getImage() )) {
			BufferedImage bimg = ImageIO.read( i );
			assertEquals( 1536, bimg.getHeight() );
		}
	}

	@Test
	@SneakyThrows
	public void convertImagePdfEchoBackgroundColor() {
		ImageConvertDto imageConvertDto = ImageConvertDto.builder()
		                                                 .image( image( "55980.pdf" ) )
		                                                 .pages( "0" )
		                                                 .transformation( "echo-*", Collections.singletonList( ImageTransformDto.builder()
		                                                                                                                        .height( 560 )
		                                                                                                                        .backgroundColor(
				                                                                                                                        ColorDto.from(
						                                                                                                                        "#fff1e0" ) )
		                                                                                                                        .dpi( 300 )
		                                                                                                                        .quality( 100 )
		                                                                                                                        .outputType( PNG )
		                                                                                                                        .build() ) )
		                                                 .build();

		ImageConvertResultDto imageConvertResultDto = imageServerClient.convertImage( imageConvertDto );

		assertEquals( 1, imageConvertResultDto.getTotal() );

		assertEquals( 1, imageConvertResultDto.getKeys().size() );
		assertTrue( imageConvertResultDto.getKeys().contains( "echo-0" ) );

		assertEquals( 1, imageConvertResultDto.getPages().size() );
		assertTrue( imageConvertResultDto.getPages().contains( 0 ) );

		assertEquals( 1, imageConvertResultDto.getTransforms().size() );

		ImageDto transformation = imageConvertResultDto.getTransforms().get( "echo-0" );
		assertEquals( ImageTypeDto.PNG, transformation.getFormat() );

		try (InputStream i = new ByteArrayInputStream( transformation.getImage() )) {
			BufferedImage bimg = ImageIO.read( i );
			assertEquals( 560, bimg.getHeight() );
		}
	}

	private byte[] image( String s ) throws IOException {
		return IOUtils.toByteArray( getClass().getClassLoader().getResourceAsStream( s ) );
	}
}
