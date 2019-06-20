package com.foreach.imageserver.core.transformers.ghostscript;

import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.transformers.AbstractOrderedImageCommandExecutor;
import com.foreach.imageserver.core.transformers.ImageTransformCommand;
import com.foreach.imageserver.core.transformers.ImageTransformerPriority;

/**
 * Handles transforms on PDF files using Ghostscript (wrapped with Ghost4J).
 *
 * @author Arne Vandamme
 * @since 5.0.0
 */
public class GhostscriptTransformCommandExecutor extends AbstractOrderedImageCommandExecutor<ImageTransformCommand>
{
	@Override
	public ImageTransformerPriority canExecute( ImageTransformCommand command ) {
		if ( ImageType.PDF == command.getOriginalImageAttributes().getType() ) {
			return ImageTransformerPriority.PREFERRED;
		}
		return ImageTransformerPriority.UNABLE;
	}

	@Override
	public void execute( ImageTransformCommand command ) {

	}
}

