package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Represents a single file on a {@link LocalFileRepository}.
 *
 * @author Arne Vandamme
 * @since 1.4.0
 */
class LocalFileResource extends FileSystemResource implements FileResource
{
	@Getter
	private final FileDescriptor fileDescriptor;

	LocalFileResource( @NonNull FileDescriptor fileDescriptor, @NonNull File file ) {
		super( file );
		this.fileDescriptor = fileDescriptor;
	}

	@Override
	public String getFilename() {
		return fileDescriptor.getFileId();
	}

	@Override
	public URI getURI() {
		return fileDescriptor.toResourceURI();
	}

	@Override
	public URL getURL() {
		throw new UnsupportedOperationException( "URL is not supported for a FileManagerModule FileResource" );
	}

	@Override
	public String getDescription() {
		return "axfs [" + fileDescriptor.toString() + "] -> " + super.getDescription();
	}

	@Override
	public File getFile() {
		throw new UnsupportedOperationException( "FileResource can not be resolved to java.io.File objects. Use getInputStream() or copyTo(File) instead." );
	}

	@Override
	public boolean delete() {
		return getFileForLastModifiedCheck().delete();
	}

	@Override
	protected File getFileForLastModifiedCheck() {
		return super.getFile();
	}

	@Override
	public FileResource createRelative( String relativePath ) {
		throw new UnsupportedOperationException( "creating relative path is not yet supported" );
	}

	@Override
	public void copyFrom( @NonNull File originalFile, boolean deleteOriginal ) throws IOException {
		if ( !deleteOriginal ) {
			FileUtils.copyFile( originalFile, getFileForLastModifiedCheck() );
		}
		else {
			if ( originalFile == null || originalFile.isDirectory() ) {
				throw new IOException( "Original file '" + originalFile + "' is a directory" );
			}

			boolean fileMoved = true;

			try {
				Files.move( originalFile.toPath(), getFileForLastModifiedCheck().toPath(), ATOMIC_MOVE, REPLACE_EXISTING );
			}
			catch ( IOException ignore ) {
				fileMoved = false;
			}

			if ( !fileMoved ) {
				FileUtils.copyFile( originalFile, getFileForLastModifiedCheck() );
				FileUtils.deleteQuietly( originalFile );
			}
		}
	}
}
