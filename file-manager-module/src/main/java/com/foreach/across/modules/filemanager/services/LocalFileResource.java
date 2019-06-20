package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import com.foreach.across.modules.filemanager.business.FolderResource;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.PathResource;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Represents a single file on a {@link LocalFileRepository}.
 *
 * @author Arne Vandamme
 * @since 1.4.0
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
class LocalFileResource extends PathResource implements FileResource, FileResource.TargetFile
{
	@Getter
	private final FileDescriptor descriptor;

	private final Path file;

	LocalFileResource( @NonNull FileDescriptor descriptor, @NonNull Path file ) {
		super( file );
		this.descriptor = descriptor;
		this.file = file;
	}

	@Override
	public FolderResource getFolderResource() {
		return new LocalFolderResource( descriptor.getFolderDescriptor(), file.getParent() );
	}

	@Override
	public String getFilename() {
		return descriptor.getFileId();
	}

	@Override
	public URI getURI() {
		return descriptor.toResourceURI();
	}

	@Override
	public URL getURL() {
		throw new UnsupportedOperationException( "URL is not supported for a FileManagerModule FileResource" );
	}

	@Override
	public String getDescription() {
		return "axfs [" + descriptor.toString() + "] -> " + super.getDescription();
	}

	@Override
	public File getFile() {
		throw new UnsupportedOperationException( "FileResource can not be resolved to java.io.File objects. Use getInputStream() or copyTo(File) instead." );
	}

	@Override
	public boolean exists() {
		File targetFile = getTargetFile();
		return targetFile.exists() && !targetFile.isDirectory();
	}

	@Override
	public boolean delete() {
		File targetFile = getTargetFile();
		return !targetFile.isDirectory() && FileUtils.deleteQuietly( targetFile );
	}

	@Override
	protected File getFileForLastModifiedCheck() {
		return getTargetFile();
	}

	@Override
	public long lastModified() {
		File targetFile = getTargetFile();
		return !targetFile.isDirectory() ? targetFile.lastModified() : 0L;
	}

	@Override
	public long contentLength() {
		File targetFile = getTargetFile();
		return !targetFile.isDirectory() ? targetFile.length() : 0L;
	}

	@Override
	@SneakyThrows
	public File getTargetFile() {
		return super.getFile();
	}

	@Override
	public FileResource createRelative( String relativePath ) {
		throw new UnsupportedOperationException( "creating relative path is not yet supported" );
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		FileUtils.forceMkdirParent( getTargetFile() );
		return super.getOutputStream();
	}

	@Override
	public void copyFrom( @NonNull File originalFile, boolean deleteOriginal ) throws IOException {
		if ( !deleteOriginal ) {
			FileUtils.copyFile( originalFile, getTargetFile() );
		}
		else {
			if ( originalFile.isDirectory() ) {
				throw new IOException( "Original file '" + originalFile + "' is a directory" );
			}

			boolean fileMoved = true;

			try {
				Files.move( originalFile.toPath(), getTargetFile().toPath(), ATOMIC_MOVE, REPLACE_EXISTING );
			}
			catch ( IOException ignore ) {
				fileMoved = false;
			}

			if ( !fileMoved ) {
				FileUtils.copyFile( originalFile, getTargetFile() );
				FileUtils.deleteQuietly( originalFile );
			}
		}
	}

	@Override
	public boolean equals( Object obj ) {
		return obj == this || ( obj instanceof FileResource && descriptor.equals( ( (FileResource) obj ).getDescriptor() ) );
	}

	@Override
	public int hashCode() {
		return descriptor.hashCode();
	}
}
