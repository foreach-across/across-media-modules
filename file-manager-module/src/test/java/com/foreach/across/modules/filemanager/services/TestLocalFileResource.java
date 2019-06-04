package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

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
class TestLocalFileResource
{
	private static final Resource RES_TEXTFILE = new ClassPathResource( "textfile.txt" );

	private File tempFile;
	private File nonExistingFile;
	private FileDescriptor descriptor;
	private FileResource resource;

	@BeforeEach
	@SneakyThrows
	void createResource() {
		tempFile = File.createTempFile( UUID.randomUUID().toString(), ".txt" );
		descriptor = FileDescriptor.of( "my-repo", "123/456", "my.file" );
		nonExistingFile = new File( UUID.randomUUID().toString() );

		resource = new LocalFileResource( descriptor, tempFile.toPath() );
	}

	@AfterEach
	@SuppressWarnings({ "ResultOfMethodCallIgnored" })
	void tearDown() {
		try {
			tempFile.delete();
		}
		catch ( Exception ignore ) {
		}
	}

	@Test
	void equals() {
		assertThat( resource )
				.isEqualTo( resource )
				.isNotEqualTo( mock( Resource.class ) )
				.isEqualTo( new LocalFileResource( descriptor, tempFile.toPath() ) )
				.isNotEqualTo( new LocalFileResource( FileDescriptor.of( "1:2:3" ), tempFile.toPath() ) );
	}

	@Test
	void folderResource() {
		descriptor = FileDescriptor.of( "my-repo", "123/456", tempFile.getName() );
		resource = new LocalFileResource( descriptor, tempFile.toPath() );

		assertThat( resource.getFolderResource() ).isNotNull();
		assertThat( resource.getFolderResource().getDescriptor() ).isEqualTo( resource.getDescriptor().getFolderDescriptor() );
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
				.isEqualTo( "axfs [" + descriptor.toString() + "] -> " + new PathResource( tempFile.toPath() ).getDescription() );
	}

	@Test
	void exists() {
		assertThat( resource.exists() )
				.isEqualTo( new FileSystemResource( tempFile ).exists() )
				.isTrue();

		assertThat( new LocalFileResource( descriptor, nonExistingFile.toPath() ).exists() ).isFalse();
	}

	@Test
	@SneakyThrows
	void targetDirectoryDoesNotOperateAsFileResource() {
		assertThat( tempFile.delete() ).isTrue();
		assertThat( resource.exists() ).isFalse();

		assertThat( tempFile.mkdir() ).isTrue();
		assertThat( resource.exists() ).isFalse();

		assertThat( resource.delete() ).isFalse();
		assertThat( tempFile.exists() ).isTrue();

		assertThat( resource.isWritable() ).isFalse();
		assertThat( resource.isReadable() ).isFalse();
		assertThat( resource.lastModified() ).isEqualTo( 0 );
		assertThat( resource.contentLength() ).isEqualTo( 0 );
	}

	@Test
	void getFile() {
		assertThatExceptionOfType( UnsupportedOperationException.class ).isThrownBy( () -> resource.getFile() );
	}

	@Test
	void isReadable() {
		assertThat( resource.isReadable() ).isTrue();

		assertThat( new LocalFileResource( descriptor, nonExistingFile.toPath() ).isReadable() )
				.isFalse()
				.isEqualTo( new FileSystemResource( nonExistingFile ).isReadable() );
	}

	@Test
	void isWritable() {
		assertThat( resource.isWritable() ).isTrue();

		assertThat( new LocalFileResource( descriptor, nonExistingFile.toPath() ).isWritable() )
				.isEqualTo( new FileSystemResource( nonExistingFile ).isWritable() )
				.isFalse();
	}

	@Test
	void isOpen() {
		assertThat( resource.isOpen() )
				.isFalse()
				.isEqualTo( new FileSystemResource( tempFile ).isOpen() );
	}

	@Test
	void delete() {
		assertThat( resource.exists() ).isTrue();
		assertThat( resource.delete() ).isTrue();
		assertThat( resource.delete() ).isFalse();
		assertThat( resource.exists() ).isFalse();
	}

	@Test
	void filename() {
		assertThat( resource.getFilename() ).isEqualTo( descriptor.getFileId() );
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Test
	@SneakyThrows
	void contentLengthAndLastModified() {
		assertThat( resource.contentLength() ).isEqualTo( 0 );

		try (InputStream is = RES_TEXTFILE.getInputStream()) {
			try (OutputStream os = resource.getOutputStream()) {
				IOUtils.copy( is, os );
			}
		}

		assertThat( resource.contentLength() )
				.isEqualTo( RES_TEXTFILE.contentLength() )
				.matches( l -> l > 0 );
		assertThat( resource.lastModified() )
				.isEqualTo( tempFile.lastModified() )
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

		assertThat( resourceData() ).isEqualTo( "some dummy text" );
	}

	@Test
	@SneakyThrows
	void copyFromResource() {
		resource.copyFrom( RES_TEXTFILE );
		assertThat( resourceData() ).isEqualTo( "some dummy text" );
	}

	@Test
	@SneakyThrows
	void copyFromFileResource() {
		File otherTempFile = File.createTempFile( UUID.randomUUID().toString(), ".txt" );
		FileResource other = new LocalFileResource( descriptor, otherTempFile.toPath() );
		other.copyFrom( RES_TEXTFILE );

		resource.copyFrom( other, false );
		assertThat( resourceData() ).isEqualTo( "some dummy text" );

		assertThat( other.exists() ).isTrue();
		FileUtils.writeStringToFile( otherTempFile, "hello file", "UTF-8" );

		resource.copyFrom( other, true );
		assertThat( resourceData() ).isEqualTo( "hello file" );
		assertThat( other.exists() ).isFalse();
	}

	@Test
	@SneakyThrows
	void noFileCreatedIfExceptionOnInputStream() {
		assertThat( tempFile.delete() ).isTrue();

		InputStream inputStream = mock( InputStream.class );
		when( inputStream.available() ).thenThrow( new IOException() );

		assertThatExceptionOfType( IOException.class )
				.isThrownBy( () -> resource.copyFrom( inputStream ) );
		assertThat( tempFile.exists() ).isFalse();
	}

	@Test
	@SneakyThrows
	void noFileCreatedIfExceptionOnOtherFileResource() {
		assertThat( tempFile.delete() ).isTrue();

		FileResource other = mock( FileResource.class );
		when( other.getInputStream() ).thenThrow( new IOException() );

		assertThatExceptionOfType( IOException.class )
				.isThrownBy( () -> resource.copyFrom( other ) );
		assertThat( tempFile.exists() ).isFalse();
	}

	@Test
	@SneakyThrows
	void fileIsNeverWrittenIfExceptionOnInputStream() {
		File otherTempFile = File.createTempFile( UUID.randomUUID().toString(), ".txt" );
		assertThat( otherTempFile.delete() ).isTrue();
		assertThat( tempFile.delete() ).isTrue();

		assertThatExceptionOfType( IOException.class )
				.isThrownBy( () -> resource.copyTo( otherTempFile ) );
		assertThat( otherTempFile.exists() ).isFalse();
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
