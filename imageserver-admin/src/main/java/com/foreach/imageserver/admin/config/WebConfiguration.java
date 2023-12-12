package com.foreach.imageserver.admin.config;

import com.foreach.imageserver.admin.ImageServerAdminWebModuleSettings;
import com.foreach.imageserver.admin.controllers.AdminWebAppController;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Arne Vandamme
 */
@Configuration
@RequiredArgsConstructor
public class WebConfiguration
{
	private final ImageServerAdminWebModuleSettings settings;

	@Bean
	public AdminWebAppController adminWebAppController() {
		return new AdminWebAppController( settings.getImageServerUrl(), settings.getAccessToken() );
	}
}
