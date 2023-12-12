package com.foreach.imageserver.core.transformers;

import lombok.Getter;
import lombok.Setter;
import org.springframework.core.Ordered;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
public abstract class AbstractOrderedImageCommandExecutor<T extends ImageCommand> implements ImageCommandExecutor<T>
{
	@Setter
	@Getter
	private int order = Ordered.LOWEST_PRECEDENCE;
}
