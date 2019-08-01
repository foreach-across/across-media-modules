package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import com.foreach.across.modules.filemanager.business.FolderResource;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.output.TeeOutputStream;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;

/**
 * Represents a caching wrapper around a target {@link FileResource}.
 * Takes two file resources as parameter. One is the actual target, the
 * other contains the cached data. If the latter exists it will be used
 * instead of the target for access.
 * <p/>
 * Note that the {@link FileResource#exists()} will be called often on the
 * {@code cache} resource, it can be any type of file resource, but you
 * usually want it to come from a {@link LocalFileRepository}.
 * <p/>
 * The cache is primarily used for the actual data streams, other methods
 * might forward directly to the target resource to ensure maximum consistency.
 *
 * @author Arne Vandamme
 * @since 1.4.0
 */
@RequiredArgsConstructor
public class CachedFileResource implements ExpiringFileResource
{
	/**
	 * The actual target file resource which has a cache.
	 */
	@NonNull
	@Getter
	private final FileResource target;

	/**
	 * The underlying file resource which represents the local cache.
	 */
	@NonNull
	@Getter
	private final FileResource cache;

	private final CachingFileRepository cachingFileRepository;

	/**
	 * Timestamp when the input stream was last accessed.
	 * Can be used to determine if the cached resource should be flushed.
	 */
	@Getter
	private long lastAccessTime = System.currentTimeMillis();

	@Override
	public FileDescriptor getDescriptor() {
		return target.getDescriptor();
	}

	@Override
	public FolderResource getFolderResource() {
		return cachingFileRepository.createExpiringFolderResource( target.getFolderResource() );
	}

	@Override
	public boolean delete() {
		flushCache();
		return target.delete();
	}

	@Override
	public FileResource createRelative( String relativePath ) {
		throw new UnsupportedOperationException( "creating relative path is not yet supported" );
	}

	@Override
	public boolean isWritable() {
		return target.isWritable();
	}

	@Override
	public boolean exists() {
		lastAccessTime = System.currentTimeMillis();

		return cache.exists() || target.exists();
	}

	@Override
	public boolean isReadable() {
		return target.isReadable();
	}

	@Override
	public boolean isOpen() {
		return target.isOpen();
	}

	@Override
	public URL getURL() throws IOException {
		return target.getURL();
	}

	@Override
	public URI getURI() {
		return target.getURI();
	}

	@Override
	public long contentLength() throws IOException {
		lastAccessTime = System.currentTimeMillis();
		return cache.exists() ? cache.contentLength() : target.contentLength();
	}

	@Override
	public long lastModified() throws IOException {
		lastAccessTime = System.currentTimeMillis();
		return target.lastModified();
	}

	@Override
	public String getFilename() {
		return target.getFilename();
	}

	@Override
	public String getDescription() {
		return "axfs cached resource (" + cache.getDescription() + " : " + target.getDescription() + ")";
	}

	@Override
	public InputStream getInputStream() throws IOException {
		lastAccessTime = System.currentTimeMillis();
		if ( !cache.exists() ) {
			synchronized ( this ) {
				if ( !cache.exists() ) {
					target.copyTo( cache );
				}
			}
		}
		return cache.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		lastAccessTime = System.currentTimeMillis();
		return new TeeOutputStream( target.getOutputStream(), cache.getOutputStream() );
	}

	@SuppressWarnings("WeakerAccess")
	public boolean flushCache() {
		try {
			if ( cache.exists() ) {
				return cache.delete();
			}
		}
		catch ( Exception ignore ) {
			// ignore exceptions on flushing cache, simply return false
		}
		return false;
	}

	@Override
	public void copyFrom( File originalFile, boolean deleteOriginal ) throws IOException {
		try {
			ExpiringFileResource.super.copyFrom( originalFile, deleteOriginal );
		}
		catch ( IOException ioe ) {
			flushCache();
			throw ioe;
		}
	}

	@Override
	public void copyFrom( FileResource originalFileResource, boolean deleteOriginal ) throws IOException {
		try {
			ExpiringFileResource.super.copyFrom( originalFileResource, deleteOriginal );
		}
		catch ( IOException ioe ) {
			flushCache();
			throw ioe;
		}
	}

	@Override
	public void copyFrom( Resource resource ) throws IOException {
		try {
			ExpiringFileResource.super.copyFrom( resource );
		}
		catch ( IOException ioe ) {
			flushCache();
			throw ioe;
		}
	}

	@Override
	public void copyFrom( InputStream inputStream ) throws IOException {
		try {
			ExpiringFileResource.super.copyFrom( inputStream );
		}
		catch ( IOException ioe ) {
			flushCache();
			throw ioe;
		}
	}

	/**
	 * @return timestamp when cache item was created (or 0 if there is no cache item)
	 */
	@Override
	public long getCreationTime() {
		try {
			return cache.exists() ? cache.lastModified() : 0;
		}
		catch ( IOException ignore ) {
			return 0;
		}
	}

	@Override
	public boolean equals( Object obj ) {
		return obj == this || ( obj instanceof FileResource && target.equals( obj ) );
	}

	@Override
	public int hashCode() {
		return target.hashCode();
	}
}
