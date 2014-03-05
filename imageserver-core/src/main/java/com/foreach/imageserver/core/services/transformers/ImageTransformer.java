package com.foreach.imageserver.core.services.transformers;

import com.foreach.imageserver.core.services.exceptions.ImageModificationException;

public interface ImageTransformer {
    /**
     * @return Short name of the transformer implementation.
     */
    String getName();

    /**
     * Checks if the instance can execute a certain action (without actually doing it)
     * and returns the corresponding priority enum value.
     *
     * @param action ImageTransformerAction to execute.
     * @return ImageTransformerPriority value
     */
    ImageTransformerPriority canExecute(ImageTransformerAction action);

    /**
     * Applies the actual modification on an image.  If the canExecute() method is implemented
     * correctly, the actual execute() can rely on only being called if the canExecute() did not return UNABLE.
     * After execution the result property will be set on the action instance.
     *
     * @param action ImageTransformerAction to execute.
     */
    void execute(ImageTransformerAction action) throws ImageModificationException;

    /**
     * Determines the preferred order into which this transformer is applied, if it can
     * handle the modification and others can as well.
     * Transformers with higher priority are called first.
     *
     * @return Priority as int.
     */
    int getPriority();

    /**
     * @return True if transformer can be used.
     */
    boolean isEnabled();

    /**
     * @param enabled True if transformer can be used.
     */
    void setEnabled(boolean enabled);
}
