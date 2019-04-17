package com.foreach.imageserver.core.transformers;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.dto.ImageTransformDto;
import lombok.NonNull;
import org.springframework.core.Ordered;

public interface ImageTransformer extends Ordered
{
	/**
	 * Checks whether the implementation can execute this action. The implementation will indicate that it is
	 * entirely unable to perform the action, that it is to be treated as a fallback implementation for this action
	 * or that it is a preferred implementation for this action.
	 */
	ImageTransformerPriority canExecute( ImageCalculateDimensionsAction action );

	/**
	 * Checks whether the implementation can execute this action. The implementation will indicate that it is
	 * entirely unable to perform the action, that it is to be treated as a fallback implementation for this action
	 * or that it is a preferred implementation for this action.
	 */
	ImageTransformerPriority canExecute( GetImageAttributesAction action );

	/**
	 * Checks whether the implementation can execute this action. The implementation will indicate that it is
	 * entirely unable to perform the action, that it is to be treated as a fallback implementation for this action
	 * or that it is a preferred implementation for this action.
	 */
	ImageTransformerPriority canExecute( ImageModifyAction action );

	Dimensions execute( ImageCalculateDimensionsAction action );

	ImageAttributes execute( GetImageAttributesAction action );

	InMemoryImageSource execute( ImageModifyAction action );

	/**
	 * Apply a transform to a single image source, resulting in a single result.
	 * The transform should be applicable to the original image source, any
	 * calculation of unknown dimensions based on the original source should happen
	 * before calling the actual transformer.
	 *
	 * @param original     image source
	 * @param transformDto transform to apply
	 * @return transformed image source
	 */
	ImageSource transform( @NonNull ImageSource original, @NonNull ImageTransformDto transformDto );
}
