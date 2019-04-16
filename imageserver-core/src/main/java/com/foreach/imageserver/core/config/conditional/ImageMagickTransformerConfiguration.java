package com.foreach.imageserver.core.config.conditional;

import com.foreach.imageserver.core.ImageServerCoreModuleSettings;
import com.foreach.imageserver.core.transformers.GhostScriptImageTransformer;
import com.foreach.imageserver.core.transformers.ImageMagickImageTransformer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;

/**
 * @author Arne Vandamme
 */
@ConditionalOnExpression("${" + ImageServerCoreModuleSettings.IMAGEMAGICK_ENABLED + ":false}")
public class ImageMagickTransformerConfiguration
{
	@Bean
	public ImageMagickImageTransformer imageMagickImageTransformer( ImageServerCoreModuleSettings settings ) {
		return new ImageMagickImageTransformer(
				settings.getTransformers().getImageMagick().getPriority(),
				settings.getTransformers().getImageMagick().getPath(),
				settings.getTransformers().getImageMagick().getUseGhostScript(),
				settings.getTransformers().getImageMagick().getUseGraphicsMagick()
		);
	}

	@Bean
	// todo: experimental ghostscript example
	public GhostScriptImageTransformer ghostScriptImageTransformer() {
		return new GhostScriptImageTransformer();
	}
}
