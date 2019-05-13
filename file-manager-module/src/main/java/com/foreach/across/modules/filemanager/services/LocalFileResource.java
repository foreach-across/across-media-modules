package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import lombok.NonNull;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.net.URI;
import java.net.URL;

/**
 * Represents a single file on a {@link LocalFileRepository}.
 *
 * @author Arne Vandamme
 * @since 1.4.0
 */
class LocalFileResource extends FileSystemResource implements FileResource
{
	private final FileDescriptor descriptor;

	LocalFileResource( @NonNull FileDescriptor descriptor, @NonNull File file ) {
		super( file );
		this.descriptor = descriptor;
	}

	@Override
	public FileDescriptor getFileDescriptor() {
		return descriptor;
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
	public boolean delete() {
		return getFile().delete();
	}

	@Override
	public FileResource createRelative( String relativePath ) {
		throw new UnsupportedOperationException( "creating relative path is not yet supported" );
	}
}
