package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.*;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.ftp.session.FtpRemoteFileTemplate;
import utils.FtpContainer;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static utils.FtpContainer.TEST_PORT;

/**
 * @author Arne Vandamme
 * @since 1.4.0
 */
class TestSpringIntegrationFtpFolderResource
{
	private static final FtpContainer ftpContainer = new FtpContainer();

	private FolderDescriptor descriptor;
	private SpringIntegrationFtpFolderResource resource;
	private String objectName;

	private SpringIntegrationFtpFolderResource childFolder;
	private SpringIntegrationFtpFolderResource childFolderInChildFolder;
	private SpringIntegrationFtpFileResource childFile;
	private SpringIntegrationFtpFileResource childFileInChildFolder;

	private FtpRemoteFileTemplate template;

	private FtpRemoteFileTemplate getFtpRemoteFileTemplate() {
		if ( template == null ) {
			DefaultFtpSessionFactory defaultFtpSessionFactory = new DefaultFtpSessionFactory();
			defaultFtpSessionFactory.setClientMode( FTPClient.PASSIVE_LOCAL_DATA_CONNECTION_MODE );
			defaultFtpSessionFactory.setUsername( "fmm" );
			defaultFtpSessionFactory.setPassword( "test" );
			defaultFtpSessionFactory.setHost( "localhost" );
			defaultFtpSessionFactory.setPort( TEST_PORT );
			defaultFtpSessionFactory.setDefaultTimeout( 5000 );
			defaultFtpSessionFactory.setConnectTimeout( 5000 );
			defaultFtpSessionFactory.setDataTimeout( 5000 );

			template = new FtpRemoteFileTemplate( defaultFtpSessionFactory );
			// STAT seems to incorrectly return files as still existing
			template.setExistsMode( FtpRemoteFileTemplate.ExistsMode.NLST );
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
	void resetResource() {
		String parentObjectName = UUID.randomUUID().toString() + "/";
		objectName = parentObjectName + "456/";
		descriptor = FolderDescriptor.of( "my-repo", "123/456" ).createFolderDescriptor( objectName );
//		FolderDescriptor resourceDescriptor = descriptor.createFolderDescriptor( objectName );
//		createFolderViaFtp( SpringIntegrationFolderResource.getPath( resourceDescriptor ) );
		resource = folderResource( descriptor );
	}

	@Test
	void descriptor() {
		assertThat( resource.getDescriptor() ).isEqualTo( descriptor );
	}

	@Test
	void folderName() {
		assertThat( resource.getFolderName() ).isEqualTo( "456" );
		assertThat( folderResource( FolderDescriptor.of( "my-repo", "123" ) ).getFolderName() ).isEqualTo( "123" );
		assertThat( folderResource( FolderDescriptor.rootFolder( "repo" ) ).getFolderName() ).isNotNull().isEmpty();
	}

	@Test
	void uri() {
		assertThat( resource.getURI() ).isEqualTo( descriptor.toResourceURI() );
	}

	@Test
	void equals() {
		assertThat( resource )
				.isEqualTo( resource )
				.isNotEqualTo( mock( Resource.class ) )
				.isEqualTo( folderResource( descriptor ) )
				.isNotEqualTo( folderResource( FolderDescriptor.of( "1:2/" ) ) );
	}

	@Test
	void parentFolderResource() {
		FolderResource rootFolder = folderResource( FolderDescriptor.rootFolder( "repo" ) );
		assertThat( rootFolder.getParentFolderResource() ).isEmpty();

		assertThat( resource.getParentFolderResource() )
				.contains( folderResource( descriptor.getParentFolderDescriptor().orElse( null ) ) );
	}

	@Test
	void folderExistsIfItExistsRemotely() {
		assertThat( resource.exists() ).isFalse();
		createFolderViaFtp( resource.getPath() );
		assertThat( resource.exists() ).isTrue();
	}

	@Test
	void create() {
		assertThat( verifyResourceFolderExistsViaFtp() ).isFalse();
		assertThat( resource.exists() ).isFalse();
		assertThat( resource.create() ).isTrue();
		assertThat( verifyResourceFolderExistsViaFtp() ).isTrue();
		assertThat( resource.exists() ).isTrue();
		assertThat( resource.create() ).isFalse();
		assertThat( resource.exists() ).isTrue();
	}

	@Test
	void rootFolderExistsButCannotBeCreatedOrDeleted() {
		SpringIntegrationFtpFolderResource rootFolder = folderResource( FolderDescriptor.rootFolder( "my-repo" ) );
		assertThat( rootFolder.exists() ).isTrue();
		assertThat( rootFolder.create() ).isFalse();
		assertThat( rootFolder.delete( false ) ).isFalse();
	}

	@Test
	void findResourcesInNonExistingFolder() {
		assertThat( resource.findResources( "*" ) ).isEmpty();
		assertThat( resource.create() ).isTrue();
		assertThat( resource.findResources( "*" ) ).isEmpty();
	}

	@Test
	void findResources() {
		createFileTree();

		assertThat( resource.findResources( "*" ) )
				.isNotEmpty()
				.hasSize( 2 )
				.containsExactlyInAnyOrder( childFile, childFolder )
				.isEqualTo( resource.findResources( "/*" ) );
		assertThat( resource.findResources( "**" ) )
				.isNotEmpty()
				.hasSize( 4 )
				.containsExactlyInAnyOrder( childFile, childFolder, childFolderInChildFolder, childFileInChildFolder )
				.isEqualTo( resource.findResources( "/**" ) );

		assertThat( resource.findResources( "*/*" ) ).containsExactlyInAnyOrder( childFolderInChildFolder, childFileInChildFolder );
		assertThat( resource.findResources( "*/" ) ).containsExactly( childFolder );
		assertThat( resource.findResources( "*/*/" ) ).containsExactly( childFolderInChildFolder );
		assertThat( resource.findResources( "**/" ) ).containsExactly( childFolder, childFolderInChildFolder );

		assertThat( resource.findResources( "*", FileResource.class ) ).containsExactlyInAnyOrder( childFile );
		assertThat( resource.findResources( "*", FolderResource.class ) ).containsExactlyInAnyOrder( childFolder );
		assertThat( resource.findResources( "**", FileResource.class ) ).containsExactlyInAnyOrder( childFile, childFileInChildFolder );
		assertThat( resource.findResources( "**", FolderResource.class ) ).containsExactlyInAnyOrder( childFolder, childFolderInChildFolder );
		assertThat( resource.findResources( "**/", FileResource.class ) ).isEmpty();

		assertThat( resource.findResources( "childFil?" ) )
				.containsExactly( childFile )
				.satisfies( l -> {
					try {
						FileResource fr = (FileResource) l.iterator().next();
						assertThat( fr.contentLength() ).isEqualTo( 10L );
					}
					catch ( IOException ioe ) {
						throw new AssertionError( ioe );
					}
				} );
		assertThat( resource.findResources( "childFile" ) ).containsExactly( childFile );
		assertThat( resource.findResources( "/**/childFile" ) ).containsExactly( childFile );
		assertThat( resource.findResources( "/**/childFile*" ) ).containsExactlyInAnyOrder( childFile, childFileInChildFolder );
		assertThat( resource.findResources( "/childFolder/childFileInChildFolder" ) ).containsExactlyInAnyOrder( childFileInChildFolder );
		assertThat( resource.findResources( "/**/child*" ) )
				.containsExactlyInAnyOrder( childFile, childFolder, childFolderInChildFolder, childFileInChildFolder );
	}

	@Test
	void listChildren() {
		assertThat( resource.listResources( false ) ).isEmpty();
		assertThat( resource.listResources( true ) ).isEmpty();

		createFileTree();
		assertThat( resource.listResources( false ) ).containsExactlyInAnyOrder( childFile, childFolder );
		assertThat( resource.listResources( true ) )
				.containsExactlyInAnyOrder( childFile, childFolder, childFolderInChildFolder, childFileInChildFolder );

		assertThat( resource.listResources( false, FileResource.class ) ).containsExactlyInAnyOrder( childFile );
		assertThat( resource.listResources( false, FolderResource.class ) ).containsExactlyInAnyOrder( childFolder );
		assertThat( resource.listResources( true, FileResource.class ) ).containsExactlyInAnyOrder( childFile, childFileInChildFolder );
		assertThat( resource.listResources( true, FolderResource.class ) ).containsExactlyInAnyOrder( childFolder, childFolderInChildFolder );
	}

	@Test
	void listFiles() {
		assertThat( resource.listFiles() ).isEmpty();

		createFileTree();
		assertThat( resource.listFiles() ).containsExactly( childFile );
	}

	@Test
	void listFolders() {
		assertThat( resource.listFolders() ).isEmpty();

		createFileTree();
		assertThat( resource.listFolders() ).containsExactly( childFolder );
	}

	@Test
	void emptyIfResourceNotExists() {
		assertThat( resource.isEmpty() ).isTrue();
	}

	@Test
	void notEmptyIfChildren() {
		createFileTree();
		assertThat( resource.isEmpty() ).isFalse();
	}

	@Test
	void getResource() {
		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> resource.getResource( null ) );

		assertThat( resource.getResource( "" ) ).isSameAs( resource );
		assertThat( resource.getResource( "/" ) ).isSameAs( resource );

		FileRepositoryResource fileResource = resource.getResource( "childFile" );
		assertThat( fileResource ).isNotNull().isInstanceOf( FileResource.class ).isEqualTo( resource.getResource( "/childFile" ) );
		assertThat( fileResource.exists() ).isFalse();

		FileRepositoryResource folderResource = resource.getResource( "childFolder/" );
		assertThat( folderResource ).isNotNull().isInstanceOf( FolderResource.class ).isEqualTo( resource.getResource( "/childFolder/" ) );
		assertThat( folderResource.exists() ).isFalse();

		createFileTree();
		assertThat( fileResource.exists() ).isTrue();

		assertThat( resource.getResource( "childFolder/childFileInChildFolder" ).exists() ).isTrue();
		assertThat( resource.getResource( "/childFolder/childFileInChildFolder" ).exists() ).isTrue();
		assertThat( resource.getResource( "childFolder/childFileInChildFolder/" ).exists() ).isTrue();

		//todo should this be false? why?
		assertThat( resource.getResource( "childFolder/childFolderInChildFolder" ).exists() ).isTrue();

		assertThat( resource.getResource( "childFolder/childFolderInChildFolder/" ).exists() ).isTrue();
		assertThat( resource.getResource( "/childFolder/childFolderInChildFolder/" ).exists() ).isTrue();

		FolderResource created = (FolderResource) resource.getResource( "/childFolder/childFolderInChildFolder/nestedFolder/" );
		assertThat( created.exists() ).isFalse();
		assertThat( created.create() ).isTrue();

		assertThat( SpringIntegrationFolderResource.getPath( created.getDescriptor() ) )
				.isEqualTo( SpringIntegrationFolderResource
						            .getPath( resource.getFolderResource( "childFolder/childFolderInChildFolder/nestedFolder" ).getDescriptor() ) );
		assertThat( verifyFolderExistsViaFtp( SpringIntegrationFolderResource.getPath( created.getDescriptor() ) ) ).isTrue();
		assertThat( verifyFolderExistsViaFtp(
				SpringIntegrationFolderResource
						.getPath( resource.getFolderResource( "childFolder/childFolderInChildFolder/nestedFolder/" ).getDescriptor() ) ) ).isTrue();
	}

	@Test
	@SneakyThrows
	void getFileResource() {
		createFileTree();

		assertThat( resource.getFileResource( "childFile" ) ).isEqualTo( childFile ).matches( Resource::exists );
		assertThat( resource.getFileResource( "/childFile" ) ).isEqualTo( childFile );

		assertThat( resource.getFileResource( "childFile" ).contentLength() ).isEqualTo( 10L );
		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> resource.getFileResource( "" ) );
		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> resource.getFileResource( "/" ) );
		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> resource.getFileResource( null ) );
		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> resource.getFileResource( "childFile/" ) );
	}

	@Test
	void getFolderResource() {
		createFileTree();

		assertThat( resource.getFolderResource( "childFolder" ) ).isEqualTo( childFolder ).matches( FileRepositoryResource::exists );
		assertThat( resource.getFolderResource( "/childFolder" ) ).isEqualTo( childFolder );
		assertThat( resource.getFolderResource( "/childFolder/" ) ).isEqualTo( childFolder );

		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> resource.getFileResource( null ) );
		assertThat( resource.getFolderResource( "" ) ).isSameAs( resource );
		assertThat( resource.getFolderResource( "/" ) ).isSameAs( resource );

		assertThat( resource.getFolderResource( "/childFile" ) ).isNotEqualTo( childFile ).matches( FileRepositoryResource::exists );
	}

	@Test
	void createFileResource() {
		FileResource one = resource.createFileResource();
		FileResource two = resource.createFileResource();
		assertThat( one ).isNotNull().isNotEqualTo( two );

		assertThat( one.getDescriptor().getFolderDescriptor() ).isEqualTo( resource.getDescriptor() );
		assertThat( two.getDescriptor().getFolderDescriptor() ).isEqualTo( resource.getDescriptor() );
	}

	@Test
	void deleteWithoutDeletingChildrenOnlyDeletesFolder() {
		assertThat( resource.exists() ).isFalse();
		assertThat( verifyResourceFolderExistsViaFtp() ).isFalse();
		assertThat( resource.delete( false ) ).isFalse();

		assertThat( resource.exists() ).isFalse();
		createResourceFolderViaFtp();
		assertThat( verifyResourceFolderExistsViaFtp() ).isTrue();

		createFileTree();
		assertThat( resource.delete( false ) ).isFalse();

		assertThat( resource.exists() ).isTrue();
		assertThat( verifyResourceFolderExistsViaFtp() ).isTrue();
		assertThat( childFile.exists() ).isTrue();
	}

	@Test
	void deleteIfFolderNotEmpty() {
		createFileTree();

		assertThat( resource.exists() ).isTrue();
		assertThat( childFileInChildFolder.exists() ).isTrue();
		assertThat( resource.delete( false ) ).isFalse();

		childFileInChildFolder.resetFileMetadata();
		assertThat( childFileInChildFolder.exists() ).isTrue();

		createResourceFolderViaFtp();
		assertThat( verifyResourceFolderExistsViaFtp() ).isTrue();

		assertThat( resource.delete( true ) ).isTrue();

		childFileInChildFolder.resetFileMetadata();
		assertThat( childFileInChildFolder.exists() ).isFalse();
		assertThat( verifyResourceFolderExistsViaFtp() ).isFalse();
	}

	@Test
	void deleteChildren() {
		assertThat( resource.exists() ).isFalse();
		assertThat( resource.deleteChildren() ).isFalse();

		createFileTree();

		assertThat( resource.exists() ).isTrue();
		assertThat( childFileInChildFolder.exists() ).isTrue();

		assertThat( resource.deleteChildren() ).isTrue();
		childFileInChildFolder.resetFileMetadata();
		assertThat( childFileInChildFolder.exists() ).isFalse();
		assertThat( resource.exists() ).isTrue();
	}

	@SneakyThrows
	private void createFileTree() {
		createFolderViaFtp( SpringIntegrationFolderResource.getPath( descriptor ) );
		createFileViaFtp( SpringIntegrationFileResource.getPath( descriptor.createFileDescriptor( "childFile" ) ), "dummy file" );
		childFile = fileResource( FileDescriptor.of( descriptor.getRepositoryId(), descriptor.getFolderId(), "childFile" ) );

		assertThat( childFile.exists() ).isTrue();

		createFolderViaFtp( SpringIntegrationFolderResource.getPath( descriptor.createFolderDescriptor( "childFolder/" ) ) );
		childFolder = folderResource( FolderDescriptor.of( descriptor.getRepositoryId(), descriptor.getFolderId() + "/childFolder" )
		);
		assertThat( childFolder.exists() ).isTrue();

		createFileViaFtp( SpringIntegrationFileResource.getPath( descriptor.createFileDescriptor( "childFolder/childFileInChildFolder" ) ), "" );
		childFileInChildFolder = fileResource(
				FileDescriptor.of( descriptor.getRepositoryId(), childFolder.getDescriptor().getFolderId(), "childFileInChildFolder" )
		);
		assertThat( childFileInChildFolder.exists() ).isTrue();

		childFolderInChildFolder = folderResource(
				FolderDescriptor.of( descriptor.getRepositoryId(),
				                     childFolder.getDescriptor().getFolderId() + "/childFolderInChildFolder" )
		);
		// Explicitly created so it would exist as empty
		assertThat( childFolderInChildFolder.create() ).isTrue();
		assertThat( childFolderInChildFolder.exists() ).isTrue();
	}

	private SpringIntegrationFtpFolderResource folderResource( FolderDescriptor descriptor ) {
		return new SpringIntegrationFtpFolderResource( descriptor, getFtpRemoteFileTemplate() );
	}

	private SpringIntegrationFtpFileResource fileResource( FileDescriptor descriptor ) {
		return new SpringIntegrationFtpFileResource( descriptor, null, getFtpRemoteFileTemplate() );
	}

	private void createResourceFolderViaFtp() {
		createFolderViaFtp( SpringIntegrationFolderResource.getPath( resource.getDescriptor() ) );
	}

	private void createFolderViaFtp( String path ) {
		getFtpRemoteFileTemplate().<Void, FTPClient>executeWithClient( client -> {
			String[] parts = path.split( "/" );
			try {
				for ( int i = 0; i < parts.length; i++ ) {
					String part = StringUtils.join( ArrayUtils.subarray( parts, 0, i + 1 ), "/" );
					FTPFile remoteFile = client.mlistFile( part );
					if ( remoteFile == null ) {
						client.makeDirectory( part );
					}
				}
			}
			catch ( IOException ignore ) {
			}
			return null;
		} );
	}

	private void createFileViaFtp( String path, String content ) {
		getFtpRemoteFileTemplate().<Void, FTPClient>executeWithClient( client -> {
			try {
				InputStream inputStream = IOUtils.toInputStream( content, "UTF-8" );
				client.storeFile( path, inputStream );
				inputStream.close();
			}
			catch ( IOException ignore ) {
			}
			return null;
		} );
	}

	private boolean verifyResourceFolderExistsViaFtp() {
		return verifyFolderExistsViaFtp( SpringIntegrationFolderResource.getPath( resource.getDescriptor() ) );
	}

	private boolean verifyFolderExistsViaFtp( String path ) {
		return getFtpRemoteFileTemplate().<Boolean, FTPClient>executeWithClient( client -> {
			try {
				FTPFile ftpFile = client.mlistFile( path );
				return ftpFile != null && ftpFile.isDirectory();
			}
			catch ( IOException e ) {
				return false;
			}
		} );
	}
}
