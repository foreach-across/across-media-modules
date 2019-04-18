package com.foreach.imageserver.core.transformers;

import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
public interface ImageCommandExecutor<T extends ImageCommand> extends Ordered
{
	default boolean handles( Class<? extends ImageCommand> commandType ) {
		Class<?> parameter = ResolvableType.forClass( getClass() ).as( ImageCommandExecutor.class ).resolveGeneric( 0 );
		return parameter.isAssignableFrom( commandType );
	}

	ImageTransformerPriority canExecute( T command );

	void execute( T command );

	default int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}
}
