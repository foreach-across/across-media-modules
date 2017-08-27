package com.foreach.imageserver.core.config.conditional;

import com.foreach.across.core.annotations.AcrossCondition;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.imageserver.client.ImageRequestHashBuilder;
import com.foreach.imageserver.client.ImageServerClient;
import com.foreach.imageserver.core.ImageServerCoreModuleSettings;
import com.foreach.imageserver.core.client.LocalImageServerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author Arne Vandamme
 */
@AcrossCondition("${" + ImageServerCoreModuleSettings.CREATE_LOCAL_CLIENT + ":false}")
@Configuration
public class LocalImageServerClientConfiguration
{
	@Autowired
	private Environment environment;

	@Autowired(required = false)
	private ImageRequestHashBuilder serverImageRequestHashBuilder;

	@Autowired
	private ImageServerCoreModuleSettings settings;

	@Bean
	@Exposed
	public ImageServerClient localImageServerClient() {
		LocalImageServerClient client = new LocalImageServerClient(
				environment.getRequiredProperty( ImageServerCoreModuleSettings.IMAGE_SERVER_URL )
		);
		if ( !settings.isStrictMode() && serverImageRequestHashBuilder != null ) {
			client.setImageRequestHashBuilder( serverImageRequestHashBuilder );
		}
		return client;
	}
}
