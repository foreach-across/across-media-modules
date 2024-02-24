package com.foreach.across.modules.filemanager.services;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobServiceClient;
import com.foreach.across.modules.filemanager.business.*;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import utils.AzureStorageHelper;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

public class TestAzureFolderResource
{
	private static final String CONTAINER_NAME = "folder-resource-test";

	private BlobServiceClient blobServiceClient;
	private FolderDescriptor descriptor;
	private AzureFolderResource resource;
	private String objectName;

	private AzureFolderResource childFolder;
	private AzureFolderResource childFolderInChildFolder;
	private AzureFileResource childFile;
	private AzureFileResource childFileInChildFolder;

	@BeforeEach
	@SneakyThrows
	void resetResource() {
		if ( blobServiceClient == null ) {
			blobServiceClient = AzureStorageHelper.azurite.storageAccount();
			blobServiceClient.createBlobContainerIfNotExists( CONTAINER_NAME );
		}

		String parentObjectName = UUID.randomUUID() + "/";
		objectName = parentObjectName + "456/";
		descriptor = FolderDescriptor.of( "my-repo", "123/456" );
		resource = folderResource( descriptor, objectName );
	}

	@AfterEach
	@SneakyThrows
	void tearDown() {
		blobServiceClient.deleteBlobContainerIfExists( CONTAINER_NAME );
	}

	@Test
	void descriptor() {
		assertThat( resource.getDescriptor() ).isEqualTo( descriptor );
	}

	@Test
	@SneakyThrows
	void folderName() {
		assertThat( resource.getFolderName() ).isEqualTo( "456" );
		assertThat( folderResource( FolderDescriptor.of( "my-repo", "123" ), "some-obj/" ).getFolderName() ).isEqualTo( "123" );
		assertThat( folderResource( FolderDescriptor.rootFolder( "repo" ), "some-obj/" ).getFolderName() ).isNotNull().isEmpty();
	}

	@Test
	void uri() {
		assertThat( resource.getURI() ).isEqualTo( descriptor.toResourceURI() );
	}

	@Test
	@SneakyThrows
	void equals() {
		assertThat( resource )
				.isEqualTo( resource )
				.isNotEqualTo( mock( Resource.class ) )
				.isEqualTo( folderResource( descriptor, "123/456/" ) )
				.isNotEqualTo( folderResource( FolderDescriptor.of( "1:2/" ), "123/456/" ) );
	}

	@Test
	@SneakyThrows
	void parentFolderResource() {
		FolderResource rootFolder = folderResource( FolderDescriptor.rootFolder( "repo" ), "" );
		assertThat( rootFolder.getParentFolderResource() ).isEmpty();

		assertThat( resource.getParentFolderResource() )
				.contains( folderResource( descriptor.getParentFolderDescriptor().orElse( null ), "123/456/" ) );
	}

	@Test
	void folderAlwaysExists() {
		assertThat( resource.exists() ).isTrue();
	}

	@Test
	@SneakyThrows
	void create() {
		assertThat( resource.exists() ).isTrue();
		assertThat( resource.create() ).isTrue();
		assertThat( resource.exists() ).isTrue();
		assertThat( resource.create() ).isTrue();
		assertThat( resource.exists() ).isTrue();
	}

	@Test
	@SneakyThrows
	void rootFolderExistsButCannotBeCreatedOrDeleted() {
		AzureFolderResource rootFolder = folderResource( FolderDescriptor.rootFolder( "my-repo" ), "" );
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
				.hasSize( 3 )
				.containsExactlyInAnyOrder( childFile, childFolder, childFileInChildFolder )
				.isEqualTo( resource.findResources( "/**" ) );

		assertThat( resource.findResources( "*/*" ) ).containsExactlyInAnyOrder( childFileInChildFolder );
		assertThat( resource.findResources( "*/" ) ).containsExactly( childFolder );
		assertThat( resource.findResources( "*/*/" ) ).isEmpty();
		assertThat( resource.findResources( "**/" ) ).containsExactly( childFolder );

		assertThat( resource.findResources( "*", FileResource.class ) ).containsExactlyInAnyOrder( childFile );
		assertThat( resource.findResources( "*", FolderResource.class ) ).containsExactlyInAnyOrder( childFolder );
		assertThat( resource.findResources( "**", FileResource.class ) ).containsExactlyInAnyOrder( childFile, childFileInChildFolder );
		assertThat( resource.findResources( "**", FolderResource.class ) ).containsExactlyInAnyOrder( childFolder );
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
				.containsExactlyInAnyOrder( childFile, childFolder, childFileInChildFolder );
	}

	@Test
	void listChildren() {
		assertThat( resource.listResources( false ) ).isEmpty();
		assertThat( resource.listResources( true ) ).isEmpty();

		createFileTree();
		assertThat( resource.listResources( false ) ).containsExactlyInAnyOrder( childFile, childFolder );
		assertThat( resource.listResources( true ) )
				.containsExactlyInAnyOrder( childFile, childFolder, childFileInChildFolder );

		assertThat( resource.listResources( false, FileResource.class ) ).containsExactlyInAnyOrder( childFile );
		assertThat( resource.listResources( false, FolderResource.class ) ).containsExactlyInAnyOrder( childFolder );
		assertThat( resource.listResources( true, FileResource.class ) ).containsExactlyInAnyOrder( childFile, childFileInChildFolder );
		assertThat( resource.listResources( true, FolderResource.class ) ).containsExactlyInAnyOrder( childFolder );
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
	@SneakyThrows
	void getResource() {
		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> resource.getResource( null ) );

		assertThat( resource.getResource( "" ) ).isSameAs( resource );
		assertThat( resource.getResource( "/" ) ).isSameAs( resource );

		FileRepositoryResource fileResource = resource.getResource( "childFile" );
		assertThat( fileResource ).isNotNull().isInstanceOf( FileResource.class ).isEqualTo( resource.getResource( "/childFile" ) );
		assertThat( fileResource.exists() ).isFalse();

		FileRepositoryResource folderResource = resource.getResource( "childFolder/" );
		assertThat( folderResource ).isNotNull().isInstanceOf( FolderResource.class ).isEqualTo( resource.getResource( "/childFolder/" ) );
		assertThat( folderResource.exists() ).isTrue();

		createFileTree();
		assertThat( fileResource.exists() ).isTrue();

		assertThat( resource.getResource( "childFolder/childFileInChildFolder" ).exists() ).isTrue();
		assertThat( resource.getResource( "/childFolder/childFileInChildFolder" ).exists() ).isTrue();
		assertThat( resource.getResource( "childFolder/childFileInChildFolder/" ).exists() ).isTrue();
		assertThat( resource.getResource( "childFolder/childFolderInChildFolder" ).exists() ).isFalse();
		assertThat( resource.getResource( "childFolder/childFolderInChildFolder/" ).exists() ).isTrue();
		assertThat( resource.getResource( "/childFolder/childFolderInChildFolder/" ).exists() ).isTrue();

		FolderResource created = (FolderResource) resource.getResource( "/childFolder/childFolderInChildFolder/nestedFolder/" );
		assertThat( created.exists() ).isTrue();
		assertThat( created.create() ).isTrue();
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
	@SneakyThrows
	void deleteWithoutDeletingChildrenOnlyDeletesFolder() {
		assertThat( resource.exists() ).isTrue();
		assertThat( resource.delete( false ) ).isTrue();

		assertThat( resource.exists() ).isTrue();
		AzureStorageHelper.createFolder( blobServiceClient, CONTAINER_NAME, objectName );

		createFileTree();
		assertThat( resource.delete( false ) ).isTrue();

		assertThat( resource.exists() ).isTrue();
		assertThat( childFile.exists() ).isTrue();
	}

	@Test
	@SneakyThrows
	void deleteIfFolderNotEmpty() {
		createFileTree();

		assertThat( resource.exists() ).isTrue();
		assertThat( childFileInChildFolder.exists() ).isTrue();
		assertThat( resource.delete( false ) ).isTrue();

		childFileInChildFolder.resetBlobProperties();
		assertThat( childFileInChildFolder.exists() ).isTrue();

		AzureStorageHelper.createFolder( blobServiceClient, CONTAINER_NAME, objectName );
		assertThat( blobServiceClient.getBlobContainerClient( CONTAINER_NAME )
		                             .getBlobClient( objectName )
		                             .exists() )
				.isTrue();

		assertThat( resource.delete( true ) ).isTrue();

		childFileInChildFolder.resetBlobProperties();
		assertThat( childFileInChildFolder.exists() ).isFalse();
	}

	@Test
	void deleteChildren() {
		assertThat( resource.exists() ).isTrue();
		assertThat( resource.deleteChildren() ).isFalse();

		createFileTree();

		assertThat( resource.exists() ).isTrue();
		assertThat( childFileInChildFolder.exists() ).isTrue();

		assertThat( resource.deleteChildren() ).isTrue();
		childFileInChildFolder.resetBlobProperties();
		assertThat( childFileInChildFolder.exists() ).isFalse();
		assertThat( resource.exists() ).isTrue();
	}

	@Test
	void toStringIsCorrect() {
		assertThat( resource.toString() ).startsWith( "axfs [" + descriptor.toString() + "] -> Azure storage blob resource" );
	}

	@SneakyThrows
	private void createFileTree() {
		blobServiceClient.getBlobContainerClient( CONTAINER_NAME )
		                 .getBlobClient( objectName + "childFile" )
		                 .upload( BinaryData.fromString( "dummy file" ) );
		childFile = fileResource( FileDescriptor.of( descriptor.getRepositoryId(), descriptor.getFolderId(), "childFile" ), objectName + "childFile" );

		assertThat( childFile.exists() ).isTrue();

		AzureStorageHelper.createFolder( blobServiceClient, CONTAINER_NAME, objectName + "childFolder/" );
		childFolder = folderResource( FolderDescriptor.of( descriptor.getRepositoryId(), descriptor.getFolderId() + "/childFolder" ),
		                              objectName + "childFolder/" );
		assertThat( childFolder.exists() ).isTrue();

		blobServiceClient.getBlobContainerClient( CONTAINER_NAME )
		                 .getBlobClient( objectName + "childFolder/childFileInChildFolder" )
		                 .upload( BinaryData.fromString( "" ) );
		childFileInChildFolder = fileResource(
				FileDescriptor.of( descriptor.getRepositoryId(), childFolder.getDescriptor().getFolderId(), "childFileInChildFolder" ),
				objectName + "childFolder/childFileInChildFolder"
		);
		assertThat( childFileInChildFolder.exists() ).isTrue();

		childFolderInChildFolder = folderResource(
				FolderDescriptor.of( descriptor.getRepositoryId(),
				                     childFolder.getDescriptor().getFolderId() + "/childFolderInChildFolder" ),
				objectName + "childFolder/childFolderInChildFolder/" );
		// Explicitly created so it would exist as empty
		assertThat( childFolderInChildFolder.create() ).isTrue();
		assertThat( childFolderInChildFolder.exists() ).isTrue();
	}

	private AzureFolderResource folderResource( FolderDescriptor descriptor, String objectName ) throws IOException {
		return new AzureFolderResource( descriptor, blobServiceClient, CONTAINER_NAME, objectName );
	}

	private AzureFileResource fileResource( FileDescriptor descriptor, String objectName ) throws IOException {
		return new AzureFileResource( descriptor, blobServiceClient, CONTAINER_NAME, objectName );
	}

}
