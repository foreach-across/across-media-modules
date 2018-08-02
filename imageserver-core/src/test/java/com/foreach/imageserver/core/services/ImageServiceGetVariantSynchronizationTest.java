package com.foreach.imageserver.core.services;

import com.foreach.common.test.MockedLoader;
import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.managers.ImageManager;
import com.foreach.imageserver.core.managers.ImageModificationManager;
import com.foreach.imageserver.core.managers.ImageResolutionManager;
import com.foreach.imageserver.core.transformers.InMemoryImageSource;
import com.foreach.imageserver.core.transformers.StreamImageSource;
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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ImageServiceGetVariantSynchronizationTest.TestConfig.class },
                      loader = MockedLoader.class)
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
		Answer<InMemoryImageSource> firstImageAnswer =
				new DelayedTransformResult( lock, inMemoryImageSource( "IMAGE1" ) );
		Answer<InMemoryImageSource> secondImageAnswer =
				new DelayedTransformResult( lock, inMemoryImageSource( "IMAGE2" ) );

		TestResults testResults = runTest( threadsPerImage, firstImageAnswer, secondImageAnswer );

		// Wait for all threads to have started.
		Thread.sleep( 1000 );

		synchronized ( lock ) {
			lock.notifyAll();
		}

		verify( imageTransformService, times( 1 ) ).modify( eq( testResults.getFirstOriginalImageSource() ), anyInt(),
		                                                    anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(),
		                                                    anyInt(), eq( ImageType.PNG ), any(Dimensions.class) );
		verify( imageTransformService, times( 1 ) ).modify( eq( testResults.getSecondOriginalImageSource() ), anyInt(),
		                                                    anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(),
		                                                    anyInt(), eq( ImageType.PNG ), any(Dimensions.class) );

		for ( Future<StreamImageSource> future : testResults.getFirstImageFutures() ) {
			assertEquals( "IMAGE1", new String( IOUtils.toByteArray( future.get().getImageStream() ) ) );
		}

		for ( Future<StreamImageSource> future : testResults.getSecondImageFutures() ) {
			assertEquals( "IMAGE2", new String( IOUtils.toByteArray( future.get().getImageStream() ) ) );
		}
	}

	@Test
	public void simultaneousGenerationWithExceptions() throws InterruptedException, ExecutionException, IOException {
		int threadsPerImage = 30;
		Object lock = new Object();
		Answer<InMemoryImageSource> firstImageAnswer =
				new DelayedTransformResult( lock, inMemoryImageSource( "IMAGE1" ) );
		Answer<InMemoryImageSource> secondImageAnswer = new DelayedTransformExceptionResult( lock );

		TestResults testResults = runTest( threadsPerImage, firstImageAnswer, secondImageAnswer );

		// Wait for all threads to have started.
		Thread.sleep( 1000 );

		synchronized ( lock ) {
			lock.notifyAll();
		}

		verify( imageTransformService, times( 1 ) ).modify( eq( testResults.getFirstOriginalImageSource() ), anyInt(),
		                                                    anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(),
		                                                    anyInt(), eq( ImageType.PNG ), any(Dimensions.class) );
		verify( imageTransformService, times( 1 ) ).modify( eq( testResults.getSecondOriginalImageSource() ), anyInt(),
		                                                    anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(),
		                                                    anyInt(), eq( ImageType.PNG ), any(Dimensions.class) );

		for ( Future<StreamImageSource> future : testResults.getFirstImageFutures() ) {
			assertEquals( "IMAGE1", new String( IOUtils.toByteArray( future.get().getImageStream() ) ) );
		}

		for ( Future<StreamImageSource> future : testResults.getSecondImageFutures() ) {
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
		Answer<InMemoryImageSource> firstImageAnswer =
				new DelayedTransformResult( lock, inMemoryImageSource( "IMAGE1" ) );
		Answer<InMemoryImageSource> secondImageAnswer = new DelayedTransformErrorResult( lock );

		TestResults testResults = runTest( threadsPerImage, firstImageAnswer, secondImageAnswer );

		// Wait for all threads to have started.
		Thread.sleep( 1000 );

		synchronized ( lock ) {
			lock.notifyAll();
		}

		verify( imageTransformService, times( 1 ) ).modify( eq( testResults.getFirstOriginalImageSource() ), anyInt(),
		                                                    anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(),
		                                                    anyInt(), eq( ImageType.PNG ), any(Dimensions.class) );
		verify( imageTransformService, times( 1 ) ).modify( eq( testResults.getSecondOriginalImageSource() ), anyInt(),
		                                                    anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt(),
		                                                    anyInt(), eq( ImageType.PNG ), any(Dimensions.class) );

		for ( Future<StreamImageSource> future : testResults.getFirstImageFutures() ) {
			assertEquals( "IMAGE1", new String( IOUtils.toByteArray( future.get().getImageStream() ) ) );
		}

		for ( Future<StreamImageSource> future : testResults.getSecondImageFutures() ) {
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
	                             Answer<InMemoryImageSource> firstImageAnswer,
	                             Answer<InMemoryImageSource> secondImageAnswer ) throws InterruptedException {
		Image firstImage = image( 1 );
		Image secondImage = image( 2 );
		ImageContext context = context( 10L );
		ImageResolution imageResolution = imageResolution( 20 );
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

		StreamImageSource firstOriginalImageSource = new StreamImageSource( null, (InputStream) null );
		StreamImageSource secondOriginalImageSource = new StreamImageSource( null, (InputStream) null );
		when( imageStoreService.getOriginalImage( firstImage ) ).thenReturn( firstOriginalImageSource );
		when( imageStoreService.getOriginalImage( secondImage ) ).thenReturn( secondOriginalImageSource );

		when( imageTransformService.modify( eq( firstOriginalImageSource ), anyInt(), anyInt(), anyInt(), anyInt(),
		                                    anyInt(), anyInt(), anyInt(), anyInt(), eq( ImageType.PNG ), any(Dimensions.class) ) ).thenAnswer(
				firstImageAnswer );
		when( imageTransformService.modify( eq( secondOriginalImageSource ), anyInt(), anyInt(), anyInt(), anyInt(),
		                                    anyInt(), anyInt(), anyInt(), anyInt(), eq( ImageType.PNG ), any(Dimensions.class) ) ).thenAnswer(
				secondImageAnswer );

		List<Future<StreamImageSource>> firstImageFutures = new ArrayList<>();
		List<Future<StreamImageSource>> secondImageFutures = new ArrayList<>();

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

	private Image image( int id ) {
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

	private ImageResolution imageResolution( int id ) {
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

	private InMemoryImageSource inMemoryImageSource( String byteStream ) {
		return new InMemoryImageSource( null, byteStream.getBytes() );
	}

	private static class TestResults
	{
		private final List<Future<StreamImageSource>> firstImageFutures;
		private final List<Future<StreamImageSource>> secondImageFutures;
		private final StreamImageSource firstOriginalImageSource;
		private final StreamImageSource secondOriginalImageSource;

		public TestResults( List<Future<StreamImageSource>> firstImageFutures,
		                    List<Future<StreamImageSource>> secondImageFutures,
		                    StreamImageSource firstOriginalImageSource,
		                    StreamImageSource secondOriginalImageSource ) {
			this.firstImageFutures = firstImageFutures;
			this.secondImageFutures = secondImageFutures;
			this.firstOriginalImageSource = firstOriginalImageSource;
			this.secondOriginalImageSource = secondOriginalImageSource;
		}

		public List<Future<StreamImageSource>> getFirstImageFutures() {
			return firstImageFutures;
		}

		public List<Future<StreamImageSource>> getSecondImageFutures() {
			return secondImageFutures;
		}

		public StreamImageSource getFirstOriginalImageSource() {
			return firstOriginalImageSource;
		}

		public StreamImageSource getSecondOriginalImageSource() {
			return secondOriginalImageSource;
		}
	}

	private static class GetVariantImageCallable implements Callable<StreamImageSource>
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
		public StreamImageSource call() {
			return imageService.getVariantImage( image, context, imageResolution, imageVariant );
		}
	}

	private static class DelayedTransformResult implements Answer<InMemoryImageSource>
	{
		private final Object lock;
		private final InMemoryImageSource imageSource;

		public DelayedTransformResult( Object lock, InMemoryImageSource imageSource ) {
			this.lock = lock;
			this.imageSource = imageSource;
		}

		@Override
		public InMemoryImageSource answer( InvocationOnMock invocationOnMock ) throws Throwable {
			synchronized ( lock ) {
				lock.wait();
			}
			return imageSource;
		}
	}

	private static class DelayedTransformExceptionResult implements Answer<InMemoryImageSource>
	{
		private final Object lock;

		public DelayedTransformExceptionResult( Object lock ) {
			this.lock = lock;
		}

		@Override
		public InMemoryImageSource answer( InvocationOnMock invocationOnMock ) throws Throwable {
			synchronized ( lock ) {
				lock.wait();
			}
			throw new DelayedTransformException();
		}
	}

	private static class DelayedTransformErrorResult implements Answer<InMemoryImageSource>
	{
		private final Object lock;

		public DelayedTransformErrorResult( Object lock ) {
			this.lock = lock;
		}

		@Override
		public InMemoryImageSource answer( InvocationOnMock invocationOnMock ) throws Throwable {
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

	}
}
