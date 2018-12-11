package com.foreach.imageserver.core.config;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.filemanager.services.*;
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
import java.nio.file.Path;

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
	public static final String TEMP_REPOSITORY = "temp";
	public static final String ORIGINALS_REPOSITORY = "originals";
	public static final String VARIANTS_REPOSITORY = "variants";

	private final static PathGenerator PATH_GENERATOR = new DateFormatPathGenerator( "yyyy/MM/dd/HH" );

	private final Environment environment;
	private final ImageServerCoreModuleSettings settings;
	private final FileRepositoryRegistry fileRepositoryRegistry;

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
	public DefaultImageFileDescriptorFactory defaultImageFileDescriptorFactory(){
		return new DefaultImageFileDescriptorFactory();
	}

	@Bean
	public ImageStoreService imageStoreService() throws IOException {
		registerFileRepositories();
		return new ImageStoreServiceImpl(
				environment.getRequiredProperty( ImageServerCoreModuleSettings.IMAGE_STORE_FOLDER,
				                                 File.class ).toPath(),
				settings.getStoreSettings().getFolderPermissions(),
				settings.getStoreSettings().getFilePermissions() );
	}

	private void registerFileRepositories() {
		Path rootFolder = environment.getRequiredProperty( ImageServerCoreModuleSettings.IMAGE_STORE_FOLDER,
		                                                   File.class ).toPath();
		fileRepositoryRegistry.registerRepository( createFileRepository( TEMP_REPOSITORY, rootFolder, false ) );
		fileRepositoryRegistry.registerRepository( createFileRepository( ORIGINALS_REPOSITORY, rootFolder, true ) );
		fileRepositoryRegistry.registerRepository( createFileRepository( VARIANTS_REPOSITORY, rootFolder, true ) );
	}

	private FileRepository createFileRepository( String repositoryId, Path rootFolder, boolean withPathGenerator ) {
		LocalFileRepository repo =
				new LocalFileRepository( repositoryId, rootFolder.resolve( repositoryId ).toString() );
		if ( withPathGenerator ) {
			repo.setPathGenerator( PATH_GENERATOR );
		}
		return repo;
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
