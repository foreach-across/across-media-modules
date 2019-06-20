package com.foreach.imageserver.core.transformers;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.io.InputStream;

/**
 * Represents the command to retrieve the {@link ImageAttributes} for image data.
 *
 * @author Arne Vandamme
 * @since 5.0.0
 */
@Builder(toBuilder = true)
@Getter
public class ImageAttributesCommand extends ImageCommand<ImageAttributes>
{
	/**
	 * Image data.
	 */
	@NonNull
	private final InputStream imageStream;
}
