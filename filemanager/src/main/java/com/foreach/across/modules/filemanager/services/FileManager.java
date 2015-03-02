package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;

import java.io.File;
import java.io.InputStream;

/**
 * Central interface providing access to the underlying file repositories.  A single FileRepository can
 * have any type of implementation (local storage, remote storage, database backed).
 * <p/>
 * This interface also implements the {@link com.foreach.across.modules.filemanager.services.FileRepository}
 * and will considerd repository methods to run either against the default file repository or (in case of
 * using a FileDescriptor) against the repository specified in the FileDescriptor.
 */
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
	FileRepository getRepository( String repositoryId );

	/**
	 * Returns the FileRepository a given File belongs to.
	 *
	 * @param descriptor FileDescriptor instance.
	 * @return FileRepository instance or null if none registered.
	 */
	FileRepository getRepository( FileDescriptor descriptor );

	/**
	 * Create a new file in the repository.  This allocates the file instance, but
	 * the content of the file is empty.  What exactly empty means depends on the
	 * implementation of the repository.
	 *
	 * @param repositoryId Id of the file repository.
	 * @return FileDescriptor instance.
	 */
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
	 */
	FileDescriptor moveInto( String repositoryId, File file );

	/**
	 * Stores a file in the repository, but leaves the original file alone.
	 * If you want to move a temporary file to the repository and delete it
	 * immediately when done, use {@link #moveInto(java.io.File)} instead.
	 *
	 * @param repositoryId Id of the file repository.
	 * @param file         File instance to save in the repository.
	 * @return FileDescriptor instance.
	 */
	FileDescriptor save( String repositoryId, File file );

	/**
	 * Stores an InputStream as a new file in the repository.
	 *
	 * @param repositoryId Id of the file repository.
	 * @param inputStream  InputStream of the file content.
	 * @return FileDescriptor instance.
	 */
	FileDescriptor save( String repositoryId, InputStream inputStream );

	/**
	 * Quick creates a physical file in the temporary FileRepository.
	 * The file returned can safely be used for writing to and can afterwards be moved
	 * to a particular FileRepository using
	 * {@link com.foreach.across.modules.filemanager.services.FileRepository#moveInto(java.io.File)}.
	 *
	 * @return A File instance to be used.
	 */
	File createTempFile();
}