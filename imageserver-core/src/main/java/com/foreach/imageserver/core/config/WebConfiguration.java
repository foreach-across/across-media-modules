package com.foreach.imageserver.core.config;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.web.mvc.PrefixingRequestMappingHandlerMapping;
import com.foreach.imageserver.core.ImageServerCoreModuleSettings;
import com.foreach.imageserver.core.annotations.ImageServerController;
import com.foreach.imageserver.core.controllers.ImageLoadController;
import com.foreach.imageserver.core.controllers.ImageModificationController;
import com.foreach.imageserver.core.controllers.ImageStreamingController;
import org.springframework.aop.support.annotation.AnnotationClassFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author Arne Vandamme
 */
@Configuration
public class WebConfiguration
{
	@Autowired
	private Environment environment;

	/**
	 * Separate handlerMapping that allows its own interceptor collection (for reasons of performance).
	 */
	@Bean
	@Exposed
	public PrefixingRequestMappingHandlerMapping imageServerHandlerMapping() {
		return new PrefixingRequestMappingHandlerMapping(
				environment.getProperty( ImageServerCoreModuleSettings.ROOT_PATH, "" ),
				new AnnotationClassFilter( ImageServerController.class, true )
		);
	}

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
