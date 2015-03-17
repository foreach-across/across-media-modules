package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface for a single file repository, allowing storing and getting of a single file.
 */
public interface FileRepository
{
	/**
	 * @return The unique id of the file repository.
	 */
	String getRepositoryId();

	/**
	 * Create a new file in the repository.  This allocates the file instance, but
	 * the content of the file is empty.  What exactly empty means depends on the
	 * implementation of the repository.
	 *
	 * @return FileDescriptor instance.
	 */
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
	 */
	FileDescriptor moveInto( File file );

	/**
	 * Stores a file in the repository, but leaves the original file alone.
	 * If you want to move a temporary file to the repository and delete it
	 * immediately when done, use {@link #moveInto(java.io.File)} instead.
	 *
	 * @param file File instance to save in the repository.
	 * @return FileDescriptor instance.
	 */
	FileDescriptor save( File file );

	/**
	 * Stores an InputStream as a new file in the repository.
	 *
	 * @param inputStream InputStream of the file content.
	 * @return FileDescriptor instance.
	 */
	FileDescriptor save( InputStream inputStream );

	/**
	 * Deletes a file from the repository.
	 *
	 * @param descriptor FileDescriptor instance.
	 * @return True if delete was successful, false if not (delete failed or file did not exist).
	 */
	boolean delete( FileDescriptor descriptor );

	/**
	 * Get an OutputStream that can be used to update the contents of the file.
	 * No matter the repository implementation, writes to the OutputStream should
	 * replace the existing contents.
	 *
	 * Depending on the implementation, this can happen on close(), flush() or instantaneously.
	 *
	 * @param descriptor FileDescriptor instance.
	 * @return OutputStream that can be used to update the contents of the file.
	 */
	OutputStream getOutputStream( FileDescriptor descriptor );

	/**
	 * Get an InputStream to read the contents of the file.
	 * Reading the stream *should not be done more than once* to ensure compatibility
	 * across FileRepository implementations.  Once a stream has been consumed, a new
	 * InputStream should be requested of the repository.
	 *
	 * If the file does not exist null will be returned.
	 *
	 * @param descriptor FileDescriptor instance.
	 * @return InputStream for the contents of the file.
	 */
	InputStream getInputStream( FileDescriptor descriptor );

	/**
	 * Gets the file contents as a File instance.  This file *should not be used for writing*
	 * as the file instance might be a local cached instance depending on the implementation.
	 * If you want to write directly you should use
	 * {@link #getOutputStream(com.foreach.across.modules.filemanager.business.FileDescriptor)}.
	 *
	 * @param descriptor FileDescriptor instance.
	 * @return File instance.
	 */
	File getAsFile( FileDescriptor descriptor );

	/**
	 * Checks if a descriptor actually points to an existing file.
	 *
	 * @param descriptor FileDescriptor instance.
	 * @return True if the file exists.
	 */
	boolean exists( FileDescriptor descriptor );

	/**
	 * Moves the file "original" into "renamed", which may not exist yet.
	 * </p>
	 * An IllegalArgumentException will be thrown if the two are not in the same repository.
	 *
	 * @param source FileDescriptor instance of the original file.
	 * @param target FileDescriptor instance of the wanted resulting file.
	 * @return True if the file was successfully moved.
	 */
	boolean move( FileDescriptor source, FileDescriptor target );
}
