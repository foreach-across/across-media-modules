package com.foreach.across.modules.filemanager.services;

import com.amazonaws.services.s3.AmazonS3;
import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.util.StreamUtils;
import utils.AmazonS3Helper;

import java.io.*;
import java.nio.charset.Charset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 * @since 1.4.0
 */
class TestAmazonS3FileResource
{
	private static final Resource RES_TEXTFILE = new ClassPathResource( "textfile.txt" );
	private static final String BUCKET_NAME = "ax-filemanager-test";

	private static AmazonS3 amazonS3 = AmazonS3Helper.createClientWithBuckets( BUCKET_NAME );

	private FileDescriptor descriptor;
	private FileResource resource;
	private String objectName;

	@BeforeEach
	@SneakyThrows
	void createResource() {
		objectName = UUID.randomUUID().toString();
		descriptor = FileDescriptor.of( "my-repo", "123/456", objectName );
		resource = new AmazonS3FileResource( descriptor, amazonS3, BUCKET_NAME, objectName, new SyncTaskExecutor() );
	}

	@AfterAll
	static void tearDown() {
		try {
			AmazonS3Helper.deleteBuckets( amazonS3, BUCKET_NAME );
		} finally {
			amazonS3.shutdown();
		}
		amazonS3 = null;
	}

	@Test
	void equals() {
		assertThat( resource )
				.isEqualTo( resource )
				.isNotEqualTo( mock( Resource.class ) )
				.isEqualTo( new AmazonS3FileResource( resource.getDescriptor(), amazonS3, "other", "objectName", new SyncTaskExecutor() ) )
				.isNotEqualTo( new AmazonS3FileResource( FileDescriptor.of( "1:2:3" ), amazonS3, BUCKET_NAME, objectName, new SyncTaskExecutor() ) );
	}

	@Test
	void folderResource() {
		assertThat( resource.getFolderResource() ).isNotNull();
		assertThat( resource.getFolderResource().getDescriptor() ).isEqualTo( resource.getDescriptor().getFolderDescriptor() );

		assertThat( resource.getFolderResource().listFiles() ).doesNotContain( resource );
		amazonS3.putObject( BUCKET_NAME, objectName, "some-data" );
		assertThat( resource.getFolderResource().listFiles() ).contains( resource );
	}

	@Test
	void fileDescriptor() {
		assertThat( resource.getDescriptor() ).isEqualTo( descriptor );
	}

	@Test
	@SneakyThrows
	void uri() {
		assertThat( resource.getURI() ).isEqualTo( descriptor.toResourceURI() );
	}

	@Test
	void urlIsNotSupported() {
		assertThatExceptionOfType( UnsupportedOperationException.class ).isThrownBy( () -> resource.getURL() );
	}

	@Test
	void description() {
		assertThat( resource.getDescription() )
				.startsWith( "axfs [" + descriptor.toString() + "] -> Amazon s3 resource" )
				.isEqualTo( resource.toString() );
	}

	@Test
	void exists() {
		assertThat( resource.exists() ).isFalse();
		amazonS3.putObject( BUCKET_NAME, objectName, "some-data" );
		assertThat( resource.exists() ).isTrue();
	}

	@Test
	void getFile() {
		assertThatExceptionOfType( UnsupportedOperationException.class ).isThrownBy( () -> resource.getFile() );
	}

	@Test
	void isReadable() {
		amazonS3.putObject( BUCKET_NAME, objectName, "some-data" );
		assertThat( resource.isReadable() ).isTrue();
	}

	@Test
	void isWritable() {
		assertThat( resource.isWritable() ).isTrue();
	}

	@Test
	void isOpen() {
		assertThat( resource.isOpen() ).isFalse();
	}

	@Test
	void delete() {
		amazonS3.putObject( BUCKET_NAME, objectName, "some-data" );
		assertThat( resource.exists() ).isTrue();
		assertThat( resource.delete() ).isTrue();
		assertThat( resource.exists() ).isFalse();

		// delete always returns true
		assertThat( resource.delete() ).isTrue();
	}

	@Test
	void filename() {
		assertThat( resource.getFilename() ).isEqualTo( descriptor.getFileId() );
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Test
	@SneakyThrows
	void contentLengthAndLastModified() {
		assertThatExceptionOfType( FileNotFoundException.class )
				.isThrownBy( () -> resource.contentLength() )
				.withMessageContaining( resource.getDescriptor().toString() );

		try (InputStream is = RES_TEXTFILE.getInputStream()) {
			try (OutputStream os = resource.getOutputStream()) {
				IOUtils.copy( is, os );
			}
		}

		assertThat( resource.contentLength() )
				.isEqualTo( RES_TEXTFILE.contentLength() )
				.matches( l -> l > 0 );
		assertThat( resource.lastModified() )
				.matches( l -> l > 0 );

		File otherTempFile = File.createTempFile( UUID.randomUUID().toString(), ".txt" );
		try (InputStream is = resource.getInputStream()) {
			try (OutputStream os = new FileOutputStream( otherTempFile )) {
				IOUtils.copy( is, os );
			}
		}

		long length = otherTempFile.length();
		otherTempFile.delete();
		assertThat( length ).isEqualTo( resource.contentLength() );
	}

	@Test
	void createRelative() {
		assertThatExceptionOfType( UnsupportedOperationException.class ).isThrownBy( () -> resource.createRelative( "relative" ) );
	}

	@Test
	@SneakyThrows
	void outputStreamResetsMetadata() {
		assertThat( resource.exists() ).isFalse();
		amazonS3.putObject( BUCKET_NAME, objectName, "some-data" );
		assertThat( resource.exists() ).isTrue();
		assertThat( resource.contentLength() ).isEqualTo( 9 );

		try (InputStream is = RES_TEXTFILE.getInputStream()) {
			try (OutputStream os = resource.getOutputStream()) {
				IOUtils.copy( is, os );
			}
		}

		assertThat( resource.exists() ).isTrue();
		assertThat( resource.contentLength() ).isNotEqualTo( 9 ).isEqualTo( RES_TEXTFILE.contentLength() );

		assertThat( amazonS3.getObjectAsString( BUCKET_NAME, objectName ) )
				.isEqualTo( "some dummy text" );
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Test
	@SneakyThrows
	void copyFromFile() {
		File otherTempFile = File.createTempFile( UUID.randomUUID().toString(), ".txt" );
		FileUtils.writeStringToFile( otherTempFile, "hello file", "UTF-8" );

		resource.copyFrom( otherTempFile, false );
		otherTempFile.delete();

		assertThat( resourceData() ).isEqualTo( "hello file" );

		FileUtils.writeStringToFile( otherTempFile, "hello file 2", "UTF-8" );
		resource.copyFrom( otherTempFile, true );

		assertThat( otherTempFile.exists() ).isFalse();
		assertThat( resourceData() ).isEqualTo( "hello file 2" );
	}

	@Test
	@SneakyThrows
	void copyFromInputStream() {
		try (InputStream is = RES_TEXTFILE.getInputStream()) {
			resource.copyFrom( is );
		}

		try (InputStream is = resource.getInputStream()) {
			assertThat( StreamUtils.copyToString( is, Charset.defaultCharset() ) ).isEqualTo( "some dummy text" );
		}
	}

	@Test
	@SneakyThrows
	void copyFromResource() {
		resource.copyFrom( RES_TEXTFILE );
		try (InputStream is = resource.getInputStream()) {
			assertThat( StreamUtils.copyToString( is, Charset.defaultCharset() ) ).isEqualTo( "some dummy text" );
		}
	}

	@Test
	@SneakyThrows
	void copyFromFileResource() {
		File otherTempFile = File.createTempFile( UUID.randomUUID().toString(), ".txt" );
		FileResource other = new LocalFileResource( descriptor, otherTempFile.toPath() );
		other.copyFrom( RES_TEXTFILE );

		resource.copyFrom( other, false );
		try (InputStream is = resource.getInputStream()) {
			assertThat( StreamUtils.copyToString( is, Charset.defaultCharset() ) ).isEqualTo( "some dummy text" );
		}

		assertThat( other.exists() ).isTrue();
		FileUtils.writeStringToFile( otherTempFile, "hello file", "UTF-8" );

		resource.copyFrom( other, true );
		try (InputStream is = resource.getInputStream()) {
			assertThat( StreamUtils.copyToString( is, Charset.defaultCharset() ) ).isEqualTo( "hello file" );
		}
		assertThat( other.exists() ).isFalse();
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Test
	@SneakyThrows
	void copyToFileResource() {
		amazonS3.putObject( BUCKET_NAME, objectName, "some-data" );

		File otherTempFile = File.createTempFile( UUID.randomUUID().toString(), ".txt" );
		FileResource other = new LocalFileResource( descriptor, otherTempFile.toPath() );
		resource.copyTo( other );

		otherTempFile.deleteOnExit();
		assertThat( FileUtils.readFileToString( otherTempFile, Charset.defaultCharset() ) ).isEqualTo( "some-data" );
		otherTempFile.delete();
	}

	@Test
	@SneakyThrows
	void noFileCreatedIfExceptionOnInputStream() {
		assertThat( amazonS3.doesObjectExist( BUCKET_NAME, objectName ) ).isFalse();

		InputStream inputStream = mock( InputStream.class );
		when( inputStream.available() ).thenThrow( new IOException() );

		assertThatExceptionOfType( IOException.class )
				.isThrownBy( () -> resource.copyFrom( inputStream ) );
		assertThat( amazonS3.doesObjectExist( BUCKET_NAME, objectName ) ).isFalse();
	}

	@Test
	@SneakyThrows
	void noFileCreatedIfExceptionOnOtherFileResource() {
		assertThat( amazonS3.doesObjectExist( BUCKET_NAME, objectName ) ).isFalse();

		FileResource other = mock( FileResource.class );
		when( other.getInputStream() ).thenThrow( new IOException() );

		assertThatExceptionOfType( IOException.class )
				.isThrownBy( () -> resource.copyFrom( other ) );
		assertThat( amazonS3.doesObjectExist( BUCKET_NAME, objectName ) ).isFalse();
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Test
	@SneakyThrows
	void copyToFile() {
		File otherTempFile = File.createTempFile( UUID.randomUUID().toString(), ".txt" );
		resource.copyFrom( RES_TEXTFILE );
		resource.copyTo( otherTempFile );
		otherTempFile.deleteOnExit();
		assertThat( resourceData() ).isEqualTo( "some dummy text" );
		otherTempFile.delete();
	}

	@Test
	@SneakyThrows
	void fileIsNeverWrittenIfExceptionOnInputStream() {
		File otherTempFile = File.createTempFile( UUID.randomUUID().toString(), ".txt" );
		assertThat( otherTempFile.delete() ).isTrue();

		assertThatExceptionOfType( IOException.class )
				.isThrownBy( () -> resource.copyTo( otherTempFile ) );
		assertThat( otherTempFile.exists() ).isFalse();
	}

	@Test
	@SneakyThrows
	void copyToOutputStream() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream( 1024 );
		resource.copyFrom( RES_TEXTFILE );
		resource.copyTo( bos );
		assertThat( new String( bos.toByteArray(), Charset.defaultCharset() ) ).isEqualTo( "some dummy text" );
	}

	@SneakyThrows
	private String resourceData() {
		try (InputStream is = resource.getInputStream()) {
			return StreamUtils.copyToString( is, Charset.defaultCharset() );
		}
	}
}
