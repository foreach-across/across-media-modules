package com.foreach.imageserver.core.transformers.ghostscript;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Arne Vandamme
 * @since 5.0.0
 */
@Configuration
@ConditionalOnProperty(value = "image-server-core.transformers.ghostscript.enabled", havingValue = "true")
@EnableConfigurationProperties(GhostscriptSettings.class)
class GhostscriptConfiguration
{
	@Bean
	GhostscriptTransformCommandExecutor ghostscriptTransformCommandExecutor( GhostscriptSettings settings ) {
		GhostscriptTransformCommandExecutor transformCommandExecutor = new GhostscriptTransformCommandExecutor();
		transformCommandExecutor.setOrder( settings.getPriority() );
		return transformCommandExecutor;
	}

	@Bean
	// todo: experimental ghostscript example
	public GhostScriptImageTransformer ghostScriptImageTransformer() {
		return new GhostScriptImageTransformer();
	}
}
