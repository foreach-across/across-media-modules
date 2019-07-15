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

package com.foreach.across.modules.filemanager.business;

import lombok.NonNull;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;

import java.io.*;
import java.net.URI;

/**
 * Represents a single {@link com.foreach.across.modules.filemanager.services.FileRepository} file,
 * identified by a {@link FileDescriptor}. Can be used to read or manipulate the file (including update and delete).
 *
 * @author Arne Vandamme
 * @since 1.4.0
 */
public interface FileResource extends WritableResource, FileRepositoryResource
{
	/**
	 * @return resource URI to this resource
	 */
	@Override
	default URI getURI() {
		return getDescriptor().toResourceURI();
	}

	/**
	 * @return the descriptor to this file resource
	 */
	FileDescriptor getDescriptor();

	/**
	 * @return the folder to which this file belongs
	 */
	FolderResource getFolderResource();

	/**
	 * Method {@code getFile()} is extended from {@link Resource} but it's advised
	 * not to implement it directly on {@link FileResource} to ensure better active
	 * management of physical files. Use either {@link #getInputStream()} to get the
	 * data of the resource, or {@link #copyTo(File)} if you require it as a physical file.
	 *
	 * @see #getInputStream()
	 * @see #copyTo(File)
	 */
	default File getFile() {
		throw new UnsupportedOperationException( "FileResource can not be resolved to java.io.File objects. Use getInputStream() or copyTo(File) instead." );
	}

	/**
	 * Delete the actual file. The return code should give an indication if
	 * delete has failed or not. Whereas {@code true} might not guarantee that a
	 * file has been deleted, {@code false} should guarantee that delete has failed.
	 * <p/>
	 * If delete was successful, subsequent calls to {@link #exists()} should return {@code false}.
	 *
	 * @return false if delete failed
	 */
	boolean delete();

	@Override
	FileResource createRelative( String relativePath );

	/**
	 * Copy the file data from a physical file into this resource.
	 * Optionally deleting the physical file when done (useful for temporary files).
	 *
	 * @param originalFile   data to copy
	 * @param deleteOriginal true if the original file should be deleted when copy is done
	 * @throws IOException thrown in case of IO error or resource not found
	 */
	default void copyFrom( @NonNull File originalFile, boolean deleteOriginal ) throws IOException {
		try (InputStream is = new FileInputStream( originalFile )) {
			try (OutputStream os = getOutputStream()) {
				IOUtils.copy( is, os );
			}
		}

		if ( deleteOriginal ) {
			FileUtils.deleteQuietly( originalFile );
		}
	}

	/**
	 * Copy the file data from another file resource into this resource.
	 * Optionally deletes the original file resource when done (useful for temporary files).
	 *
	 * @param originalFileResource whose data to copy
	 * @param deleteOriginal       true if the original file resources should be deleted when copy is done
	 * @throws IOException thrown in case of IO error or resource not found
	 */
	default void copyFrom( @NonNull FileResource originalFileResource, boolean deleteOriginal ) throws IOException {
		originalFileResource.copyTo( this );

		if ( deleteOriginal ) {
			originalFileResource.delete();
		}
	}

	/**
	 * Copy the data of any type of {@link Resource} into this file resource.
	 *
	 * @param resource whose data to copy
	 * @throws IOException thrown in case of IO error or resource not found
	 */
	default void copyFrom( @NonNull Resource resource ) throws IOException {
		try (InputStream is = resource.getInputStream()) {
			copyFrom( is );
		}
	}

	/**
	 * Copy the data from the input stream into this resource.
	 * The caller is responsible for correctly closing the input stream.
	 *
	 * @param inputStream data to copy
	 * @throws IOException thrown in case of IO error or resource not found
	 */
	@SuppressWarnings("ResultOfMethodCallIgnored")
	default void copyFrom( @NonNull InputStream inputStream ) throws IOException {
		inputStream.available();

		try (OutputStream os = getOutputStream()) {
			IOUtils.copy( inputStream, os );
		}
	}

	/**
	 * Copy the data from this file resource to the physical file specified.
	 * If the file already exists, it will be removed, and any subdirectories that
	 * do not yet exist will be created.
	 *
	 * @param file to copy the data to
	 * @throws IOException thrown in case of IO error or resource not found
	 */
	default void copyTo( @NonNull File file ) throws IOException {
		try (InputStream is = getInputStream()) {
			FileUtils.forceMkdirParent( file );
			try (OutputStream os = new FileOutputStream( file, false )) {
				IOUtils.copy( is, os );
			}
		}
	}

	/**
	 * Copy the data from this file resource to another.
	 *
	 * @param targetResource to copy the data toestLocalFile
	 * @throws IOException thrown in case of IO error or resource not found
	 */
	default void copyTo( @NonNull WritableResource targetResource ) throws IOException {
		try (InputStream is = getInputStream()) {
			try (OutputStream os = targetResource.getOutputStream()) {
				IOUtils.copy( is, os );
			}
		}

	}

	/**
	 * Copy the data from this file resource to the output stream specified.
	 * Opening and closing the output stream is the responsibility of the caller.
	 *
	 * @param outputStream to copy the data to
	 * @throws IOException thrown in case of IO error or resource not found
	 */
	default void copyTo( @NonNull OutputStream outputStream ) throws IOException {
		try (InputStream is = getInputStream()) {
			IOUtils.copy( is, outputStream );
		}
	}

	/**
	 * Additional interface that {@link FileResource} types can implement to provide
	 * access to the underlying physical {@link File}. This allows them to be used
	 * for temporary file storage. It *must* be possible to write to the target file.
	 */
	interface TargetFile
	{
		/**
		 * @return physical file instance
		 */
		File getTargetFile();
	}
}
