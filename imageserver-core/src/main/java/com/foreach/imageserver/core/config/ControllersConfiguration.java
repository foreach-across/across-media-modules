package com.foreach.imageserver.core.config;

import com.foreach.imageserver.core.controllers.ImageLoadController;
import com.foreach.imageserver.core.controllers.ImageModificationController;
import com.foreach.imageserver.core.controllers.ImageStreamingController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Arne Vandamme
 */
@Configuration
public class ControllersConfiguration
{
	@Bean
	public ImageLoadController imageLoadController() {
		return new ImageLoadController();
	}

	@Bean
	public ImageModificationController imageModificationController() {
		return new ImageModificationController();
	}

	@Bean
	public ImageStreamingController imageStreamingController() {
		return new ImageStreamingController();
	}
}
