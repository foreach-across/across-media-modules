package com.foreach.imageserver.core.config.conditional;

import com.foreach.imageserver.core.ImageServerCoreModuleSettings;
import com.foreach.imageserver.core.transformers.ImageMagickImageTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;

/**
 * @author Arne Vandamme
 */
@ConditionalOnExpression("${" + ImageServerCoreModuleSettings.IMAGEMAGICK_ENABLED + ":false}")
@RequiredArgsConstructor
public class ImageMagickTransformerConfiguration
{
	private final ImageServerCoreModuleSettings settings;

	@Bean
	public ImageMagickImageTransformer imageMagickImageTransformer() {
		return new ImageMagickImageTransformer(
				settings.getTransformers().getImageMagick().getPriority(),
				settings.getTransformers().getImageMagick().getPath(),
				settings.getTransformers().getImageMagick().getUseGhostScript(),
				settings.getTransformers().getImageMagick().getUseGraphicsMagick()
		);
	}
}
