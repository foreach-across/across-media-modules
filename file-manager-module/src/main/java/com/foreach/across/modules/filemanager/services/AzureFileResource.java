package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import com.foreach.across.modules.filemanager.business.FileStorageException;
import com.foreach.across.modules.filemanager.business.FolderResource;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class AzureFileResource implements FileResource
{
	private final FileDescriptor descriptor;
	private final CloudBlobClient blobClient;
	private final String containerName;
	private final String fileName;

	private final CloudBlockBlob blob;

	private volatile BlobProperties blobProperties;

	AzureFileResource( @NonNull FileDescriptor descriptor,
	                   @NonNull CloudBlobClient cloudBlobClient,
	                   @NonNull String containerName,
	                   @NonNull String fileName ) {
		this.descriptor = descriptor;
		this.blobClient = cloudBlobClient;
		this.containerName = containerName;
		this.fileName = fileName;
		try {
			CloudBlobContainer blobContainer = blobClient.getContainerReference( containerName );
			this.blob = blobContainer.getBlockBlobReference( fileName );
		}
		catch ( StorageException e ) {
			throw handleStorageException( e );
		}
		catch ( URISyntaxException e ) {
			throw new FileStorageException( e );
		}
	}

	@Override
	public FileDescriptor getDescriptor() {
		return descriptor;
	}

	public CloudBlobClient getBlobClient() {
		return blobClient;
	}

	public String getContainerName() {
		return containerName;
	}

	public String getFileName() {
		return fileName;
	}

	public CloudBlockBlob getBlob() {
		return blob;
	}

	@Override
	public FolderResource getFolderResource() {
		int ix = fileName.lastIndexOf( '/' );
		String folderObjectName = ix > 0 ? fileName.substring( 0, ix + 1 ) : "";
		return new AzureFolderResource( descriptor.getFolderDescriptor(), blobClient, containerName, folderObjectName );
	}

	@Override
	public boolean delete() {
		try {
			blob.delete( DeleteSnapshotsOption.INCLUDE_SNAPSHOTS, null, null, null );
			resetBlobProperties();
			return true;
		}
		catch ( StorageException e ) {
			//This means the resource was not found and therefor was deleted for sure
			return e.getHttpStatusCode() == 404;
		}
	}

	@Override
	public boolean exists() {
		try {
			return getBlobProperties() != null;
		}
		catch ( FileStorageException fse ) {
			if ( fse.getCause() instanceof FileNotFoundException ) {
				return false;
			}
			throw fse;
		}
	}

	@Override
	public FileResource createRelative( String relativePath ) {
		throw new UnsupportedOperationException( "creating relative path is not yet supported" );
	}

	@Override
	public OutputStream getOutputStream() {
		try {
			resetBlobProperties();
			return new LazyOutputStream( blob.openOutputStream() );
		}
		catch ( StorageException e ) {
			throw handleStorageException( e );
		}
	}

	@Override
	public URI getURI() {
		return descriptor.toResourceURI();
	}

	@Override
	public URL getURL() throws IOException {
		return descriptor.toResourceURI().toURL();
	}

	@Override
	public long contentLength() {
		return getBlobProperties().getLength();
	}

	@Override
	public long lastModified() {
		return getBlobProperties().getLastModified().toInstant().toEpochMilli();
	}

	@Override
	public String getFilename() {
		return descriptor.getFileId();
	}

	@Override
	public String getDescription() {
		return "axfs [" + descriptor.toString() + "] -> "
				+ String.format( "Azure storage blob resource[container='%s', blob='%s']", containerName, fileName );
	}

	@Override
	public InputStream getInputStream() {
		try {
			return blob.openInputStream();
		}
		catch ( StorageException e ) {
			throw handleStorageException( e );
		}
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		return o != null && ( o instanceof FileResource && descriptor.equals( ( (FileResource) o ).getDescriptor() ) );
	}

	@Override
	public int hashCode() {
		return descriptor.hashCode();
	}

	public void resetBlobProperties() {
		this.blobProperties = null;
	}

	public BlobProperties getBlobProperties() {
		if ( blobProperties == null ) {
			try {
				blob.downloadAttributes();
				this.blobProperties = blob.getProperties();
			}
			catch ( StorageException e ) {
				throw handleStorageException( e );
			}
		}
		return blobProperties;
	}

	/**
	 * CloudBlockBlob cannot be mocked because it's final, and a real CloudBlockBlob needs a real connection string,
	 * and will attempt to perform a real blob upload. This method allows you to work around that.
	 */
	public void uploadProperties() {
		try {
			blob.uploadProperties();
		}
		catch ( StorageException e ) {
			throw handleStorageException( e );
		}
	}

	@Override
	public String toString() {
		return getDescription();
	}

	private FileStorageException handleStorageException( StorageException e ) {
		if ( e.getHttpStatusCode() == 404 ) {
			FileNotFoundException exception = new FileNotFoundException( "File resource with descriptor [" + descriptor.toString() + "] not found!" );
			exception.initCause( e );
			return new FileStorageException( exception );
		}
		else {
			return new FileStorageException( e );
		}
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	private static class LazyOutputStream extends OutputStream
	{
		private final OutputStream fileOutputStream;
		private boolean wasClosed = false;

		@Override
		public void write( int b ) throws IOException {
			fileOutputStream.write( b );
		}

		@Override
		public void write( byte[] b ) throws IOException {
			fileOutputStream.write( b );
		}

		@Override
		public void write( byte[] b, int off, int len ) throws IOException {
			fileOutputStream.write( b, off, len );
		}

		@Override
		public void flush() throws IOException {
			if ( !wasClosed ) {
				fileOutputStream.flush();
			}
		}

		@Override
		public void close() throws IOException {
			if ( !wasClosed ) {
				fileOutputStream.close();
			}
			super.close();
			wasClosed = true;
		}
	}
}
