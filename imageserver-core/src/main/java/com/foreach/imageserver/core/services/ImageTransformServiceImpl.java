package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.transformers.*;
import com.foreach.imageserver.dto.ImageTransformDto;
import com.foreach.imageserver.logging.LogHelper;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.concurrent.Semaphore;

@Service
public class ImageTransformServiceImpl implements ImageTransformService
{
	@Autowired
	private ImageTransformerRegistry imageTransformerRegistry;

	private Semaphore semaphore;

	public ImageTransformServiceImpl( int concurrentTransformLimit ) {
		/**
		 * Right now, we have only one ImageTransformer implementation and it runs on the local machine. In theory,
		 * however, we could have implementations that off-load the actual computations to other machines. Should this
		 * ever get to be the case, we may want to provide more fine-grained control over the number of concurrent
		 * transformations. For now, a single limit will suffice.
		 */

		this.semaphore = new Semaphore( concurrentTransformLimit, true );
	}

	@Override
	public Dimensions computeDimensions( StreamImageSource imageSource ) {
		if ( imageSource == null ) {
			LOG.warn( "Null parameters not allowed - ImageTransformServiceImpl#computeDimensions: imageSource=null" );
		}

		final ImageCalculateDimensionsAction action = new ImageCalculateDimensionsAction( imageSource );

		ImageTransformer imageTransformer = findAbleTransformer( new CanExecute()
		{
			@Override
			public ImageTransformerPriority consider( ImageTransformer imageTransformer ) {
				return imageTransformer.canExecute( action );
			}
		} );

		// TODO I'm opting for returning null in case of failure now, maybe raise an exception instead?
		Dimensions dimensions = null;
		if ( imageTransformer != null ) {
			semaphore.acquireUninterruptibly();
			try {
				dimensions = imageTransformer.execute( action );
			}
			catch ( Exception e ) {
				LOG.error(
						"Error while computing dimensions - ImageTransformServiceImpl#computeDimensions: imageSource={}",
						LogHelper.flatten( imageSource ), e );
			}
			finally {
				semaphore.release();
			}
		}
		return dimensions;
	}

	@Override
	public ImageAttributes getAttributes( InputStream imageStream ) {
		if ( imageStream == null ) {
			LOG.warn( "Null parameters not allowed - ImageTransformServiceImpl#getAttributes: imageStream=null" );
		}

		final GetImageAttributesAction action = new GetImageAttributesAction( imageStream );

		ImageTransformer imageTransformer = findAbleTransformer( new CanExecute()
		{
			@Override
			public ImageTransformerPriority consider( ImageTransformer imageTransformer ) {
				return imageTransformer.canExecute( action );
			}
		} );

		// TODO I'm opting for returning null in case of failure now, maybe raise an exception instead?
		ImageAttributes imageAttributes = null;
		if ( imageTransformer != null ) {
			semaphore.acquireUninterruptibly();
			try {
				imageAttributes = imageTransformer.execute( action );
			}
			catch ( Exception e ) {
				LOG.error(
						"Encountered failure during image transform - ImageTransformServiceImpl#getAttributes: imageStream={}",
						imageStream, e );
			}
			finally {
				semaphore.release();
			}
		}
		return imageAttributes;
	}

	@Override
	public InMemoryImageSource modify( StreamImageSource imageSource,
	                                   int outputWidth,
	                                   int outputHeight,
	                                   int cropX,
	                                   int cropY,
	                                   int cropWidth,
	                                   int cropHeight,
	                                   int densityWidth,
	                                   int densityHeight,
	                                   ImageType outputType ) {
		return modify( imageSource, outputWidth, outputHeight, cropX, cropY, cropWidth, cropHeight, densityWidth, densityHeight, outputType, null );
	}

	@Override
	public InMemoryImageSource modify( StreamImageSource imageSource,
	                                   int outputWidth,
	                                   int outputHeight,
	                                   int cropX,
	                                   int cropY,
	                                   int cropWidth,
	                                   int cropHeight,
	                                   int densityWidth,
	                                   int densityHeight,
	                                   ImageType outputType,
	                                   Dimensions boundaries ) {
		if ( imageSource == null ) {
			LOG.warn(
					"Null parameters not allowed - ImageTransformServiceImpl#modify: imageSource, outputWidth={}, outputHeight={}, cropX={}, cropY={}, cropWidth={}, cropHeight={}, densityWidth={}, densityHeight={}, outputType={}",
					LogHelper.flatten( outputWidth, outputHeight, cropX, cropY, cropWidth, cropHeight,
					                   densityWidth, densityHeight, outputType ) );
		}

		final ImageModifyAction action =
				new ImageModifyAction( imageSource, outputWidth, outputHeight, cropX, cropY, cropWidth, cropHeight,
				                       densityWidth, densityHeight, outputType, boundaries );

		ImageTransformer imageTransformer = findAbleTransformer( new CanExecute()
		{
			@Override
			public ImageTransformerPriority consider( ImageTransformer imageTransformer ) {
				return imageTransformer.canExecute( action );
			}
		} );

		// TODO I'm opting for returning null in case of failure now, maybe raise an exception instead?
		InMemoryImageSource result = null;
		if ( imageTransformer != null ) {
			semaphore.acquireUninterruptibly();
			try {
				result = imageTransformer.execute( action );
			}
			catch ( Exception e ) {
				LOG.warn(
						"Encountered error modifying file - ImageTransformServiceImpl#modify: imageSource, outputWidth={}, outputHeight={}, cropX={}, cropY={}, cropWidth={}, cropHeight={}, densityWidth={}, densityHeight={}, outputType={}",
						LogHelper.flatten( imageSource, outputWidth, outputHeight, cropX, cropY, cropWidth, cropHeight,
						                   densityWidth, densityHeight, outputType ) );
			}
			finally {
				semaphore.release();
			}
		}
		return result;
	}

	@Override
	public ImageSource transform( @NonNull ImageSource imageSource,
	                              @NonNull ImageAttributes sourceAttributes,
	                              @NonNull Collection<ImageTransformDto> transforms ) {
		if ( transforms.isEmpty() ) {
			return imageSource;
		}

		ArrayDeque<ImageTransformDto> queue = new ArrayDeque<>( transforms );
		ImageSource source = imageSource;
		ImageAttributes attributes = sourceAttributes;

		do {
			ImageTransformDto transform = queue.removeFirst();

			if ( attributes == null ) {
				attributes = getAttributes( source.getImageStream() );
			}

			source = transform( source, attributes, transform );

			attributes = null;
		}
		while ( !queue.isEmpty() );

		return source;
	}

	private ImageSource transform( ImageSource imageSource, ImageAttributes attributes, ImageTransformDto transformDto ) {
		// simplify the transform dto using the attributes
		// delegate to actual transformer
		return null;
	}

	private ImageTransformer findAbleTransformer( CanExecute canExecute ) {
		ImageTransformer firstFallback = null;

		for ( ImageTransformer imageTransformer : imageTransformerRegistry ) {
			ImageTransformerPriority priority = canExecute.consider( imageTransformer );
			if ( priority == ImageTransformerPriority.PREFERRED ) {
				return imageTransformer;
			}
			else if ( priority == ImageTransformerPriority.FALLBACK && firstFallback == null ) {
				firstFallback = imageTransformer;
			}
		}

		return firstFallback;
	}

	private interface CanExecute
	{
		ImageTransformerPriority consider( ImageTransformer imageTransformer );
	}
}
