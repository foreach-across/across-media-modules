package com.foreach.imageserver.core.transformers.imagemagick;

import com.foreach.imageserver.core.ImageServerCoreModuleSettings;
import org.im4java.process.ProcessStarter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * @author Arne Vandamme
 */
@Configuration
@ConditionalOnProperty(value = "imageServerCore.transformers.imageMagick.enabled", havingValue = "true")
@EnableConfigurationProperties(ImageMagickSettings.class)
class ImageMagickConfiguration
{
	@Autowired
	public void configureImageMagick( ImageMagickSettings settings ) {
		// configure global settings required by the third-party library
		ProcessStarter.setGlobalSearchPath( new File( settings.getPath() ).getAbsolutePath() );
		if ( settings.getUseGraphicsMagick() ) {
			System.setProperty( "im4java.useGM", "true" );
		}
	}

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
	ImageMagickTransformCommandExecutor imageMagickTransformCommandExecutor( ImageMagickSettings settings ) {
		ImageMagickTransformCommandExecutor transformCommandExecutor = new ImageMagickTransformCommandExecutor();
		transformCommandExecutor.setDefaultQuality( settings.getDefaultQuality() );
		transformCommandExecutor.setDefaultDpi( settings.getDefaultDpi() );
		transformCommandExecutor.setFilter( settings.getFilter() );
		transformCommandExecutor.setUseThumbnail( settings.isUseThumbnail() );
		transformCommandExecutor.setOrder( settings.getPriority() );
		return transformCommandExecutor;
	}

	@Bean
	ImageMagickAttributesCommandExecutor imageMagickAttributesCommandExecutor( ImageMagickSettings settings ) {
		ImageMagickAttributesCommandExecutor attributesCommandExecutor = new ImageMagickAttributesCommandExecutor();
		attributesCommandExecutor.setOrder( settings.getPriority() );
		return attributesCommandExecutor;
	}
}
