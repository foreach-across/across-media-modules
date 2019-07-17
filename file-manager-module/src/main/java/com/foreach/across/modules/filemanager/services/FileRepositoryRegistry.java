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

import java.util.Collection;

/**
 * Management service to manage the registered FileRepository instances.
 *
 * @author Arne Vandamme
 * @see FileManager
 * @since 1.0.0
 */
public interface FileRepositoryRegistry
{
	/**
	 * Set the FileRepositoryFactory that should be used if a repository is looked up for
	 * the first time, but no repository is registered yet under that id.
	 *
	 * @param factory FileRepositoryFactory instance or null.
	 */
	void setFileRepositoryFactory( FileRepositoryFactory factory );

	/**
	 * Returns the FileRepository to use for the given repositoryId.  If no FileRepository is registered
	 * yet under that name, the factory will be called to create one.  Depending on the factory a new
	 * repository will actually get created or null will be returned.
	 *
	 * @param repositoryId Id of the file repository.
	 * @return FileRepository instance or null if none registered or created.
	 */
	FileRepository getRepository( String repositoryId );

	/**
	 * Checks if a FileRepository with the given id exists.  Will not attempt to create it if it does not.
	 *
	 * @param repositoryId Id of the file repository.
	 * @return True if a FileRepository is registered with that id.
	 */
	boolean repositoryExists( String repositoryId );

	/**
	 * Adds a FileRepository to the registry.  This will replace the currently registered repository with that name.
	 * This method can return a different instance of the FileRepository that is supposed to be used instead of the
	 * original passed in.  The default {@link FileManagerImpl} wraps provided {@link FileRepository} instances
	 * in a {@link FileRepositoryDelegate} so they can be replaced at runtime.
	 *
	 * @param fileRepository FileRepository instance.
	 * @see FileRepositoryDelegate
	 */
	FileRepository registerRepository( FileRepository fileRepository );

	/**
	 * @return collection of all registered repositories
	 */
	Collection<FileRepository> listRepositories();
}
