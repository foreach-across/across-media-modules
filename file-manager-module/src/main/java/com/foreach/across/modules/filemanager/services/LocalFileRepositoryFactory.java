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

package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileStorageException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Standard implementation that creates LocalFileRepository instances.
 * This factory takes a root directory, every created instance will get its own root folder
 * that is a direct child of the factories root folder.  The name of the child will be the same
 * as the repository id.
 *
 * @see com.foreach.across.modules.filemanager.services.LocalFileRepository
 */
public class LocalFileRepositoryFactory implements FileRepositoryFactory
{
	private static final Logger LOG = LoggerFactory.getLogger( LocalFileRepositoryFactory.class );

	private final String rootFolder;
	private final PathGenerator pathGenerator;

	public LocalFileRepositoryFactory( String rootFolder,
	                                   PathGenerator pathGenerator ) {
		this.rootFolder = rootFolder;
		this.pathGenerator = pathGenerator;
	}

	/**
	 * Attempt to create a new FileRepository instance with the given repository id.
	 * If creation is not possible, null should be returned.
	 *
	 * @param repositoryId Unique id of the FileRepository.
	 * @return FileRepository instance or null.
	 */
	@Override
	public FileRepository create( String repositoryId ) {
		File directory = Paths.get( rootFolder, repositoryId ).toFile();

		LOG.info( "Creating a new LocalFileRepository with root folder {}", directory );

		if ( !directory.exists() ) {
			try {
				FileUtils.forceMkdir( directory );
			}
			catch ( IOException ioe ) {
				throw new FileStorageException( ioe );
			}
		}

		return LocalFileRepository.builder()
		                          .repositoryId( repositoryId )
		                          .rootFolder( directory.getAbsolutePath() )
		                          .pathGenerator( pathGenerator )
		                          .build();
	}
}
