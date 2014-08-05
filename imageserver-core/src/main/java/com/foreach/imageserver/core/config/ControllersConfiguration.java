package com.foreach.imageserver.core.config;

import com.foreach.imageserver.core.ImageServerCoreModuleSettings;
import com.foreach.imageserver.core.controllers.ImageLoadController;
import com.foreach.imageserver.core.controllers.ImageModificationController;
import com.foreach.imageserver.core.controllers.ImageStreamingController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author Arne Vandamme
 */
@Configuration
public class ControllersConfiguration
{
	@Autowired
	private Environment environment;

	@Bean
	public ImageLoadController imageLoadController() {
		return new ImageLoadController( accessToken() );
	}

	@Bean
	public ImageModificationController imageModificationController() {
		return new ImageModificationController( accessToken() );
	}

	@Bean
	public ImageStreamingController imageStreamingController() {
		ImageStreamingController imageStreamingController = new ImageStreamingController( accessToken() );
		imageStreamingController.setFallbackImageKey(
				environment.getProperty( ImageServerCoreModuleSettings.IMAGE_NOT_FOUND_IMAGEKEY, "" ) );
		imageStreamingController.setMaxCacheAgeInSeconds(
				environment.getProperty( ImageServerCoreModuleSettings.MAX_BROWSER_CACHE_SECONDS, Integer.class, 60 ) );
		imageStreamingController.setProvideStackTrace(
				environment.getProperty( ImageServerCoreModuleSettings.PROVIDE_STACKTRACE, Boolean.class, false ) );

		return imageStreamingController;
	}

	private String accessToken() {
		return environment.getProperty( ImageServerCoreModuleSettings.ACCESS_TOKEN, "azerty" );
	}
}
