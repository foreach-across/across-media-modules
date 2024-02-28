package com.foreach.across.modules.filemanager.services;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobStorageException;
import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import com.foreach.across.modules.filemanager.business.FileStorageException;
import com.foreach.across.modules.filemanager.business.FolderResource;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;

@Getter
public class AzureFileResource implements FileResource
{
	public static final int NOT_FOUND = HttpStatus.NOT_FOUND.value();
	private final FileDescriptor descriptor;
	private final BlobServiceClient blobServiceClient;
	private final String containerName;
	private final String fileName;

	private final BlobClient blobClient;

	private volatile BlobProperties blobProperties;

	AzureFileResource( @NonNull FileDescriptor descriptor,
	                   @NonNull BlobServiceClient blobServiceClient,
	                   @NonNull String containerName,
	                   @NonNull String fileName ) {
		this.descriptor = descriptor;
		this.blobServiceClient = blobServiceClient;
		this.containerName = containerName;
		this.fileName = fileName;
		try {
			this.blobClient = blobServiceClient
					.getBlobContainerClient( containerName )
					.getBlobClient( fileName );
		}
		catch ( BlobStorageException e ) {
			throw handleStorageException( e );
		}
	}

	@Override
	public FileDescriptor getDescriptor() {
		return descriptor;
	}

	@Override
	public FolderResource getFolderResource() {
		int ix = fileName.lastIndexOf( '/' );
		String folderObjectName = ix > 0 ? fileName.substring( 0, ix + 1 ) : "";
		return new AzureFolderResource( descriptor.getFolderDescriptor(), blobServiceClient, containerName, folderObjectName );
	}

	@Override
	public boolean delete() {
		try {
			blobClient.delete();
			resetBlobProperties();
			return true;
		}
		catch ( BlobStorageException e ) {
			if ( e.getStatusCode() == NOT_FOUND ) {
				return true;
			}
			throw new RuntimeException( e );
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
			return new LazyOutputStream( blobClient.getBlockBlobClient().getBlobOutputStream( true ) );
		}
		catch ( BlobStorageException e ) {
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
		return getBlobProperties().getBlobSize();
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
			return blobClient.openInputStream();
		}
		catch ( BlobStorageException e ) {
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
				this.blobProperties = blobClient.getProperties();
			}
			catch ( BlobStorageException e ) {
				throw handleStorageException( e );
			}
		}
		return blobProperties;
	}

	@Override
	public String toString() {
		return getDescription();
	}

	private FileStorageException handleStorageException( BlobStorageException e ) {
		if ( e.getStatusCode() == NOT_FOUND ) {
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
