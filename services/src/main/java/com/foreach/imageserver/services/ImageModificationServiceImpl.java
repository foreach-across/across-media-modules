package com.foreach.imageserver.services;

import com.foreach.imageserver.business.ImageFile;
import com.foreach.imageserver.business.ImageModifier;
import com.foreach.imageserver.services.exceptions.ImageModificationException;
import com.foreach.imageserver.services.transformers.ImageTransformer;
import com.foreach.imageserver.services.transformers.ImageTransformerPriority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Will iterate over all registered ImageTransformers to find the best one to apply the modification.
 */
@Service
public class ImageModificationServiceImpl implements ImageModificationService
{
	private static final Logger LOG = LoggerFactory.getLogger( ImageModificationServiceImpl.class );

	@Autowired
	private List<ImageTransformer> transformerList;

	@PostConstruct
	private void sortTransformers() {
		LOG.info( "Sorting {} image transformers according to configured priority", transformerList.size() );
		Collections.sort( transformerList, new Comparator<ImageTransformer>()
		{
			@Override
			public int compare( ImageTransformer left, ImageTransformer right ) {
				return Integer.valueOf( right.getPriority() ).compareTo( left.getPriority() );
			}
		} );

		if ( LOG.isDebugEnabled() ) {
			LOG.debug( "The following {} image transformers have been registered, in priority order:",
			           transformerList.size() );
			for ( ImageTransformer transformer : transformerList ) {
				LOG.debug( "class: {} - priority: {}", transformer.getClass(), transformer.getPriority() );
			}
		}
	}

	@Override
	public ImageFile apply( ImageFile original, ImageModifier modifier ) {
		List<ImageTransformer> transformers = new LinkedList<ImageTransformer>();
		List<ImageTransformer> fallback = new LinkedList<ImageTransformer>();

		for ( ImageTransformer candidate : transformerList ) {
			ImageTransformerPriority priority = candidate.canApply( original, modifier );

			if ( priority != null && priority != ImageTransformerPriority.UNABLE ) {
				if ( priority == ImageTransformerPriority.PREFERRED ) {
					transformers.add( candidate );
				}
				else {
					fallback.add( candidate );
				}
			}
		}

		transformers.addAll( fallback );

		if ( transformers.isEmpty() ) {
			LOG.error( "No possible transformer to modify image {} with {}", original, modifier );
			throw new ImageModificationException( "No valid transformer for image modification" );
		}

		for ( ImageTransformer transformer : transformers ) {
			try {
				ImageFile result = transformer.apply( original, modifier );

				if ( result != null ) {
					return result;
				}
				else {
					LOG.warn(
							"ImageTransformer {} said it could handle modification {} on image {}, but it returned empty result",
							transformer, modifier, original );
				}
			}
			catch ( Exception e ) {
				LOG.warn( "ImageTransformer {} threw exception on handling modification {} on image {}: {}",
				          transformer, modifier, original, e );
			}
		}

		LOG.error( "All transformers failed trying to modify image {} with {}", original, modifier );
		throw new ImageModificationException( "All transformers failed trying to apply image modification" );
	}
}
