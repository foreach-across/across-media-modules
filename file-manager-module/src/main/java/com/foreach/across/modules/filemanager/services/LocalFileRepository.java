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
import com.foreach.across.modules.filemanager.business.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * The simplest of file repositories, storing all files in a set of local folders
 * with one root parent folder.
 *
 * @see com.foreach.across.modules.filemanager.services.PathGenerator
 */
@Slf4j
public class LocalFileRepository extends AbstractFileRepository implements FileRepository
{
	private String repositoryId;
	private String rootFolder;
	private PathGenerator pathGenerator;

	public LocalFileRepository( String repositoryId, String rootFolder ) {
		super( repositoryId );
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
//	@Override
//	public FileDescriptor createFile() {
//		FileDescriptor descriptor = buildNewDescriptor( null, null );
//
//		try {
//			File file = getAsFile( descriptor );
//			FileUtils.forceMkdir( file.getParentFile() );
//
//			if ( !file.createNewFile() ) {
//				throw new FileStorageException( "Unable to create new file " + file );
//			}
//		}
//		catch ( IOException ioe ) {
//			throw new FileStorageException( ioe );
//		}
//
//		return descriptor;
//	}

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
//	@Override
//	public FileDescriptor moveInto( File file ) {
//		FileDescriptor descriptor = buildNewDescriptor( file.getName(), null );
//
//		try {
//			File newFile = getAsFile( descriptor );
//
//			if ( !newFile.exists() ) {
//				FileUtils.forceMkdir( newFile.getParentFile() );
//			}
//
//			moveFileIfPossible( file, newFile );
//		}
//		catch ( IOException ioe ) {
//			throw new FileStorageException( ioe );
//		}
//
//		return descriptor;
//	}

	/**
	 * Based on {@link FileUtils#moveFile(java.io.File, java.io.File)}.  Copies the file into
	 * the repository and tries to delete the original.
	 */
//	private void moveFileIfPossible( File srcFile, File destFile ) throws IOException {
//		if ( srcFile == null ) {
//			throw new NullPointerException( "Source must not be null" );
//		}
//		if ( destFile == null ) {
//			throw new NullPointerException( "Destination must not be null" );
//		}
//		if ( !srcFile.exists() ) {
//			throw new FileNotFoundException( "Source '" + srcFile + "' does not exist" );
//		}
//		if ( srcFile.isDirectory() ) {
//			throw new IOException( "Source '" + srcFile + "' is a directory" );
//		}
//		if ( destFile.exists() ) {
//			throw new FileExistsException( "Destination '" + destFile + "' already exists" );
//		}
//		if ( destFile.isDirectory() ) {
//			throw new IOException( "Destination '" + destFile + "' is a directory" );
//		}
//		boolean rename = srcFile.renameTo( destFile );
//		if ( !rename ) {
//			FileUtils.copyFile( srcFile, destFile );
//			if ( !srcFile.delete() ) {
//				LOG.warn( "File {} was copied into the LocalFileRepository but could not be deleted",
//				          srcFile );
//			}
//		}
//	}

	/**
	 * Stores a file in the repository, but leaves the original file alone.
	 * If you want to move a temporary file to the repository and delete it
	 * immediately when done, use {@link #moveInto(java.io.File)} instead.
	 *
	 * @param file File instance to save in the repository.
	 * @return FileDescriptor instance.
	 */
//	@Override
//	public FileDescriptor save( File file ) {
//		FileDescriptor descriptor = buildNewDescriptor( file.getName(), null );
//
//		try {
//			File newFile = getAsFile( descriptor );
//
//			if ( !newFile.exists() ) {
//				FileUtils.forceMkdir( newFile.getParentFile() );
//
//				if ( !newFile.createNewFile() ) {
//					throw new FileStorageException( "Unable to create new file " + newFile );
//				}
//			}
//
//			FileManagerUtils.fastCopy( file, newFile );
//		}
//		catch ( IOException ioe ) {
//			throw new FileStorageException( ioe );
//		}
//
//		return descriptor;
//	}

	/**
	 * Stores an InputStream as a new file in the repository.
	 *
	 * @param inputStream InputStream of the file content.
	 * @return FileDescriptor instance.
	 */
//	@Override
//	public FileDescriptor save( InputStream inputStream ) {
//		FileDescriptor descriptor = buildNewDescriptor( null, null );
//		save( descriptor, inputStream, true );
//		return descriptor;
//	}

//	@Override
//	public void save( FileDescriptor target, InputStream inputStream, boolean replaceExisting ) {
//		if ( !StringUtils.equals( repositoryId, target.getRepositoryId() ) ) {
//			throw new IllegalArgumentException(
//					"Invalid file descriptor. File repository " + target.getRepositoryId() +
//							" can not persist a file for the provided descriptor: " + target.getUri() );
//		}
//
//		try {
//			File newFile = getAsFile( target );
//
//			if ( !newFile.exists() ) {
//				FileUtils.forceMkdir( newFile.getParentFile() );
//
//				if ( !newFile.createNewFile() ) {
//					throw new FileStorageException( "Unable to create new file " + newFile );
//				}
//			}
//			else if ( !replaceExisting ) {
//				throw new IllegalArgumentException( "Unable to save file to the given descriptor: " + target.getUri() + ". File already exists." );
//			}
//
//			FileManagerUtils.fastCopy( inputStream, newFile );
//		}
//		catch ( IOException ioe ) {
//			throw new FileStorageException( ioe );
//		}
//	}

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
//	@Override
//	public OutputStream getOutputStream( FileDescriptor descriptor ) {
//		assertValidDescriptor( descriptor );
//
//		try {
//			return Files.newOutputStream( buildPath( descriptor ),
//			                              StandardOpenOption.CREATE, StandardOpenOption.WRITE,
//			                              StandardOpenOption.TRUNCATE_EXISTING );
//		}
//		catch ( IOException ioe ) {
//			throw new FileStorageException( ioe );
//		}
//	}

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
//	@Override
//	public InputStream getInputStream( FileDescriptor descriptor ) {
//		assertValidDescriptor( descriptor );
//
//		try {
//			return new FileInputStream( getAsFile( descriptor ) );
//		}
//		catch ( FileNotFoundException fnfe ) {
//			return null;
//		}
//	}

	/**
	 * Gets the file contents as a File instance.  This file *should not be used for writing*
	 * as the file instance might be a local cached instance depending on the implementation.
	 * If you want to write directly you should use
	 * {@link #getOutputStream(FileDescriptor)}.
	 *
	 * @param descriptor FileDescriptor instance.
	 * @return File instance.
	 */
//	@Override
//	public File getAsFile( FileDescriptor descriptor ) {
//		assertValidDescriptor( descriptor );
//
//		return buildPath( descriptor ).toFile();
//	}

	/**
	 * Checks if a descriptor actually points to an existing file.
	 *
	 * @param descriptor FileDescriptor instance.
	 * @return True if the file exists.
	 */
//	@Override
//	public boolean exists( FileDescriptor descriptor ) {
//		if ( !StringUtils.equals( repositoryId, descriptor.getRepositoryId() ) ) {
//			return false;
//		}
//
//		return getAsFile( descriptor ).exists();
//	}

//	@Override
//	public boolean move( FileDescriptor source, FileDescriptor target ) {
//		String renamedRep = target.getRepositoryId();
//		String originalRep = source.getRepositoryId();
//		if ( !StringUtils.equals( originalRep, renamedRep ) ) {
//			throw new IllegalArgumentException( "Repository id of the target is different from the source." );
//		}
//
//		Path result;
//		Path renamedPath = buildPath( target );
//		try {
//			Path parent = renamedPath.getParent();
//			if ( parent != null && !Files.isDirectory( parent ) ) {
//				Files.createDirectories( parent );
//			}
//			result = Files.move( buildPath( source ), renamedPath, StandardCopyOption.ATOMIC_MOVE,
//			                     StandardCopyOption.REPLACE_EXISTING );
//		}
//		catch ( IOException e ) {
//			throw new FileStorageException( e );
//		}
//
//		return renamedPath.equals( result );
//	}

	/**
	 * Deletes a file from the repository.
	 *
	 * @param descriptor FileDescriptor instance.
	 * @return True if delete was successful, false if not (delete failed or file did not exist).
	 */
//	@Override
//	public boolean delete( FileDescriptor descriptor ) {
//		assertValidDescriptor( descriptor );
//
//		return getAsFile( descriptor ).delete();
//	}

	@Override
	protected FileResource buildFileResource( FileDescriptor descriptor ) {
		return new LocalFileResource( descriptor, buildPath( descriptor ).toFile() );
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
