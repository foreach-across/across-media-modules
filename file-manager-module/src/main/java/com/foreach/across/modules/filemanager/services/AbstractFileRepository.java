package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.*;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Base class for {@link FileRepository} implementations that delegate all resource
 * methods to the {@link FileResource} instance. Implementations should usually only
 * implement {@link #buildFileResource(FileDescriptor)} and possibly extend {@link #validateFileDescriptor(FileDescriptor)}.
 *
 * @author Arne Vandamme
 * @since 1.4.0
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractFileRepository implements FileRepository, FileManagerAware
{
	@Getter
	private final String repositoryId;

	/**
	 * Set a custom {@link PathGenerator} that should be used for
	 * the folder id on generated descriptors (eg using {@link #createFileResource(boolean)}).
	 */
	@Setter
	@Getter(AccessLevel.PROTECTED)
	private PathGenerator pathGenerator;

	/**
	 * Set the {@link FileManager} that should be used for temporary files.
	 * If set will use {@link FileManager#createTempFile()} when a temporary file is needed,
	 * instead of the default {@link File#createTempFile(String, String)}.
	 */
	@Setter
	private FileManager fileManager;

	@Override
	public FileResource createFileResource( boolean allocateImmediately ) {
		FileDescriptor descriptor = generateFileDescriptor();
		FileResource fileResource = buildFileResource( descriptor );
		if ( allocateImmediately ) {
			try (OutputStream os = fileResource.getOutputStream()) {
				os.write( 0 );
				os.flush();
			}
			catch ( IOException ioe ) {
				throw new FileStorageException( ioe );
			}
		}
		return fileResource;
	}

	@Override
	public FileDescriptor createFile() {
		return createFileResource( true ).getDescriptor();
	}

	@Override
	public InputStream getInputStream( @NonNull FileDescriptor descriptor ) {
		try {
			return getFileResource( descriptor ).getInputStream();
		}
		catch ( IOException ioe ) {
			throw new FileStorageException( ioe );
		}
	}

	@Override
	public OutputStream getOutputStream( @NonNull FileDescriptor descriptor ) {
		try {
			return getFileResource( descriptor ).getOutputStream();
		}
		catch ( IOException ioe ) {
			throw new FileStorageException( ioe );
		}
	}

	@Override
	public FileDescriptor moveInto( File file ) {
		try {
			return createFileResource( file, true ).getDescriptor();
		}
		catch ( IOException ioe ) {
			throw new FileStorageException( ioe );
		}
	}

	@Override
	public FileDescriptor save( File file ) {
		try {
			return createFileResource( file, false ).getDescriptor();
		}
		catch ( IOException ioe ) {
			throw new FileStorageException( ioe );
		}
	}

	@Override
	public FileDescriptor save( InputStream inputStream ) {
		try {
			return createFileResource( inputStream ).getDescriptor();
		}
		catch ( IOException ioe ) {
			throw new FileStorageException( ioe );
		}
	}

	@Override
	public void save( FileDescriptor target, InputStream inputStream, boolean replaceExisting ) {
		validateFileDescriptor( target );
		FileResource fileResource = getFileResource( target );

		if ( !replaceExisting && fileResource.exists() ) {
			throw new IllegalArgumentException( "Unable to save file to the given descriptor: " + target.toString() + ". File resource already exists." );
		}

		try {
			fileResource.copyFrom( inputStream );
		}
		catch ( IOException ioe ) {
			throw new FileStorageException( ioe );
		}
	}

	@Override
	public boolean move( FileDescriptor source, FileDescriptor target ) {
		FileResource targetResource = getFileResource( target );
		FileResource sourceResource = getFileResource( source );

		try {
			targetResource.copyFrom( sourceResource );
			return sourceResource.delete();
		}
		catch ( IOException ioe ) {
			throw new FileStorageException( ioe );
		}
	}

	@Override
	public File getAsFile( FileDescriptor descriptor ) {
		try {
			File file = createTempFile();
			FileResource fileResource = getFileResource( descriptor );
			fileResource.copyTo( file );
			return file;
		}
		catch ( IOException ioe ) {
			throw new FileStorageException( ioe );
		}
	}

	@Override
	public boolean delete( FileDescriptor descriptor ) {
		return getFileResource( descriptor ).delete();
	}

	@Override
	public boolean exists( FileDescriptor descriptor ) {
		return getFileResource( descriptor ).exists();
	}

	@Override
	public FileResource getFileResource( FileDescriptor descriptor ) {
		validateFileDescriptor( descriptor );
		return buildFileResource( descriptor );
	}

	@Override
	public FileDescriptor generateFileDescriptor() {
		return FileDescriptor.of( repositoryId, pathGenerator != null ? pathGenerator.generatePath() : null,
		                          UUID.randomUUID().toString().replaceAll( "-", "" ) );
	}

	@Override
	public FolderResource getFolderResource( FolderDescriptor descriptor ) {
		validateFolderDescriptor( descriptor );
		return buildFolderResource( descriptor );
	}

	/**
	 * Validates if the descriptor is valid for this file repository.
	 * A valid descriptor means it should be possible to have an actual file resource
	 * matching it, it does not mean that the resource should already exist.
	 *
	 * @param descriptor to the file resource
	 */
	protected void validateFileDescriptor( @NonNull FileDescriptor descriptor ) {
		if ( !StringUtils.equals( repositoryId, descriptor.getRepositoryId() ) ) {
			throw new IllegalArgumentException( String.format(
					"Attempt to use a FileDescriptor of repository %s on repository %s", descriptor.getRepositoryId(),
					repositoryId ) );
		}
	}

	/**
	 * Validates if the descriptor is valid for this file repository.
	 * A valid descriptor means it should be possible to have an actual folder resource
	 * matching it, it does not mean that the resource should already exist.
	 *
	 * @param descriptor to the folder resource
	 */
	protected void validateFolderDescriptor( FolderDescriptor descriptor ) {
		if ( !StringUtils.equals( repositoryId, descriptor.getRepositoryId() ) ) {
			throw new IllegalArgumentException( String.format(
					"Attempt to use a FolderDescriptor of repository %s on repository %s", descriptor.getRepositoryId(),
					repositoryId ) );
		}
	}

	private File createTempFile() throws IOException {
		return fileManager != null ? fileManager.createTempFile() : File.createTempFile( UUID.randomUUID().toString(), "" );
	}

	/**
	 * Create the {@link FileResource} for a file descriptor.
	 * Basic validation of the descriptor will have been done in {@link #validateFileDescriptor(FileDescriptor)},
	 * this method should return the actual file resource that can be used.
	 *
	 * @param descriptor to the file resource
	 * @return file resource to use
	 */
	protected abstract FileResource buildFileResource( FileDescriptor descriptor );

	/**
	 * Create the {@link FolderResource} for a folder descriptor.
	 * Basic validation of the descriptor will have been done in {@link #validateFolderDescriptor(FolderDescriptor)},
	 * this method should return the actual folder resource that can be used.
	 *
	 * @param descriptor to the folder resource
	 * @return folder resource to use
	 */
	protected abstract FolderResource buildFolderResource( FolderDescriptor descriptor );
}
