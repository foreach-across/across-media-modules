package com.foreach.imageserver.core.config;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.imageserver.core.ImageServerCoreModuleSettings;
import com.foreach.imageserver.core.rest.services.ResolutionRestService;
import com.foreach.imageserver.core.rest.services.ResolutionRestServiceImpl;
import com.foreach.imageserver.core.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.File;
import java.io.IOException;

/**
 * @author Arne Vandamme
 */
@Configuration
public class ServicesConfiguration
{
	@Autowired
	private Environment environment;

	@Bean
	public ResolutionRestService resolutionRestService() {
		return new ResolutionRestServiceImpl();
	}

	@Bean
	@Exposed
	public ImageService imageService() {
		return new ImageServiceImpl();
	}

	@Bean
	@Exposed
	public ImageProfileService imageProfileService() {
		return new ImageProfileServiceImpl();
	}

	@Bean
	@Exposed
	public ImageTransformService imageTransformService() {
		return new ImageTransformServiceImpl(
				environment.getProperty(
						ImageServerCoreModuleSettings.TRANSFORMERS_CONCURRENT_LIMIT,
						Integer.class,
						10
				)
		);
	}

	@Bean
	@Exposed
	public ImageContextService contextService() {
		return new ImageContextServiceImpl();
	}

	@Bean
	public ImageStoreService imageStoreService() throws IOException {
		return new ImageStoreServiceImpl(
				environment.getRequiredProperty( ImageServerCoreModuleSettings.IMAGE_STORE_FOLDER,
				                                 File.class ).toPath(),
				environment.getProperty( ImageServerCoreModuleSettings.IMAGE_STORE_FOLDER_PERMISSIONS, "" ),
				environment.getProperty( ImageServerCoreModuleSettings.IMAGE_STORE_FILE_PERMISSIONS, "" ) );
	}

	@Bean
	public CropGenerator cropGenerator() {
		return new CropGeneratorImpl();
	}
}
