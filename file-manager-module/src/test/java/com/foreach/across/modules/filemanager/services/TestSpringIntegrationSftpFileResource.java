package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.util.StreamUtils;
import utils.SftpContainer;

import java.io.*;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static utils.SftpContainer.TEST_PORT;

@Slf4j
class TestSpringIntegrationSftpFileResource
{
	private static final Resource RES_TEXTFILE = new ClassPathResource( "textfile.txt" );

	private static final SftpContainer ftpContainer = new SftpContainer();

	private FileDescriptor descriptor;
	private FileResource resource;
	private String objectName;

	private SftpRemoteFileTemplate template;

	private SftpRemoteFileTemplate getSftpRemoteFileTemplate() {
		if ( template == null ) {
			DefaultSftpSessionFactory defaultFtpSessionFactory = new DefaultSftpSessionFactory();
			defaultFtpSessionFactory.setUser( "fmm" );
			defaultFtpSessionFactory.setPassword( "test" );
			defaultFtpSessionFactory.setHost( "localhost" );
			defaultFtpSessionFactory.setPort( TEST_PORT );
			defaultFtpSessionFactory.setTimeout( 5000 );
			defaultFtpSessionFactory.setChannelConnectTimeout( Duration.ofSeconds( 5 ) );
			defaultFtpSessionFactory.setAllowUnknownKeys( true );

			template = new SftpRemoteFileTemplate( defaultFtpSessionFactory );
			template.setAutoCreateDirectory( true );
		}
		return template;
	}

	@BeforeAll
	static void init() {
		ftpContainer.start();
	}

	@AfterAll
	static void tearDown() {
		ftpContainer.stop();
	}

	@BeforeEach
	@SneakyThrows
	void createResource() {
		objectName = UUID.randomUUID().toString();
		descriptor = FileDescriptor.of( "my-repo", "123/456", objectName );
		resource = new SpringIntegrationSftpFileResource( descriptor, null, getSftpRemoteFileTemplate() );
		if ( !resource.getFolderResource().exists() ) {
			resource.getFolderResource().create();
		}
	}

	@Test
	void equals() {
		assertThat( resource )
				.isEqualTo( resource )
				.isNotEqualTo( mock( Resource.class ) )
				.isEqualTo( new SpringIntegrationSftpFileResource( resource.getDescriptor(), null, getSftpRemoteFileTemplate() ) )
				.isNotEqualTo( new SpringIntegrationSftpFileResource( FileDescriptor.of( "1:2:3" ), null, getSftpRemoteFileTemplate() ) );
	}

	@Test
	void folderResource() {
		assertThat( resource.getFolderResource() ).isNotNull();
		assertThat( resource.getFolderResource().getDescriptor() ).isEqualTo( resource.getDescriptor().getFolderDescriptor() );

		assertThat( resource.getFolderResource().listFiles() ).doesNotContain( resource );
		createFileViaFtp();
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
				.startsWith( "axfs [" + descriptor.toString() + "] -> FTP file" )
				.isEqualTo( resource.toString() );
	}

	@Test
	void exists() {
		assertThat( resource.exists() ).isFalse();
		createFileViaFtp();
		assertThat( resource.exists() ).isTrue();
	}

	@Test
	void getFile() {
		assertThatExceptionOfType( UnsupportedOperationException.class ).isThrownBy( () -> resource.getFile() );
	}

	@Test
	void isReadable() {
		createFileViaFtp();
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
		createFileViaFtp();
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
		createFileViaFtp();
		assertThat( resource.exists() ).isTrue();
		assertThat( resource.contentLength() ).isEqualTo( 9 );

		try (InputStream is = RES_TEXTFILE.getInputStream()) {
			try (OutputStream os = resource.getOutputStream()) {
				IOUtils.copy( is, os );
			}
		}

		assertThat( resource.exists() ).isTrue();
		assertThat( resource.contentLength() ).isNotEqualTo( 9 ).isEqualTo( RES_TEXTFILE.contentLength() );

		String fileContent = getSftpRemoteFileTemplate().<String, ChannelSftp>executeWithClient( client -> {
			String path = SpringIntegrationFileResource.getPath( resource.getDescriptor() );
			try {
				InputStream is = client.get( path );
				String content = IOUtils.toString( is, Charset.forName( "UTF-8" ) );
				is.close();
				return content;
			}
			catch ( IOException | SftpException ignore ) {
				return "";
			}
		} );
		assertThat( fileContent )
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
		createFileViaFtp();

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
		assertThat( verifyFileExistsViaFtp() ).isFalse();

		InputStream inputStream = mock( InputStream.class );
		when( inputStream.available() ).thenThrow( new IOException() );

		assertThatExceptionOfType( IOException.class )
				.isThrownBy( () -> resource.copyFrom( inputStream ) );
		assertThat( verifyFileExistsViaFtp() ).isFalse();
	}

	@Test
	@SneakyThrows
	void noFileCreatedIfExceptionOnOtherFileResource() {
		assertThat( verifyFileExistsViaFtp() ).isFalse();

		FileResource other = mock( FileResource.class );
		when( other.getInputStream() ).thenThrow( new IOException() );

		assertThatExceptionOfType( IOException.class )
				.isThrownBy( () -> resource.copyFrom( other ) );
		assertThat( verifyFileExistsViaFtp() ).isFalse();
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

	private void createFileViaFtp() {
		getSftpRemoteFileTemplate().<Void, ChannelSftp>executeWithClient( client -> {
			String path = SpringIntegrationFileResource.getPath( resource.getDescriptor() );
			try {
				InputStream inputStream = IOUtils.toInputStream( "some-data", "UTF-8" );
				client.put( inputStream, path );
				inputStream.close();
			}
			catch ( IOException | SftpException ignore ) {
				LOG.error( "Unable to create file via SFTP", ignore );
			}
			return null;
		} );
	}

	private boolean verifyFileExistsViaFtp() {
		return getSftpRemoteFileTemplate().<Boolean, ChannelSftp>executeWithClient( client -> {
			String path = SpringIntegrationFileResource.getPath( resource.getDescriptor() );
			try {
				return client.stat( path ) != null;
			}
			catch ( SftpException e ) {
				return false;
			}
		} );
	}
}
