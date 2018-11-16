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

import com.foreach.across.modules.filemanager.FileManagerModuleSettings;
import com.foreach.across.modules.filemanager.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class FileManagerConfiguration
{
	private static final Logger LOG = LoggerFactory.getLogger( FileManagerConfiguration.class );

	@Autowired
	private FileManagerModuleSettings settings;

	@PostConstruct
	public void registerRepositories() {
		FileRepositoryRegistry registry = fileRepositoryRegistry();

		// Register the default LocalFileRepositoryFactory
		String repositoriesRoot = settings.getLocalRepositoriesRoot();

		if ( repositoriesRoot != null ) {
			LOG.info( "Creating a LocalFileRepositoryFactory with root folder {}", repositoriesRoot );

			LocalFileRepositoryFactory factory = new LocalFileRepositoryFactory( repositoriesRoot,
			                                                                     DateFormatPathGenerator.YEAR_MONTH_DAY );
			registry.setFileRepositoryFactory( factory );
		}
		else {
			LOG.warn(
					"Not creating a LocalFileRepositoryFactory as no root folder for the repositories has been set.  " +
							"Please set the {} property manually register a FileRepositoryFactory on the FileRepositoryRegistry.",
					FileManagerModuleSettings.LOCAL_REPOSITORIES_ROOT );
		}

		// Register temp repository
		String tempFolder = settings.getTempFolder();

		if ( tempFolder != null ) {
			LOG.info( "Creating file repository for temporary files in folder {}", tempFolder );

			LocalFileRepository tempRepository = new LocalFileRepository( FileManager.TEMP_REPOSITORY, tempFolder );
			tempRepository.setPathGenerator( DateFormatPathGenerator.YEAR_MONTH_DAY );

			registry.registerRepository( tempRepository );
		}
		else {
			LOG.warn( "Not creating a file repository for temporary files as no tempFolder has been set.  " +
					          "Either set the {} property or manually register {} FileRepository to enable temporary files.",
			          FileManagerModuleSettings.DEFAULT_TEMP_FOLDER,
			          FileManager.TEMP_REPOSITORY );
		}
	}

	@Bean
	public FileManager fileManager() {
		return new FileManagerImpl();
	}

	@Bean
	public FileRepositoryRegistry fileRepositoryRegistry() {
		return (FileManagerImpl) fileManager();
	}
}
