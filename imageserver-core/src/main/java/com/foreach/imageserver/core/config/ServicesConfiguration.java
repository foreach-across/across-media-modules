package com.foreach.imageserver.core.config;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.filemanager.services.*;
import com.foreach.imageserver.core.ImageServerCoreModuleSettings;
import com.foreach.imageserver.core.rest.services.ImageRestService;
import com.foreach.imageserver.core.rest.services.ImageRestServiceImpl;
import com.foreach.imageserver.core.services.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Arne Vandamme
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class ServicesConfiguration
{
	public static final String IMAGESERVER_TEMP_REPOSITORY = "imageserver-temp";
	public static final String IMAGESERVER_ORIGINALS_REPOSITORY = "imageserver-originals";
	public static final String IMAGESERVER_VARIANTS_REPOSITORY = "imageserver-variants";

	private final static PathGenerator PATH_GENERATOR = new DateFormatPathGenerator( "yyyy/MM/dd/HH" );

	private final ImageServerCoreModuleSettings settings;
	private final FileRepositoryRegistry fileRepositoryRegistry;

	@Bean
	public ImageRepositoryRegistry imageRepositoryRegistry() {
		return new ImageRepositoryRegistry();
	}

	@Bean
	public CropGeneratorUtil cropGeneratorUtil() {
		return new CropGeneratorUtilImpl();
	}

	@Bean
	@Exposed
	public ImageRestService imageRestService() {
		ImageRestServiceImpl imageRestService = new ImageRestServiceImpl();
		imageRestService.setFallbackImageKey( settings.getStreaming().getImageNotFoundKey() );
		return imageRestService;
	}

	@Autowired
	public void autoRegisterFileRepositories() {
		File folder = settings.getStore().getFolder();
		Path rootFolder = folder != null ? folder.toPath() : null;
		createAndRegisterFileRepositoryIfNecessary( IMAGESERVER_TEMP_REPOSITORY, "temp", rootFolder, false );
		createAndRegisterFileRepositoryIfNecessary( IMAGESERVER_ORIGINALS_REPOSITORY, "originals", rootFolder, true );
		createAndRegisterFileRepositoryIfNecessary( IMAGESERVER_VARIANTS_REPOSITORY, "variants", rootFolder, true );
	}

	private FileRepository createAndRegisterFileRepositoryIfNecessary( String repositoryId,
	                                                                   String folderName,
	                                                                   Path rootFolder,
	                                                                   boolean withPathGenerator ) {
		if ( !fileRepositoryRegistry.repositoryExists( repositoryId ) ) {
			if ( rootFolder == null ) {
				LOG.warn( "File repository {} has not been initialized as no root folder has been provided.", repositoryId );
				return null;
			}
			LOG.info( "File repository '{}' does not exist. Creating a new local file repository for location '{}'.", repositoryId,
			          rootFolder + "/" + folderName );
			LocalFileRepository repo =
					new LocalFileRepository( repositoryId, rootFolder.resolve( folderName ).toString() );
			if ( withPathGenerator ) {
				repo.setPathGenerator( PATH_GENERATOR );
			}
			return fileRepositoryRegistry.registerRepository( repo );
		}

		LOG.info( "Not creating a file repository for id '{}' as it already exists.", repositoryId );
		return fileRepositoryRegistry.getRepository( repositoryId );
	}
}
