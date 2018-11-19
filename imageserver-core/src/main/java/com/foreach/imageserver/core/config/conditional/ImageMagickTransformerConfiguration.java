package com.foreach.imageserver.core.config.conditional;

import com.foreach.imageserver.core.ImageServerCoreModuleSettings;
import com.foreach.imageserver.core.transformers.ImageMagickImageTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * @author Arne Vandamme
 */
@ConditionalOnExpression("${" + ImageServerCoreModuleSettings.IMAGEMAGICK_ENABLED + ":false}")
@RequiredArgsConstructor
public class ImageMagickTransformerConfiguration
{
	private final Environment environment;

	@Bean
	public ImageMagickImageTransformer imageMagickImageTransformer() {
		return new ImageMagickImageTransformer(
				environment.getProperty( ImageServerCoreModuleSettings.IMAGEMAGICK_PRIORITY, Integer.class, 1 ),
				environment.getProperty( ImageServerCoreModuleSettings.IMAGEMAGICK_PATH, "/usr/bin/" ),
				environment.getProperty( ImageServerCoreModuleSettings.IMAGEMAGICK_USE_GHOSTSCRIPT, Boolean.class,
				                         false ),
				environment.getProperty( ImageServerCoreModuleSettings.IMAGEMAGICK_USE_GRAPHICSMAGICK, Boolean.class,
				                         false )
		);
	}
}
