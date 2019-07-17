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

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import lombok.NonNull;

import java.io.File;
import java.io.InputStream;

/**
 * Central interface providing access to the underlying file repositories.  A single FileRepository can
 * have any type of implementation (local storage, remote storage, database backed).
 * <p/>
 * This interface also implements the {@link com.foreach.across.modules.filemanager.services.FileRepository}
 * and will considered repository methods to run either against the default file repository or (in case of
 * using a FileDescriptor) against the repository specified in the FileDescriptor.
 *
 * @author Arne Vandamme
 * @see FileManagerImpl
 * @since 1.0.0
 */
@SuppressWarnings("squid:S1214" /* constants in interface */)
public interface FileManager extends FileRepository
{
	String DEFAULT_REPOSITORY = "default";
	String TEMP_REPOSITORY = "temp";

	/**
	 * Returns the FileRepository to use for the given repositoryId.
	 *
	 * @param repositoryId Id of the file repository.
	 * @return FileRepository instance or null if none registered.
	 */
	FileRepository getRepository( @NonNull String repositoryId );

	/**
	 * Returns the FileRepository a given File belongs to.
	 *
	 * @param descriptor FileDescriptor instance.
	 * @return FileRepository instance or null if none registered.
	 */
	FileRepository getRepository( @NonNull FileDescriptor descriptor );

	/**
	 * Create a new writable file resource in the given repository.
	 *
	 * @param repositoryId repository
	 * @return file resource
	 */
	FileResource createFileResource( @NonNull String repositoryId );

	/**
	 * Create a new file in the repository.  This allocates the file instance, but
	 * the content of the file is empty.  What exactly empty means depends on the
	 * implementation of the repository.
	 *
	 * @param repositoryId Id of the file repository.
	 * @return FileDescriptor instance.
	 * @deprecated since 1.4.0 - use {@link #createFileResource(String)} instead
	 */
	@Deprecated
	FileDescriptor createFile( String repositoryId );

	/**
	 * Moves a File into the repository, once the file is fully available in the
	 * repository an attempt will be made to delete the original file.
	 * <p/>
	 * No exception will be thrown if the delete fails as the new instance will be
	 * available in the repository already and this is considered the important part
	 * of the transaction.
	 *
	 * @param repositoryId Id of the file repository.
	 * @param file         File instance to move into the repository.
	 * @return FileDescriptor instance.
	 * @deprecated since 1.4.0 - use {@link #createFileResource(String)} instead
	 */
	@Deprecated
	FileDescriptor moveInto( String repositoryId, File file );

	/**
	 * Stores a file in the repository, but leaves the original file alone.
	 * If you want to move a temporary file to the repository and delete it
	 * immediately when done, use {@link #moveInto(java.io.File)} instead.
	 *
	 * @param repositoryId Id of the file repository.
	 * @param file         File instance to save in the repository.
	 * @return FileDescriptor instance.
	 * @deprecated since 1.4.0 - use {@link #createFileResource(String)} instead
	 */
	@Deprecated
	FileDescriptor save( String repositoryId, File file );

	/**
	 * Stores an InputStream as a new file in the repository.
	 *
	 * @param repositoryId Id of the file repository.
	 * @param inputStream  InputStream of the file content.
	 * @return FileDescriptor instance.
	 * @deprecated since 1.4.0 - use {@link #createFileResource(String)} instead
	 */
	@Deprecated
	FileDescriptor save( String repositoryId, InputStream inputStream );

	/**
	 * Quick creates a physical file in the temporary FileRepository.
	 * The file returned can safely be used for writing to and can afterwards be moved
	 * into a specific file resource.
	 *
	 * @return A File instance to be used.
	 */
	File createTempFile();
}
