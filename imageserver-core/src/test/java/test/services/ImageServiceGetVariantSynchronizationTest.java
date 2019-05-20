package test.services;

import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.managers.ImageManager;
import com.foreach.imageserver.core.managers.ImageModificationManager;
import com.foreach.imageserver.core.managers.ImageResolutionManager;
import com.foreach.imageserver.core.services.*;
import com.foreach.imageserver.core.transformers.ImageSource;
import com.foreach.imageserver.core.transformers.SimpleImageSource;
import com.foreach.imageserver.dto.ImageModificationDto;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ImageServiceGetVariantSynchronizationTest.TestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ImageServiceGetVariantSynchronizationTest
{

	@Autowired
	public ImageService imageService;

	@Autowired
	public ImageStoreService imageStoreService;

	@Autowired
	private ImageModificationManager imageModificationManager;

	@Autowired
	private ImageTransformService imageTransformService;

	@Autowired
	private CropGenerator cropGenerator;

	@Test
	public void successfulSimultaneousGeneration() throws InterruptedException, ExecutionException, IOException {
		int threadsPerImage = 30;
		Object lock = new Object();
		Answer<ImageSource> firstImageAnswer =
				new DelayedTransformResult( lock, imageSource( "IMAGE1" ) );
		Answer<ImageSource> secondImageAnswer =
				new DelayedTransformResult( lock, imageSource( "IMAGE2" ) );

		TestResults testResults = runTest( threadsPerImage, firstImageAnswer, secondImageAnswer );

		// Wait for all threads to have started.
		Thread.sleep( 1000 );

		synchronized ( lock ) {
			lock.notifyAll();
		}

		verify( imageTransformService, times( 1 ) ).transform( eq( testResults.getFirstOriginalImageSource() ), any(), any() );
		verify( imageTransformService, times( 1 ) ).transform( eq( testResults.getSecondOriginalImageSource() ), any(), any() );

		for ( Future<ImageSource> future : testResults.getFirstImageFutures() ) {
			assertEquals( "IMAGE1", new String( IOUtils.toByteArray( future.get().getImageStream() ) ) );
		}

		for ( Future<ImageSource> future : testResults.getSecondImageFutures() ) {
			assertEquals( "IMAGE2", new String( IOUtils.toByteArray( future.get().getImageStream() ) ) );
		}
	}

	@Test
	public void simultaneousGenerationWithExceptions() throws InterruptedException, ExecutionException, IOException {
		int threadsPerImage = 30;
		Object lock = new Object();
		Answer<ImageSource> firstImageAnswer =
				new DelayedTransformResult( lock, imageSource( "IMAGE1" ) );
		Answer<ImageSource> secondImageAnswer = new DelayedTransformExceptionResult( lock );

		TestResults testResults = runTest( threadsPerImage, firstImageAnswer, secondImageAnswer );

		// Wait for all threads to have started.
		Thread.sleep( 1000 );

		synchronized ( lock ) {
			lock.notifyAll();
		}

		verify( imageTransformService, times( 1 ) ).transform( eq( testResults.getFirstOriginalImageSource() ), any(), any() );
		verify( imageTransformService, times( 1 ) ).transform( eq( testResults.getSecondOriginalImageSource() ), any(), any() );

		for ( Future<ImageSource> future : testResults.getFirstImageFutures() ) {
			assertEquals( "IMAGE1", new String( IOUtils.toByteArray( future.get().getImageStream() ) ) );
		}

		for ( Future<ImageSource> future : testResults.getSecondImageFutures() ) {
			try {
				future.get();
				assertTrue( false );
			}
			catch ( ExecutionException e ) {
				assertTrue( e.getCause() instanceof DelayedTransformException );
			}
		}
	}

	@Test
	public void simultaneousGenerationWithErrors() throws InterruptedException, ExecutionException, IOException {
		int threadsPerImage = 30;
		Object lock = new Object();
		Answer<ImageSource> firstImageAnswer =
				new DelayedTransformResult( lock, imageSource( "IMAGE1" ) );
		Answer<ImageSource> secondImageAnswer = new DelayedTransformErrorResult( lock );

		TestResults testResults = runTest( threadsPerImage, firstImageAnswer, secondImageAnswer );

		// Wait for all threads to have started.
		Thread.sleep( 1000 );

		synchronized ( lock ) {
			lock.notifyAll();
		}

		verify( imageTransformService, times( 1 ) ).transform( eq( testResults.getFirstOriginalImageSource() ), any(), any() );
		verify( imageTransformService, times( 1 ) ).transform( eq( testResults.getSecondOriginalImageSource() ), any(), any() );

		for ( Future<ImageSource> future : testResults.getFirstImageFutures() ) {
			assertEquals( "IMAGE1", new String( IOUtils.toByteArray( future.get().getImageStream() ) ) );
		}

		for ( Future<ImageSource> future : testResults.getSecondImageFutures() ) {
			try {
				future.get();
				assertTrue( false );
			}
			catch ( ExecutionException e ) {
				assertTrue( e.getCause() instanceof DelayedTransformError );
			}
		}
	}

	private TestResults runTest( int threadsPerImage,
	                             Answer<ImageSource> firstImageAnswer,
	                             Answer<ImageSource> secondImageAnswer ) throws InterruptedException {
		Image firstImage = image( 1L );
		Image secondImage = image( 2L );
		ImageContext context = context( 10L );
		ImageResolution imageResolution = imageResolution( 20L );
		ImageVariant imageVariant = imageVariant( ImageType.PNG );

		ImageModificationDto modificationDto = new ImageModificationDto();
		modificationDto.setResolution( DtoUtil.toDto( imageResolution ) );

		when( cropGenerator.buildModificationDto( firstImage, context, imageResolution ) ).thenReturn(
				modificationDto );
		when( cropGenerator.buildModificationDto( secondImage, context, imageResolution ) ).thenReturn(
				modificationDto );

		when( imageStoreService.getVariantImage( firstImage, context, imageResolution, imageVariant ) ).thenReturn(
				null );
		when( imageStoreService.getVariantImage( secondImage, context, imageResolution, imageVariant ) ).thenReturn(
				null );

		when( imageModificationManager.getById( 1, 10, 20 ) ).thenReturn( imageModification() );
		when( imageModificationManager.getById( 2, 10, 20 ) ).thenReturn( imageModification() );

		SimpleImageSource firstOriginalImageSource = new SimpleImageSource( null, (byte[]) null );
		SimpleImageSource secondOriginalImageSource = new SimpleImageSource( null, (byte[]) null );
		when( imageStoreService.getOriginalImage( firstImage ) ).thenReturn( firstOriginalImageSource );
		when( imageStoreService.getOriginalImage( secondImage ) ).thenReturn( secondOriginalImageSource );

		when( imageTransformService.transform( eq( firstOriginalImageSource ), any(), any() ) ).thenAnswer( firstImageAnswer );
		when( imageTransformService.transform( eq( secondOriginalImageSource ), any(), any() ) ).thenAnswer( secondImageAnswer );

		List<Future<ImageSource>> firstImageFutures = new ArrayList<>();
		List<Future<ImageSource>> secondImageFutures = new ArrayList<>();

		ExecutorService executorService = Executors.newFixedThreadPool( threadsPerImage * 2 );
		for ( int i = 0; i < threadsPerImage; ++i ) {
			firstImageFutures.add( executorService.submit(
					new GetVariantImageCallable( imageService, firstImage, context, imageResolution, imageVariant ) ) );
			secondImageFutures.add( executorService.submit(
					new GetVariantImageCallable( imageService, secondImage, context, imageResolution,
					                             imageVariant ) ) );
		}

		return new TestResults( firstImageFutures, secondImageFutures, firstOriginalImageSource,
		                        secondOriginalImageSource );
	}

	private Image image( Long id ) {
		Dimensions dimensions = new Dimensions();
		dimensions.setWidth( 100 );
		dimensions.setHeight( 100 );

		Image image = new Image();
		image.setId( id );
		image.setDimensions( dimensions );
		return image;
	}

	private ImageContext context( Long id ) {
		ImageContext context = new ImageContext();
		context.setId( id );
		return context;
	}

	private ImageResolution imageResolution( Long id ) {
		ImageResolution imageResolution = new ImageResolution();
		imageResolution.setId( id );
		imageResolution.setWidth( 100 );
		imageResolution.setHeight( 100 );
		return imageResolution;
	}

	private ImageVariant imageVariant( ImageType outputType ) {
		ImageVariant imageVariant = new ImageVariant();
		imageVariant.setOutputType( outputType );
		return imageVariant;
	}

	private ImageModification imageModification() {
		Crop crop = new Crop();
		crop.setX( 10 );
		crop.setY( 10 );
		crop.setWidth( 10 );
		crop.setHeight( 10 );

		Dimensions density = new Dimensions();
		density.setWidth( 100 );
		density.setHeight( 100 );

		ImageModification imageModification = new ImageModification();
		imageModification.setImageId( 101010 );
		imageModification.setContextId( 111111 );
		imageModification.setResolutionId( 121212 );
		imageModification.setCrop( crop );
		imageModification.setDensity( density );
		return imageModification;
	}

	private ImageSource imageSource( String byteStream ) {
		return new SimpleImageSource( null, byteStream.getBytes() );
	}

	private static class TestResults
	{
		private final List<Future<ImageSource>> firstImageFutures;
		private final List<Future<ImageSource>> secondImageFutures;
		private final ImageSource firstOriginalImageSource;
		private final ImageSource secondOriginalImageSource;

		public TestResults( List<Future<ImageSource>> firstImageFutures,
		                    List<Future<ImageSource>> secondImageFutures,
		                    SimpleImageSource firstOriginalImageSource,
		                    SimpleImageSource secondOriginalImageSource ) {
			this.firstImageFutures = firstImageFutures;
			this.secondImageFutures = secondImageFutures;
			this.firstOriginalImageSource = firstOriginalImageSource;
			this.secondOriginalImageSource = secondOriginalImageSource;
		}

		public List<Future<ImageSource>> getFirstImageFutures() {
			return firstImageFutures;
		}

		public List<Future<ImageSource>> getSecondImageFutures() {
			return secondImageFutures;
		}

		public ImageSource getFirstOriginalImageSource() {
			return firstOriginalImageSource;
		}

		public ImageSource getSecondOriginalImageSource() {
			return secondOriginalImageSource;
		}
	}

	private static class GetVariantImageCallable implements Callable<ImageSource>
	{
		private final ImageService imageService;
		private final Image image;
		private final ImageContext context;
		private final ImageResolution imageResolution;
		private final ImageVariant imageVariant;

		public GetVariantImageCallable( ImageService imageService,
		                                Image image,
		                                ImageContext context,
		                                ImageResolution imageResolution,
		                                ImageVariant imageVariant ) {
			this.imageService = imageService;
			this.image = image;
			this.context = context;
			this.imageResolution = imageResolution;
			this.imageVariant = imageVariant;
		}

		@Override
		public ImageSource call() {
			return imageService.getVariantImage( image, context, imageResolution, imageVariant );
		}
	}

	private static class DelayedTransformResult implements Answer<ImageSource>
	{
		private final Object lock;
		private final ImageSource imageSource;

		public DelayedTransformResult( Object lock, ImageSource imageSource ) {
			this.lock = lock;
			this.imageSource = imageSource;
		}

		@Override
		public ImageSource answer( InvocationOnMock invocationOnMock ) throws Throwable {
			synchronized ( lock ) {
				lock.wait();
			}
			return imageSource;
		}
	}

	private static class DelayedTransformExceptionResult implements Answer<ImageSource>
	{
		private final Object lock;

		public DelayedTransformExceptionResult( Object lock ) {
			this.lock = lock;
		}

		@Override
		public ImageSource answer( InvocationOnMock invocationOnMock ) throws Throwable {
			synchronized ( lock ) {
				lock.wait();
			}
			throw new DelayedTransformException();
		}
	}

	private static class DelayedTransformErrorResult implements Answer<ImageSource>
	{
		private final Object lock;

		public DelayedTransformErrorResult( Object lock ) {
			this.lock = lock;
		}

		@Override
		public ImageSource answer( InvocationOnMock invocationOnMock ) throws Throwable {
			synchronized ( lock ) {
				lock.wait();
			}
			throw new DelayedTransformError();
		}
	}

	private static class DelayedTransformException extends RuntimeException
	{
	}

	private static class DelayedTransformError extends Error
	{
	}

	@Configuration
	public static class TestConfig
	{

		@Bean
		public ImageService imageService() {
			return new ImageServiceImpl();
		}

		@Bean
		public CropGenerator cropGenerator() {
			return mock( CropGenerator.class );
		}

		@Bean
		public ImageManager imageManager() {
			return mock( ImageManager.class );
		}

		@Bean
		public ImageStoreService imageStoreService() {
			return mock( ImageStoreService.class );
		}

		@Bean
		public ImageModificationManager imageModificationManager() {
			return mock( ImageModificationManager.class );
		}

		@Bean
		public ImageTransformService imageTransformService() {
			return mock( ImageTransformService.class );
		}

		@Bean
		public ImageRepositoryService imageRepositoryService() {
			return mock( ImageRepositoryService.class );
		}

		@Bean
		public ImageResolutionManager imageResolutionManager() {
			return mock( ImageResolutionManager.class );
		}

		@Bean
		public CropGeneratorUtil cropGeneratorUtil() {
			return mock( CropGeneratorUtil.class );
		}

		@Bean
		public ImageProfileService imageProfileService() {
			return mock( ImageProfileService.class );
		}

	}
}
