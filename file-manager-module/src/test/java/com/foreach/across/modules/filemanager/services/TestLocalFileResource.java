package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

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
		descriptor = FileDescriptor.of( "my-repo", "123/456", "my.file" );
		tempFile = File.createTempFile( UUID.randomUUID().toString(), ".txt" );
		nonExistingFile = new File( UUID.randomUUID().toString() );

		resource = new LocalFileResource( descriptor, tempFile );
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
	void fileDescriptor() {
		assertThat( resource.getFileDescriptor() ).isEqualTo( descriptor );
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
				.isEqualTo( "axfs [" + descriptor.toString() + "] -> " + new FileSystemResource( tempFile ).getDescription() );
	}

	@Test
	void exists() {
		assertThat( resource.exists() )
				.isEqualTo( new FileSystemResource( tempFile ).exists() )
				.isTrue();

		assertThat( new LocalFileResource( descriptor, nonExistingFile ).exists() ).isFalse();
	}

	@Test
	@SneakyThrows
	void getFile() {
		assertThat( resource.getFile() )
				.isNotNull()
				.isEqualTo( tempFile );
	}

	@Test
	void isReadable() {
		assertThat( resource.isReadable() ).isTrue();

		assertThat( new LocalFileResource( descriptor, nonExistingFile ).isReadable() )
				.isFalse()
				.isEqualTo( new FileSystemResource( nonExistingFile ).isReadable() );
	}

	@Test
	void isWritable() {
		assertThat( resource.isWritable() ).isTrue();

		assertThat( new LocalFileResource( descriptor, nonExistingFile ).isWritable() )
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
}
