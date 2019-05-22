package com.foreach.across.modules.filemanager.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
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
	private final FileDescriptor fileDescriptor;

	private final AmazonS3 amazonS3;
	private final String bucketName;
	private final String objectName;

	AmazonS3FileResource( @NonNull FileDescriptor fileDescriptor,
	                      @NonNull AmazonS3 amazonS3,
	                      @NonNull String bucketName,
	                      @NonNull String objectName,
	                      @NonNull TaskExecutor taskExecutor ) {
		super( amazonS3, bucketName, objectName, taskExecutor );
		this.fileDescriptor = fileDescriptor;
		this.amazonS3 = amazonS3;
		this.bucketName = bucketName;
		this.objectName = objectName;
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
	public long contentLength() throws IOException {
		try {
			return super.contentLength();
		}
		catch ( FileNotFoundException fnfe ) {
			throw fileNotFound( fileDescriptor, fnfe );
		}
	}

	@Override
	public long lastModified() throws IOException {
		try {
			return super.lastModified();
		}
		catch ( FileNotFoundException fnfe ) {
			throw fileNotFound( fileDescriptor, fnfe );
		}
	}

	@Override
	public boolean delete() {
		amazonS3.deleteObject( bucketName, objectName );
		resetObjectMetadataAndTempFile();
		return true;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		// reset metadata - assume data will actually be written to the underlying resource
		resetObjectMetadataAndTempFile();
		return super.getOutputStream();
	}

	@Override
	public File getFile() {
		throw new UnsupportedOperationException( "FileResource can not be resolved to java.io.File objects. Use getInputStream() or copyTo(File) instead." );
	}

	private void resetObjectMetadataAndTempFile() {
		if ( metadataField != null ) {
			ReflectionUtils.setField( metadataField, this, null );
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
				throw fileNotFound( fileDescriptor, s3e );
			}
			else {
				throw s3e;
			}
		}
	}

	@Override
	public boolean equals( Object obj ) {
		return obj == this || ( obj instanceof FileResource && fileDescriptor.equals( ( (FileResource) obj ).getFileDescriptor() ) );
	}

	@Override
	public int hashCode() {
		return fileDescriptor.hashCode();
	}

	private FileNotFoundException fileNotFound( FileDescriptor fileDescriptor, Throwable cause ) {
		FileNotFoundException exception = new FileNotFoundException( "File resource with descriptor [" + fileDescriptor.toString() + "] not found!" );
		exception.initCause( cause );
		return exception;
	}
}
