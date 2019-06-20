package com.foreach.imageserver.core.transformers;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
public abstract class ImageCommand<T>
{
	@Getter
	@Setter
	private T executionResult;

	public boolean isCompleted() {
		return executionResult != null;
	}
}
