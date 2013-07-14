package com.foreach.imageserver.services.transformers;

import com.foreach.imageserver.business.ImageFile;
import com.foreach.imageserver.business.ImageModifier;

public interface ImageTransformer
{
	/**
	 * Checks if the instance can execute a certain modification (without actually doing it)
	 * and returns the corresponding priority enum value.
	 *
	 * @param original Original file to be modified.
	 * @param modifier Modification to be done.
	 * @return ImageTransformerPriority value
	 */
	ImageTransformerPriority canApply( ImageFile original, ImageModifier modifier );

	/**
	 * Applies the actual modification on an image.  If the canApply() method is implemented
	 * correctly, the actual apply() can rely on only being called if the canApply() did not return UNABLE.
	 *
	 * @param original Original file to be modified.
	 * @param modifier Modification to be done.
	 * @return ImageFile with the modified image
	 */
	ImageFile apply( ImageFile original, ImageModifier modifier );

	/**
	 * Determines the preferred order into which this transformer is applied, if it can
	 * handle the modification and others can as well.
	 * Transformers with higher priority are called first.
	 *
	 * @return Priority as int.
	 */
	int getPriority();
}
