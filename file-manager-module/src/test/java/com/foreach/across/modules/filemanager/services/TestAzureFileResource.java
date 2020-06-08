package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import com.foreach.across.modules.filemanager.business.FileStorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.StreamUtils;
import utils.AzureStorageHelper;

import java.io.*;
import java.nio.charset.Charset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class TestAzureFileResource
{
	private static final Resource RES_TEXTFILE = new ClassPathResource( "textfile.txt" );
	private static final String CONTAINER_NAME = "ax-filemanager-test";

	private CloudBlobClient cloudBlobClient;
	private FileDescriptor descriptor;
	private FileResource resource;
	private String objectName;

	@BeforeEach
	@SneakyThrows
	void createResource() {
		if ( cloudBlobClient == null ) {
			cloudBlobClient = AzureStorageHelper.azurite.storageAccount().createCloudBlobClient();
			cloudBlobClient.getContainerReference( CONTAINER_NAME ).createIfNotExists();
		}
		objectName = UUID.randomUUID().toString();
		descriptor = FileDescriptor.of( "my-repo", "123/456", objectName );
		resource = new AzureFileResource( descriptor, cloudBlobClient, CONTAINER_NAME, objectName );
	}

	@AfterEach
	@SneakyThrows
	void destroyResource() {
		cloudBlobClient.getContainerReference( CONTAINER_NAME ).deleteIfExists();
	}

	@Test
	@SneakyThrows
	void equals() {
		Resource actual = new AzureFileResource( descriptor, cloudBlobClient, CONTAINER_NAME, objectName );
		assertThat( resource )
				.isEqualTo( resource )
				.isEqualTo( actual );
	}

	@Test
	@SneakyThrows
	void folderResource() {
		assertThat( resource.getFolderResource() ).isNotNull();
		assertThat( resource.getFolderResource().getDescriptor() ).isEqualTo( resource.getDescriptor().getFolderDescriptor() );

		assertThat( resource.getFolderResource().listFiles() ).doesNotContain( resource );
		cloudBlobClient.getContainerReference( CONTAINER_NAME ).getBlockBlobReference( objectName ).uploadText( "some-data" );
		assertThat( resource.getFolderResource().listFiles() ).contains( resource );
	}

	@Test
	void fileDescriptor() {
		assertThat( resource.getDescriptor() ).isEqualTo( descriptor );
	}

	@Test
	void uri() {
		assertThat( resource.getURI() ).isEqualTo( descriptor.toResourceURI() );
	}

	/*
	this test appears in the TestAmazonS3FileResource, but getUrl is a supported operation on AzureFileResource
	@Test
	void urlIsNotSupported() {
		assertThatExceptionOfType( UnsupportedOperationException.class ).isThrownBy( () -> resource.getURL() );
	}*/

	@Test
	void description() {
		assertThat( resource.getDescription() )
				.startsWith( "axfs [" + descriptor.toString() + "] -> Azure storage blob resource" )
				.isEqualTo( resource.toString() );
	}

	@Test
	@SneakyThrows
	void exists() {
		assertThat( resource.exists() ).isFalse();
		cloudBlobClient.getContainerReference( CONTAINER_NAME ).getBlockBlobReference( objectName ).uploadText( "some-data" );
		assertThat( resource.exists() ).isTrue();
	}

	@Test
	void getFile() {
		assertThatExceptionOfType( UnsupportedOperationException.class ).isThrownBy( () -> resource.getFile() );
	}

	@Test
	@SneakyThrows
	void isReadable() {
		cloudBlobClient.getContainerReference( CONTAINER_NAME ).getBlockBlobReference( objectName ).uploadText( "some-data" );
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
	@SneakyThrows
	void delete() {
		cloudBlobClient.getContainerReference( CONTAINER_NAME ).getBlockBlobReference( objectName ).uploadText( "some-data" );
		assertThat( resource.exists() ).isTrue();
		assertThat( resource.delete() ).isTrue();
		assertThat( resource.exists() ).isFalse();
		assertThat( resource.delete() ).isTrue();
	}

	@Test
	void fileName() {
		assertThat( resource.getFilename() ).isEqualTo( descriptor.getFileId() );
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Test
	@SneakyThrows
	void contentLengthAndLastModified() {
		assertThatExceptionOfType( FileStorageException.class )
				.isThrownBy( () -> resource.contentLength() )
				.withCauseInstanceOf( FileNotFoundException.class )
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
		cloudBlobClient.getContainerReference( CONTAINER_NAME ).getBlockBlobReference( objectName ).uploadText( "some-data" );
		assertThat( resource.exists() ).isTrue();
		assertThat( resource.contentLength() ).isEqualTo( 9 );

		try (InputStream is = RES_TEXTFILE.getInputStream()) {
			try (OutputStream os = resource.getOutputStream()) {
				IOUtils.copy( is, os );
			}
		}

		assertThat( resource.exists() ).isTrue();
		assertThat( resource.contentLength() ).isNotEqualTo( 9 ).isEqualTo( RES_TEXTFILE.contentLength() );
		assertThat( cloudBlobClient.getContainerReference( CONTAINER_NAME ).getBlockBlobReference( objectName ).downloadText() ).isEqualTo( "some dummy text" );
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
		cloudBlobClient.getContainerReference( CONTAINER_NAME ).getBlockBlobReference( objectName ).uploadText( "some-data" );

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
		assertThat( cloudBlobClient.getContainerReference( CONTAINER_NAME ).getBlockBlobReference( objectName ).exists() ).isFalse();

		InputStream inputStream = mock( InputStream.class );
		when( inputStream.available() ).thenThrow( new IOException() );

		assertThatExceptionOfType( IOException.class )
				.isThrownBy( () -> resource.copyFrom( inputStream ) );
		assertThat( cloudBlobClient.getContainerReference( CONTAINER_NAME ).getBlockBlobReference( objectName ).exists() ).isFalse();
	}

	@Test
	@SneakyThrows
	void noFileCreatedIfExceptionOnOtherFileResource() {
		assertThat( cloudBlobClient.getContainerReference( CONTAINER_NAME ).getBlockBlobReference( objectName ).exists() ).isFalse();

		FileResource other = mock( FileResource.class );
		when( other.getInputStream() ).thenThrow( new IOException() );

		assertThatExceptionOfType( IOException.class )
				.isThrownBy( () -> resource.copyFrom( other ) );
		assertThat( cloudBlobClient.getContainerReference( CONTAINER_NAME ).getBlockBlobReference( objectName ).exists() ).isFalse();
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

		assertThatExceptionOfType( FileStorageException.class )
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

	@Test
	@SneakyThrows
	void writingToNonExistentFile() {
		assertThat( resource.exists() ).isFalse();

		try (InputStream is = RES_TEXTFILE.getInputStream()) {
			try (OutputStream os = resource.getOutputStream()) {
				IOUtils.copy( is, os );
			}
		}

		assertThat( resource.exists() ).isTrue();
		assertThat( resource.contentLength() ).isNotEqualTo( 9 ).isEqualTo( RES_TEXTFILE.contentLength() );

		assertThat( cloudBlobClient.getContainerReference( CONTAINER_NAME ).getBlockBlobReference( objectName ).downloadText() )
				.isEqualTo( "some dummy text" );
	}

	@Test
	@SneakyThrows
	void writingNothingToNonExistentFile() {
		assertThat( resource.exists() ).isFalse();
		try (OutputStream os = resource.getOutputStream()) {
			os.write( 0 );
			os.flush();
		}
		assertThat( resource.exists() ).isTrue();
		assertThat( resource.contentLength() ).isEqualTo( 1 );
		assertThat( cloudBlobClient.getContainerReference( CONTAINER_NAME ).getBlockBlobReference( objectName ).downloadText() ).isEqualTo( "\0" );
	}
}
