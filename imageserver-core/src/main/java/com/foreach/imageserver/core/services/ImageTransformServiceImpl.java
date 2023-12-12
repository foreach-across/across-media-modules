package com.foreach.imageserver.core.services;

import com.foreach.across.core.annotations.RefreshableCollection;
import com.foreach.imageserver.core.config.TransformersSettings;
import com.foreach.imageserver.core.transformers.*;
import com.foreach.imageserver.dto.ImageTransformDto;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Semaphore;

@Slf4j
@Service
@EnableConfigurationProperties(TransformersSettings.class)
public class ImageTransformServiceImpl implements ImageTransformService
{
	private final Semaphore semaphore;
	private final ImageTransformUtils imageTransformUtils;

	private Collection<ImageCommandExecutor> commandExecutors = Collections.emptyList();

	@SuppressWarnings("unused")
	public ImageTransformServiceImpl( TransformersSettings transformersSettings, ImageTransformUtils imageTransformUtils ) {
		this.imageTransformUtils = imageTransformUtils;

		// Right now, we have only one ImageTransformer implementation and it runs on the local machine. In theory,
		// however, we could have implementations that off-load the actual computations to other machines. Should this
		// ever get to be the case, we may want to provide more fine-grained control over the number of concurrent
		// transformations. For now, a single limit will suffice.
		this.semaphore = new Semaphore( transformersSettings.getConcurrentLimit(), true );
	}

	@Autowired
	public void setCommandExecutors( @RefreshableCollection(includeModuleInternals = true) Collection<ImageCommandExecutor> commandExecutors ) {
		this.commandExecutors = commandExecutors;
	}

	@Override
	public ImageAttributes getAttributes( @NonNull InputStream imageStream ) {
		ImageAttributesCommand attributesCommand = ImageAttributesCommand.builder().imageStream( imageStream ).build();

		ImageCommandExecutor executor = findCommandExecutor( attributesCommand );

		if ( executor != null ) {
			executeCommand( executor, attributesCommand );

			return attributesCommand.getExecutionResult();
		}

		throw new IllegalArgumentException( "No executor available for determining image attributes" );
	}

	@Override
	@SneakyThrows
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
				try (InputStream is = source.getImageStream()) {
					attributes = getAttributes( is );
				}
			}

			source = transform( source, attributes, transform );

			attributes = null;
		}
		while ( !queue.isEmpty() );

		return source;
	}

	private ImageSource transform( ImageSource imageSource, ImageAttributes attributes, ImageTransformDto transformDto ) {
		ImageTransformDto normalizedTransform = imageTransformUtils.normalize( transformDto, attributes );
		ImageTransformCommand command = ImageTransformCommand.builder()
		                                                     .originalImage( imageSource )
		                                                     .originalImageAttributes( attributes )
		                                                     .transform( normalizedTransform )
		                                                     .build();

		ImageCommandExecutor executor = findCommandExecutor( command );

		if ( executor != null ) {
			executeCommand( executor, command );
		}
		else {
			LOG.error( "No valid executor found for {} and transform: {}", attributes, transformDto );
			throw new IllegalArgumentException(
					String.format( "No executor available for transform '%s' on image with attributes '%s'", transformDto, attributes )
			);
		}

		return command.getExecutionResult();
	}

	@SuppressWarnings("unchecked")
	private ImageCommandExecutor findCommandExecutor( ImageCommand commandToExecute ) {
		ImageCommandExecutor fallback = null;

		for ( ImageCommandExecutor candidate : commandExecutors ) {
			if ( candidate.handles( commandToExecute.getClass() ) ) {
				ImageTransformerPriority priority = candidate.canExecute( commandToExecute );
				if ( priority == ImageTransformerPriority.PREFERRED ) {
					return candidate;
				}
				else if ( priority == ImageTransformerPriority.FALLBACK && fallback == null ) {
					fallback = candidate;
				}
			}
		}

		return fallback;
	}

	@SuppressWarnings("unchecked")
	private void executeCommand( ImageCommandExecutor executor, ImageCommand command ) {
		semaphore.acquireUninterruptibly();
		try {
			executor.execute( command );
		}
		finally {
			semaphore.release();
		}
	}
}
