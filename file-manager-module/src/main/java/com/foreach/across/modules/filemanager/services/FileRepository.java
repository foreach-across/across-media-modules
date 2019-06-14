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

import com.foreach.across.modules.filemanager.business.*;
import lombok.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

/**
 * Interface for a single file repository, allowing storing and getting of a single file.
 *
 * @author Arne Vandamme
 * @see AbstractFileRepository
 * @since 1.0.0
 */
public interface FileRepository
{
	/**
	 * @return The unique id of the file repository.
	 */
	String getRepositoryId();

	/**
	 * Create a new file in the repository.  This allocates the file resource and
	 * copies the data of the {@code file} parameter.
	 * <p/>
	 * If parameter {@code deleteOriginal} is {@code true}, the original {@code file}
	 * will be deleted once the resource has been created. This is useful when moving
	 * temporary files.
	 * <p/>
	 * By default the extension of the original file will be added to the file descriptor,
	 * so it can more easily be used for mime type detection.
	 *
	 * @param originalFile   data to copy into the resource
	 * @param deleteOriginal true if the original file should be deleted when done
	 * @return FileResource instance.
	 */
	default FileResource createFileResource( @NonNull File originalFile, boolean deleteOriginal ) throws IOException {
		FileDescriptor descriptor = generateFileDescriptor().withExtensionFrom( originalFile.getName() );
		FileResource fileResource = getFileResource( descriptor );
		fileResource.copyFrom( originalFile, deleteOriginal );
		return fileResource;
	}

	/**
	 * Create a new file in the repository. This allocates the file resource and copies the
	 * data represented by the {@code inputStream} parameter. It is the responsibility of
	 * the caller to correctly close the input stream when done.
	 * <p/>
	 * By default this does the same as {@link #createFileResource()} followed by a call to
	 * {@link FileResource#copyFrom(InputStream)}.
	 *
	 * @param inputStream data to copy into the resource
	 * @return FileResource instance
	 */
	default FileResource createFileResource( @NonNull InputStream inputStream ) throws IOException {
		FileResource fileResource = createFileResource();
		fileResource.copyFrom( inputStream );
		return fileResource;
	}

	/**
	 * Create a new file resource in the repository. The resource returned is guaranteed not to exist
	 * before the call to this method, and can normally be written to (can effectively be created).
	 * <p/>
	 * This does not allocate the physical storage for the actual file, that should only happen when
	 * actual data is written to the file resource. If you want to create a new file resource and immediately
	 * allocate it, use {@link #createFileResource(boolean)} instead.
	 * <p/>
	 * This method usually does the same as calling {@code createFileResource(false)}.
	 *
	 * @return FileResource instance.
	 */
	default FileResource createFileResource() {
		return createFileResource( false );
	}

	/**
	 * Create a new file resource in the repository and optionally allocate it immediately.
	 * The resource returned is guaranteed not to exist before the call to this method, and can normally be written to after.
	 * The call to {@link FileResource#exists()} should return {@code true} if parameter {@code allocateImmediately}
	 * was {@code true}.
	 * <p/>
	 * Allocating the file usually creates an empty physical file or the equivalent for the underlying storage engine.
	 *
	 * @param allocateImmediately true if
	 * @return FileResource instance.
	 */
	FileResource createFileResource( boolean allocateImmediately );

	/**
	 * Returns the {@link FileResource} represented by the descriptor. Should never be {@code null}.
	 * An {@link IllegalArgumentException} will be thrown if the descriptor does not match the repository
	 * or is illegal in any way.
	 * <p/>
	 * Note that this does not mean that the actual file resource exists. Only a call to {@link FileResource#exists()}
	 * would verify that. The return value simply implies that a resource matching that descriptor could
	 * exist or be created.
	 *
	 * @param descriptor to get the file resource for
	 * @return file resource
	 */
	FileResource getFileResource( @NonNull FileDescriptor descriptor );

	/**
	 * Find all file resources matching the given ANT pattern.
	 *
	 * @param pattern to match
	 * @return resources
	 */
	default Collection<FileResource> findFiles( @NonNull String pattern ) {
		return getRootFolderResource().findFiles( pattern );
	}

	/**
	 * Find all resources matching the given ANT pattern in this repository.
	 * This is the equivalent of starting a search from the root folder of the repository.
	 *
	 * @param pattern      to match
	 * @param resourceType type of resources to return
	 * @return resources
	 */
	default <U extends FileRepositoryResource> Collection<U> findResources( @NonNull String pattern, Class<U> resourceType ) {
		return getRootFolderResource().findResources( pattern, resourceType );
	}

	/**
	 * Find all resources matching the given ANT pattern.
	 *
	 * @param pattern to match
	 * @return resources
	 */
	default Collection<FileRepositoryResource> findResources( @NonNull String pattern ) {
		return getRootFolderResource().findResources( pattern );
	}

	/**
	 * Get the {@link FolderResource} representing the root of this repository.
	 *
	 * @return root folder resources
	 */
	default FolderResource getRootFolderResource() {
		return getFolderResource( FolderDescriptor.rootFolder( getRepositoryId() ) );
	}

	/**
	 * Returns the {@link FolderResource} represented by the descriptors, should never be {@code null}.
	 * An {@link IllegalArgumentException} will be thrown if the descriptor does not match the repository
	 * or is illegal in any way.
	 * <p/>
	 * Note that this does not mean that the actual folder resource exists. Only a call to {@link FolderResource#exists()}
	 * would verify that. The return value simply implies that a resource matching that descriptor could
	 * exist or be created.
	 *
	 * @param descriptor to get the folder resource for
	 * @return folder resource
	 */
	FolderResource getFolderResource( @NonNull FolderDescriptor descriptor );

	/**
	 * Quick check if a descriptor actually points to an existing file resource.
	 * Equivalent of doing {@link FileResource#exists()}.
	 *
	 * @param descriptor FileDescriptor instance.
	 * @return True if the file exists.
	 */
	boolean exists( @NonNull FileDescriptor descriptor );

	/**
	 * Deletes a file from the repository.
	 * The return value is a hint concerning the delete action as the correct status information
	 * depends on the underlying storage engine.
	 * <p/>
	 * In general a return value of {@code true} means there was no obvious failure deleting the file,
	 * but does not guarantee that all resources have been deleted. On the other hand, a return value
	 * of {@code false} guarantees that the file resource was <strong>not</strong> deleted and still exists.
	 *
	 * @param descriptor FileDescriptor instance.
	 * @return false if delete failed
	 */
	boolean delete( FileDescriptor descriptor );

	/**
	 * Generate a unique file descriptor which can be used for creating a new file resource in this repository.
	 *
	 * @return usable file descriptor
	 */
	FileDescriptor generateFileDescriptor();

	/**
	 * Create a new file in the repository.  This allocates the file instance, but
	 * the content of the file is empty.  What exactly empty means depends on the
	 * implementation of the repository.
	 * <p/>
	 * Deprecated as of 1.4.0, us {@link #createFileResource(boolean)} instead and
	 * explicitly indicate if a blank file should be allocated.
	 *
	 * @return FileDescriptor instance.
	 * @deprecated since 1.4.0 - see {@link #createFileResource(boolean)}
	 */
	@Deprecated
	FileDescriptor createFile();

	/**
	 * Moves a File into the repository, once the file is fully available in the
	 * repository an attempt will be made to delete the original file.
	 * <p/>
	 * No exception will be thrown if the delete fails as the new instance will be
	 * available in the repository already and this is considered the important part
	 * of the transaction.
	 *
	 * @param file File instance to move into the repository.
	 * @return FileDescriptor instance.
	 * @deprecated since 1.4.0 - use {@link #createFileResource(File, boolean)} instead
	 */
	@Deprecated
	FileDescriptor moveInto( File file );

	/**
	 * Stores a file in the repository, but leaves the original file alone.
	 * If you want to move a temporary file to the repository and delete it
	 * immediately when done, use {@link #moveInto(java.io.File)} instead.
	 *
	 * @param file File instance to save in the repository.
	 * @return FileDescriptor instance.
	 * @deprecated since 1.4.0 - use {@link #createFileResource(File, boolean)} instead
	 */
	@Deprecated
	FileDescriptor save( File file );

	/**
	 * Stores an InputStream as a new file in the repository.
	 *
	 * @param inputStream InputStream of the file content.
	 * @return FileDescriptor instance.
	 * @deprecated since 1.4.0 - use {@link #createFileResource(InputStream)} instead
	 */
	@Deprecated
	FileDescriptor save( InputStream inputStream );

	/**
	 * Stores a file for a specified {@link FileDescriptor} in the repository.
	 * The repository defined in the file descriptor should match the {@link FileRepository} the method is executed for.
	 * <p/>
	 * If the file may not be replaced and a file exists, an exception will be thrown.
	 *
	 * @param target          FileDescriptor that should be used for the file.
	 * @param inputStream     InputStream of the file content.
	 * @param replaceExisting Whether an existing file at the same location should be replaced.
	 * @return FileDescriptor instance.
	 * @deprecated since 1.4.0 - use {@link #getFileResource(FileDescriptor)} and {@link FileResource#copyFrom(InputStream)} instead
	 */
	@Deprecated
	void save( FileDescriptor target, InputStream inputStream, boolean replaceExisting );

	/**
	 * Get an OutputStream that can be used to update the contents of the file.
	 * No matter the repository implementation, writes to the OutputStream should
	 * replace the existing contents.
	 * <p>
	 * Depending on the implementation, this can happen on close(), flush() or instantaneously.
	 *
	 * @param descriptor FileDescriptor instance.
	 * @return OutputStream that can be used to update the contents of the file.
	 * @deprecated since 1.4.0 - use {@link #getFileResource(FileDescriptor)} and {@link FileResource#getOutputStream()} instead
	 */
	@Deprecated
	OutputStream getOutputStream( FileDescriptor descriptor );

	/**
	 * Get an InputStream to read the contents of the file.
	 * Reading the stream *should not be done more than once* to ensure compatibility
	 * across FileRepository implementations.  Once a stream has been consumed, a new
	 * InputStream should be requested of the repository.
	 * <p>
	 * If the file does not exist null will be returned.
	 *
	 * @param descriptor FileDescriptor instance.
	 * @return InputStream for the contents of the file.
	 * @deprecated since 1.4.0 - use {@link #getFileResource(FileDescriptor)} and {@link FileResource#getOutputStream()} instead
	 */
	@Deprecated
	InputStream getInputStream( FileDescriptor descriptor );

	/**
	 * Gets the file contents as a File instance.  This file *should not be used for writing*
	 * as the file instance might be a local cached instance depending on the implementation.
	 * If you want to write directly you should use {@link #getOutputStream(FileDescriptor)}.
	 * <p/>
	 * WARNING: As of 1.4.0 this method has been deprecated and you should avoid using it altogether.
	 * If you want to retrieve a resource as a file, use the {@link FileResource#copyTo(File)} instead and
	 * explicitly provide the file instance where you would like the output. However, you should avoid
	 * using {@code File} and use the {@link InputStream} wherever possible.
	 *
	 * @param descriptor FileDescriptor instance.
	 * @return File instance.
	 * @deprecated since 1.4.0 - use {@link #getFileResource(FileDescriptor)} and {@link FileResource#copyTo(File)} instead
	 */
	@Deprecated
	File getAsFile( FileDescriptor descriptor );

	/**
	 * Moves the file "original" into "renamed", which may not exist yet.
	 * </p>
	 * An IllegalArgumentException will be thrown if the two are not in the same repository.
	 *
	 * @param source FileDescriptor instance of the original file.
	 * @param target FileDescriptor instance of the wanted resulting file.
	 * @return True if the file was successfully moved.
	 * @deprecated since 1.4.0 - manage this action manually using {@link FileResource#copyFrom(FileResource, boolean)} instead
	 */
	@Deprecated
	boolean move( FileDescriptor source, FileDescriptor target );
}
