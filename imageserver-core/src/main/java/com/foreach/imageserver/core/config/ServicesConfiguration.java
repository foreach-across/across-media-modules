package com.foreach.imageserver.core.config;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.imageserver.core.ImageServerCoreModuleSettings;
import com.foreach.imageserver.core.rest.services.ImageRestService;
import com.foreach.imageserver.core.rest.services.ImageRestServiceImpl;
import com.foreach.imageserver.core.services.*;
import com.foreach.imageserver.core.transformers.ImageTransformerRegistry;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ServicesConfiguration
{
	private final Environment environment;
	private final ImageServerCoreModuleSettings settings;

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
		imageRestService.setFallbackImageKey( settings.getStreaming().getImageNotFoundKey() );
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
		return new ImageTransformServiceImpl( settings.getTransformers().getConcurrentLimit() );
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
				settings.getStoreSettings().getFolderPermissions(),
				settings.getStoreSettings().getFilePermissions() );
	}

	@Bean
	public CropGenerator cropGenerator() {
		return new CropGeneratorImpl();
	}

	@Bean
	public ImageResolutionService imageResolutionService() {
		return new ImageResolutionServiceImpl();
	}
}
