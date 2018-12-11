package com.foreach.imageserver.core.config;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.filemanager.services.*;
import com.foreach.imageserver.core.ImageServerCoreModuleSettings;
import com.foreach.imageserver.core.rest.services.ImageRestService;
import com.foreach.imageserver.core.rest.services.ImageRestServiceImpl;
import com.foreach.imageserver.core.services.*;
import com.foreach.imageserver.core.transformers.ImageTransformerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

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
@Slf4j
public class ServicesConfiguration
{
	public static final String IMAGESERVER_TEMP = "imageserver-temp";
	public static final String IMAGESERVER_ORIGINALS = "imageserver-originals";
	public static final String IMAGESERVER_VARIANTS = "imageserver-variants";

	private final static PathGenerator PATH_GENERATOR = new DateFormatPathGenerator( "yyyy/MM/dd/HH" );

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
	public DefaultImageFileDescriptorFactory defaultImageFileDescriptorFactory() {
		return new DefaultImageFileDescriptorFactory();
	}

	@Bean
	public ImageStoreService imageStoreService() throws IOException {
		registerFileRepositories();
		return new ImageStoreServiceImpl(
				settings.getStore().getFolderPermissions(),
				settings.getStore().getFilePermissions() );
	}

	private void registerFileRepositories() {
		File folder = settings.getStore().getFolder();
		Path rootFolder = folder != null ? folder.toPath() : null;
		createAndRegisterFileRepositoryIfNecessary( IMAGESERVER_TEMP, rootFolder, false );
		createAndRegisterFileRepositoryIfNecessary( IMAGESERVER_ORIGINALS, rootFolder, true );
		createAndRegisterFileRepositoryIfNecessary( IMAGESERVER_VARIANTS, rootFolder, true );
	}

	private FileRepository createAndRegisterFileRepositoryIfNecessary( String repositoryId, Path rootFolder, boolean withPathGenerator ) {
		if ( !fileRepositoryRegistry.repositoryExists( repositoryId ) ) {
			if ( rootFolder == null ) {
				LOG.warn( "File repository {} has not been initialized as no root folder has been provided.", repositoryId );
				return null;
			}
			LOG.info( "File repository '{}' does not exist. Creating a new local file repository for location '{}'.", repositoryId,
			          rootFolder + "/" + repositoryId );
			LocalFileRepository repo =
					new LocalFileRepository( repositoryId, rootFolder.resolve( repositoryId ).toString() );
			if ( withPathGenerator ) {
				repo.setPathGenerator( PATH_GENERATOR );
			}
			return fileRepositoryRegistry.registerRepository( repo );
		}

		LOG.info( "Not creating a file repository for id '{}' as it already exists.", repositoryId );
		return fileRepositoryRegistry.getRepository( repositoryId );
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
