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
	private final FileDescriptor fileDescriptor;
	private final CloudBlobClient blobClient;
	private final String containerName;
	private final String fileName;

	private final CloudBlockBlob file;

	private volatile BlobProperties blobProperties;

	AzureFileResource( @NonNull FileDescriptor fileDescriptor,
	                   @NonNull CloudBlobClient cloudBlobClient,
	                   @NonNull String containerName,
	                   @NonNull String fileName ) {
		this.fileDescriptor = fileDescriptor;
		this.blobClient = cloudBlobClient;
		this.containerName = containerName;
		this.fileName = fileName;
		try {
			CloudBlobContainer blobContainer = blobClient.getContainerReference( containerName );
			this.file = blobContainer.getBlockBlobReference( fileName );
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
		return fileDescriptor;
	}

	@Override
	public FolderResource getFolderResource() {
		int ix = fileName.lastIndexOf( '/' );
		String folderObjectName = ix > 0 ? fileName.substring( 0, ix + 1 ) : "";
		return new AzureFolderResource( fileDescriptor.getFolderDescriptor(), blobClient, containerName, folderObjectName );
	}

	@Override
	public boolean delete() {
		try {
			file.delete( DeleteSnapshotsOption.INCLUDE_SNAPSHOTS, null, null, null );
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
			return new LazyOutputStream( file.openOutputStream(), file );
		}
		catch ( StorageException e ) {
			throw handleStorageException( e );
		}
	}

	@Override
	public URI getURI() {
		return fileDescriptor.toResourceURI();
	}

	@Override
	public URL getURL() throws IOException {
		return fileDescriptor.toResourceURI().toURL();
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
		return fileDescriptor.getFileId();
	}

	@Override
	public String getDescription() {
		return "axfs [" + fileDescriptor.toString() + "] -> "
				+ String.format( "Azure storage blob resource[container='%s', blob='%s']", containerName, fileName );
	}

	@Override
	public InputStream getInputStream() {
		try {
			return file.openInputStream();
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
		return o != null && ( o instanceof FileResource && fileDescriptor.equals( ( (FileResource) o ).getDescriptor() ) );
	}

	@Override
	public int hashCode() {
		return fileDescriptor.hashCode();
	}

	void resetBlobProperties() {
		this.blobProperties = null;
	}

	private BlobProperties getBlobProperties() {
		if ( blobProperties == null ) {
			try {
				file.downloadAttributes();
				this.blobProperties = file.getProperties();
			}
			catch ( StorageException e ) {
				throw handleStorageException( e );
			}
		}
		return blobProperties;
	}

	@Override
	public String toString() {
		return getDescription();
	}

	private FileStorageException handleStorageException( StorageException e ) {
		if ( e.getHttpStatusCode() == 404 ) {
			FileNotFoundException exception = new FileNotFoundException( "File resource with descriptor [" + fileDescriptor.toString() + "] not found!" );
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
		private final CloudBlockBlob file;
		private boolean wasWrittenTo = false;
		private boolean wasClosed = false;

		@Override
		public void write( int b ) throws IOException {
			fileOutputStream.write( b );
			wasWrittenTo = true;
		}

		@Override
		public void flush() throws IOException {
			if ( wasWrittenTo && !wasClosed ) {
				fileOutputStream.flush();
			}
		}

		@Override
		public void close() throws IOException {
			if ( wasWrittenTo && !wasClosed ) {
				fileOutputStream.close();
			}
			else {
				// Empty zero-byte file
				try {
					file.uploadText( "" );
				}
				catch ( StorageException ignore ) {

				}
			}
			super.close();
			wasClosed = true;
		}
	}
}
