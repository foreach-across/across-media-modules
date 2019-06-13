package com.foreach.across.modules.filemanager.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import com.foreach.across.modules.filemanager.business.FolderResource;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.io.FileUtils;
import org.springframework.cloud.aws.core.io.s3.SimpleStorageResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.util.ReflectionUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;

/**
 * Represents an Amazon S3 file object. Extends the {@code SimpleStorageResource} from Spring Cloud AWS
 * and adapts it to the {@link FileResource} semantics.
 *
 * @author Arne Vandamme
 * @since 1.4.0
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
class AmazonS3FileResource extends SimpleStorageResource implements FileResource
{
	private static final Field metadataField;

	static {
		// required for resetting object metadata
		metadataField = ReflectionUtils.findField( SimpleStorageResource.class, "objectMetadata" );
		ReflectionUtils.makeAccessible( metadataField );
	}

	@Getter
	private final FileDescriptor descriptor;

	private final AmazonS3 amazonS3;
	private final String bucketName;
	private final String objectName;
	private final TaskExecutor taskExecutor;

	AmazonS3FileResource( @NonNull FileDescriptor fileDescriptor,
	                      @NonNull AmazonS3 amazonS3,
	                      @NonNull String bucketName,
	                      @NonNull String objectName,
	                      @NonNull TaskExecutor taskExecutor ) {
		super( amazonS3, bucketName, objectName, taskExecutor );
		this.descriptor = fileDescriptor;
		this.amazonS3 = amazonS3;
		this.bucketName = bucketName;
		this.objectName = objectName;
		this.taskExecutor = taskExecutor;
	}

	@Override
	public FolderResource getFolderResource() {
		int ix = objectName.lastIndexOf( '/' );
		String folderObjectName = ix > 0 ? objectName.substring( 0, ix + 1 ) : "";
		return new AmazonS3FolderResource( descriptor.getFolderDescriptor(), amazonS3, bucketName, folderObjectName, taskExecutor );
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
	public long contentLength() throws IOException {
		try {
			return super.contentLength();
		}
		catch ( FileNotFoundException fnfe ) {
			throw fileNotFound( descriptor, fnfe );
		}
	}

	@Override
	public long lastModified() throws IOException {
		try {
			return super.lastModified();
		}
		catch ( FileNotFoundException fnfe ) {
			throw fileNotFound( descriptor, fnfe );
		}
	}

	@Override
	public boolean delete() {
		amazonS3.deleteObject( bucketName, objectName );
		resetObjectMetadata();
		return true;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		// reset metadata - assume data will actually be written to the underlying resource
		resetObjectMetadata();
		return super.getOutputStream();
	}

	@Override
	public File getFile() {
		throw new UnsupportedOperationException( "FileResource can not be resolved to java.io.File objects. Use getInputStream() or copyTo(File) instead." );
	}

	void resetObjectMetadata() {
		if ( metadataField != null ) {
			ReflectionUtils.setField( metadataField, this, null );
		}
	}

	void loadMetadata( S3ObjectSummary summary ) {
		if ( metadataField != null ) {
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength( summary.getSize() );
			metadata.setLastModified( summary.getLastModified() );
			ReflectionUtils.setField( metadataField, this, metadata );
		}
	}

	@Override
	public AmazonS3FileResource createRelative( String relativePath ) {
		throw new UnsupportedOperationException( "creating relative path is not yet supported" );
	}

	@Override
	public void copyTo( File file ) throws IOException {
		try {
			FileUtils.forceMkdirParent( file );
			amazonS3.getObject( new GetObjectRequest( bucketName, objectName ), file );
		}
		catch ( AmazonS3Exception s3e ) {
			if ( s3e.getStatusCode() == 404 ) {
				throw fileNotFound( descriptor, s3e );
			}
			else {
				throw s3e;
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

	private FileNotFoundException fileNotFound( FileDescriptor fileDescriptor, Throwable cause ) {
		FileNotFoundException exception = new FileNotFoundException( "File resource with descriptor [" + fileDescriptor.toString() + "] not found!" );
		exception.initCause( cause );
		return exception;
	}
}
