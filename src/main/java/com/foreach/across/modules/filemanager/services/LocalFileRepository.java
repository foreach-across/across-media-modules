package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileStorageException;
import com.foreach.across.modules.filemanager.utils.FileManagerUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

/**
 * The simplest of file repositories, storing all files in a set of local folders
 * with one root parent folder.
 *
 * @see com.foreach.across.modules.filemanager.services.PathGenerator
 */
public class LocalFileRepository implements FileRepository
{
	private String repositoryId;
	private String rootFolder;
	private PathGenerator pathGenerator;

	public LocalFileRepository( String repositoryId, String rootFolder ) {
		this.repositoryId = repositoryId;
		this.rootFolder = rootFolder;
	}

	public void setPathGenerator( PathGenerator pathGenerator ) {
		this.pathGenerator = pathGenerator;
	}

	public String getRootFolder() {
		return rootFolder;
	}

	/**
	 * @return The unique id of the file repository.
	 */
	@Override
	public String getRepositoryId() {
		return repositoryId;
	}

	/**
	 * Create a new file in the repository.  This allocates the file instance, but
	 * the content of the file is empty.  What exactly empty means depends on the
	 * implementation of the repository.
	 *
	 * @return FileDescriptor instance.
	 */
	@Override
	public FileDescriptor createFile() {
		FileDescriptor descriptor = buildNewDescriptor( null, null );

		try {
			File file = getAsFile( descriptor );
			FileUtils.forceMkdir( file.getParentFile() );

			if ( !file.createNewFile() ) {
				throw new FileStorageException( "Unable to create new file " + file );
			}
		}
		catch ( IOException ioe ) {
			throw new FileStorageException( ioe );
		}

		return descriptor;
	}

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
	@Override
	public FileDescriptor moveInto( File file ) {
		FileDescriptor descriptor = buildNewDescriptor( file.getName(), null );

		try {
			File newFile = getAsFile( descriptor );

			if ( !newFile.exists() ) {
				FileUtils.forceMkdir( newFile.getParentFile() );
			}

			FileUtils.moveFile( file, newFile );
		}
		catch ( IOException ioe ) {
			throw new FileStorageException( ioe );
		}

		return descriptor;
	}

	/**
	 * Stores a file in the repository, but leaves the original file alone.
	 * If you want to move a temporary file to the repository and delete it
	 * immediately when done, use {@link #moveInto(java.io.File)} instead.
	 *
	 * @param file File instance to save in the repository.
	 * @return FileDescriptor instance.
	 */
	@Override
	public FileDescriptor save( File file ) {
		FileDescriptor descriptor = buildNewDescriptor( file.getName(), null );

		try {
			File newFile = getAsFile( descriptor );

			if ( !newFile.exists() ) {
				FileUtils.forceMkdir( newFile.getParentFile() );

				if ( !newFile.createNewFile() ) {
					throw new FileStorageException( "Unable to create new file " + newFile );
				}
			}

			FileManagerUtils.fastCopy( file, newFile );
		}
		catch ( IOException ioe ) {
			throw new FileStorageException( ioe );
		}

		return descriptor;
	}

	/**
	 * Stores an InputStream as a new file in the repository.
	 *
	 * @param inputStream InputStream of the file content.
	 * @return FileDescriptor instance.
	 */
	@Override
	public FileDescriptor save( InputStream inputStream ) {
		FileDescriptor descriptor = buildNewDescriptor( null, null );

		try {
			File newFile = getAsFile( descriptor );

			if ( !newFile.exists() ) {
				FileUtils.forceMkdir( newFile.getParentFile() );

				if ( !newFile.createNewFile() ) {
					throw new FileStorageException( "Unable to create new file " + newFile );
				}
			}

			FileManagerUtils.fastCopy( inputStream, newFile );
		}
		catch ( IOException ioe ) {
			throw new FileStorageException( ioe );
		}

		return descriptor;
	}

	/**
	 * Get an OutputStream that can be used to update the contents of the file.
	 * No matter the repository implementation, writes to the OutputStream should
	 * replace the existing contents.
	 * <p/>
	 * Depending on the implementation, this can happen on close(), flush() or instantaneously.
	 *
	 * @param descriptor FileDescriptor instance.
	 * @return OutputStream that can be used to update the contents of the file.
	 */
	@Override
	public OutputStream getOutputStream( FileDescriptor descriptor ) {
		assertValidDescriptor( descriptor );

		try {
			return Files.newOutputStream( buildPath( descriptor ),
			                              StandardOpenOption.CREATE, StandardOpenOption.WRITE,
			                              StandardOpenOption.TRUNCATE_EXISTING );
		}
		catch ( IOException ioe ) {
			throw new FileStorageException( ioe );
		}
	}

	/**
	 * Get an InputStream to read the contents of the file.
	 * Reading the stream *should not be done more than once* to ensure compatibility
	 * across FileRepository implementations.  Once a stream has been consumed, a new
	 * InputStream should be requested of the repository.
	 * <p/>
	 * If the file does not exist null will be returned.
	 *
	 * @param descriptor FileDescriptor instance.
	 * @return InputStream for the contents of the file.
	 */
	@Override
	public InputStream getInputStream( FileDescriptor descriptor ) {
		assertValidDescriptor( descriptor );

		try {
			return new FileInputStream( getAsFile( descriptor ) );
		}
		catch ( FileNotFoundException fnfe ) {
			return null;
		}
	}

	/**
	 * Gets the file contents as a File instance.  This file *should not be used for writing*
	 * as the file instance might be a local cached instance depending on the implementation.
	 * If you want to write directly you should use
	 * {@link #getOutputStream(com.foreach.across.modules.filemanager.business.FileDescriptor)}.
	 *
	 * @param descriptor FileDescriptor instance.
	 * @return File instance.
	 */
	@Override
	public File getAsFile( FileDescriptor descriptor ) {
		assertValidDescriptor( descriptor );

		return buildPath( descriptor ).toFile();
	}

	/**
	 * Checks if a descriptor actually points to an existing file.
	 *
	 * @param descriptor FileDescriptor instance.
	 * @return True if the file exists.
	 */
	@Override
	public boolean exists( FileDescriptor descriptor ) {
		if ( !StringUtils.equals( repositoryId, descriptor.getRepositoryId() ) ) {
			return false;
		}

		return getAsFile( descriptor ).exists();
	}

	/**
	 * Deletes a file from the repository.
	 *
	 * @param descriptor FileDescriptor instance.
	 * @return True if delete was successful, false if not (delete failed or file did not exist).
	 */
	@Override
	public boolean delete( FileDescriptor descriptor ) {
		assertValidDescriptor( descriptor );

		return getAsFile( descriptor ).delete();
	}

	private Path buildPath( FileDescriptor descriptor ) {
		if ( descriptor.getFolderId() != null ) {
			return Paths.get( rootFolder, descriptor.getFolderId(), descriptor.getFileId() );
		}

		return Paths.get( rootFolder, descriptor.getFileId() );
	}

	private FileDescriptor buildNewDescriptor( String proposedName, String proposedPath ) {
		String extension = FilenameUtils.getExtension( proposedName );
		String fileName = UUID.randomUUID().toString() + ( !StringUtils.isBlank( extension ) ? "." + extension : "" );

		String path = pathGenerator != null ? pathGenerator.generatePath() : null;

		return new FileDescriptor( repositoryId, path, fileName );
	}

	private void assertValidDescriptor( FileDescriptor descriptor ) {
		if ( !StringUtils.equals( repositoryId, descriptor.getRepositoryId() ) ) {
			throw new FileStorageException( String.format(
					"Attempt to use a FileDescriptor of repository %s on repository %s", descriptor.getRepositoryId(),
					repositoryId ) );
		}
	}
}
