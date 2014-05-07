package com.foreach.imageserver.core.transformers;

import com.foreach.imageserver.core.business.Dimensions;

public interface ImageTransformer {
    /**
     * Checks whether the implementation can execute this action. The implementation will indicate that it is
     * entirely unable to perform the action, that it is to be treated as a fallback implementation for this action
     * or that it is a preferred implementation for this action.
     */
    ImageTransformerPriority canExecute(ImageCalculateDimensionsAction action);

    /**
     * Checks whether the implementation can execute this action. The implementation will indicate that it is
     * entirely unable to perform the action, that it is to be treated as a fallback implementation for this action
     * or that it is a preferred implementation for this action.
     */
    ImageTransformerPriority canExecute(GetImageAttributesAction action);

    /**
     * Checks whether the implementation can execute this action. The implementation will indicate that it is
     * entirely unable to perform the action, that it is to be treated as a fallback implementation for this action
     * or that it is a preferred implementation for this action.
     */
    ImageTransformerPriority canExecute(ImageModifyAction action);

    Dimensions execute(ImageCalculateDimensionsAction action);

    ImageAttributes execute(GetImageAttributesAction action);

    InMemoryImageSource execute(ImageModifyAction action);

    /**
     * Multiple ImageTransformer beans will be ordered using this value.
     */
    int getOrder();
}
