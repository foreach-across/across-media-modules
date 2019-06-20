/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.foreach.across.modules.filemanager.config;

import com.foreach.across.core.annotations.PostRefresh;
import com.foreach.across.core.annotations.RefreshableCollection;
import com.foreach.across.core.events.AcrossModuleBootstrappedEvent;
import com.foreach.across.modules.filemanager.FileManagerModuleSettings;
import com.foreach.across.modules.filemanager.context.FileResourceProtocolResolver;
import com.foreach.across.modules.filemanager.extensions.FileResourceResolverRegistrar;
import com.foreach.across.modules.filemanager.services.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import javax.annotation.PostConstruct;
import java.util.Collection;

/**
 * Configures the {@link FileRepositoryFactory}, temp file repository and all predefined {@link FileRepository} beans
 * on the actual {@link FileRepositoryRegistry}.
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(FileManagerModuleSettings.class)
class FileManagerConfiguration
{
	private Collection<FileRepository> fileRepositories;
	private FileRepositoryRegistry fileRepositoryRegistry;

	@RefreshableCollection(includeModuleInternals = true, incremental = true)
	public void setFileRepositories( Collection<FileRepository> fileRepositories ) {
		this.fileRepositories = fileRepositories;
	}

	@Autowired
	public void configureFileManager( FileManagerModuleSettings settings,
	                                  FileRepositoryRegistry registry,
	                                  ObjectProvider<FileRepositoryFactory> fileRepositoryFactory ) {
		this.fileRepositoryRegistry = registry;

		registerFileRepositoryFactory( settings, registry, fileRepositoryFactory );
		registerTempRepository( settings, registry );
	}

	@Autowired
	public void configureProtocolResolverInParents( ApplicationContext applicationContext ) {
		FileResourceProtocolResolver protocolResolver = new FileResourceProtocolResolver( applicationContext.getAutowireCapableBeanFactory() );

		ApplicationContext parent = applicationContext.getParent();
		while ( parent != null ) {
			if ( parent instanceof ConfigurableApplicationContext ) {
				( (ConfigurableApplicationContext) parent ).addProtocolResolver( protocolResolver );
			}
			FileResourceResolverRegistrar.registerResourcePatternResolver( protocolResolver, parent );
			parent = parent.getParent();
		}
	}

	private void registerTempRepository( FileManagerModuleSettings settings, FileRepositoryRegistry registry ) {
		// Register temp repository
		String tempFolder = settings.getTempFolder();

		if ( tempFolder != null ) {
			LOG.info( "Creating file repository for temporary files in folder {}", tempFolder );

			LocalFileRepository tempRepository = LocalFileRepository.builder()
			                                                        .repositoryId( FileManager.TEMP_REPOSITORY )
			                                                        .rootFolder( tempFolder )
			                                                        .pathGenerator( DateFormatPathGenerator.YEAR_MONTH_DAY )
			                                                        .build();

			registry.registerRepository( tempRepository );
		}
		else {
			LOG.warn( "Not creating a file repository for temporary files as no tempFolder has been set.  " +
					          "Either set the {} property or manually register {} FileRepository to enable temporary files.",
			          FileManagerModuleSettings.DEFAULT_TEMP_FOLDER,
			          FileManager.TEMP_REPOSITORY );
		}
	}

	private void registerFileRepositoryFactory( FileManagerModuleSettings settings,
	                                            FileRepositoryRegistry registry,
	                                            ObjectProvider<FileRepositoryFactory> fileRepositoryFactory ) {
		FileRepositoryFactory repositoryFactory = fileRepositoryFactory.getIfAvailable();

		if ( repositoryFactory != null ) {
			LOG.info( "Using configured FileRepositoryFactory bean: {}", repositoryFactory );
		}
		else {
			String repositoriesRoot = settings.getLocalRepositoriesRoot();

			if ( repositoriesRoot != null ) {
				LOG.info( "Creating a LocalFileRepositoryFactory with root folder {}", repositoriesRoot );

				repositoryFactory = new LocalFileRepositoryFactory( repositoriesRoot, DateFormatPathGenerator.YEAR_MONTH_DAY );
			}
			else {
				LOG.debug(
						"Not creating a LocalFileRepositoryFactory as no root folder for the repositories has been set.  " +
								"Please set the {} property manually or register a FileRepositoryFactory on the FileRepositoryRegistry.",
						FileManagerModuleSettings.LOCAL_REPOSITORIES_ROOT );
			}
		}

		if ( repositoryFactory != null ) {
			registry.setFileRepositoryFactory( repositoryFactory );
		}
		else {
			LOG.warn(
					"No FileRepositoryFactory set, this means repositories will not get created automatically. Consider providing a FileRepositoryFactory bean or setting {}",
					FileManagerModuleSettings.LOCAL_REPOSITORIES_ROOT );
		}
	}

	@EventListener
	@SuppressWarnings("unused")
	public void registerModuleRepositories( AcrossModuleBootstrappedEvent ignore ) {
		// register module repositories as early as possible
		autoRegisterFileRepositories();
	}

	@SuppressWarnings("unused")
	@PostConstruct
	@PostRefresh
	public void autoRegisterFileRepositories() {
		fileRepositories.forEach( fr -> {
			if ( !( fr instanceof FileManager ) && !fileRepositoryRegistry.repositoryExists( fr.getRepositoryId() ) ) {
				LOG.info( "Auto-registration of file repository: {}", fr.getRepositoryId() );
				fileRepositoryRegistry.registerRepository( fr );
			}
		} );
	}
}
