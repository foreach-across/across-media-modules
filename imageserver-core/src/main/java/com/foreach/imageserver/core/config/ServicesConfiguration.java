package com.foreach.imageserver.core.config;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.imageserver.core.ImageServerCoreModuleSettings;
import com.foreach.imageserver.core.rest.services.ImageRestService;
import com.foreach.imageserver.core.rest.services.ImageRestServiceImpl;
import com.foreach.imageserver.core.services.*;
import com.foreach.imageserver.core.transformers.ImageTransformerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.File;
import java.io.IOException;

/**
 * @author Arne Vandamme
 */
@ComponentScan(
		basePackages = { "com.foreach.imageserver.core.managers" }
)
@Configuration
public class ServicesConfiguration
{
	@Autowired
	private Environment environment;

	@Bean
	public ImageTransformerRegistry imageTransformerRegistry() {
		return new ImageTransformerRegistry();
	}

	@Bean
	public ImageRepositoryRegistry imageRepositoryRegistry() {
		return new ImageRepositoryRegistry();
	}

	@Bean
	public CropGeneratorUtil cropGeneratorUtil() {
		return new CropGeneratorUtilImpl();
	}

	@Bean
	public ImageRestService imageRestService() {
		ImageRestServiceImpl imageRestService = new ImageRestServiceImpl();
		imageRestService.setFallbackImageKey(
				environment.getProperty( ImageServerCoreModuleSettings.IMAGE_NOT_FOUND_IMAGEKEY, "" )
		);

		return imageRestService;
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
