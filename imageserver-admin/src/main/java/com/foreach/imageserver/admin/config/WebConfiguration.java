package com.foreach.imageserver.admin.config;

import com.foreach.imageserver.admin.ImageServerAdminWebModuleSettings;
import com.foreach.imageserver.admin.controllers.AdminWebAppController;
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

	@Bean
	public AdminWebAppController adminWebAppController() {
		return new AdminWebAppController(
				environment.getProperty( ImageServerAdminWebModuleSettings.IMAGE_SERVER_URL, "" ),
				environment.getRequiredProperty( ImageServerAdminWebModuleSettings.ACCESS_TOKEN )
		);
	}
}
