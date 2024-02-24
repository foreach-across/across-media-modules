package com.foreach.across.modules.filemanager.services;

import com.azure.storage.blob.BlobServiceClient;
import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import com.foreach.across.modules.filemanager.business.FolderDescriptor;
import com.foreach.across.modules.filemanager.business.FolderResource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.AzureStorageHelper;

import java.io.File;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestAzureFileRepository extends BaseFileRepositoryTest
{
	private static final String CONTAINER_NAME = "ax-filemanager-test";

	private static BlobServiceClient blobServiceClient;

	@BeforeAll
	@SneakyThrows
	static void createResource() {
		if ( blobServiceClient == null ) {
			blobServiceClient = AzureStorageHelper.azurite.storageAccount();
			blobServiceClient.getBlobContainerClient( CONTAINER_NAME ).createIfNotExists();
		}
	}

	@Override
	FileRepository createRepository() {
		FileManager fileManager = mock( FileManager.class );
		when( fileManager.createTempFile() ).thenAnswer( invoc -> new File( tempDir, UUID.randomUUID().toString() ) );

		AzureFileRepository abs = AzureFileRepository.builder()
		                                             .repositoryId( "azure-repo" )
		                                             .blobServiceClient( blobServiceClient )
		                                             .containerName( CONTAINER_NAME )
		                                             .build();
		abs.setFileManager( fileManager );
		return abs;
	}

	@AfterAll
	@SneakyThrows
	static void destroyResource() {
		if ( blobServiceClient != null ) {
			blobServiceClient.getBlobContainerClient( CONTAINER_NAME ).deleteIfExists();
		}
	}

	@Test
	@SneakyThrows
	void folderIsCreated() {
		FileResource file = fileRepository.getFileResource( FileDescriptor.of( fileRepository.getRepositoryId() + ":aa/bb/cc:myfile" ) );
		assertThat( fileRepository.getFolderResource( FolderDescriptor.of( fileRepository.getRepositoryId() + ":aa/" ) ).exists() ).isTrue();
		assertThat( fileRepository.getFolderResource( FolderDescriptor.of( fileRepository.getRepositoryId() + ":aa/bb/" ) ).exists() ).isTrue();
		assertThat( fileRepository.getFolderResource( FolderDescriptor.of( fileRepository.getRepositoryId() + ":aa/bb/cc/" ) ).exists() ).isTrue();

		file.copyFrom( RES_TEXTFILE );

		assertThat( file.exists() ).isTrue();

		FolderResource cc = file.getFolderResource();
		assertThat( cc.getDescriptor() ).isEqualTo( FolderDescriptor.of( fileRepository.getRepositoryId() + ":aa/bb/cc/" ) );
		assertThat( cc.listResources( false ) ).containsExactly( file );
		assertThat( cc.getFolderName() ).isEqualTo( "cc" );

		FolderResource bb = cc.getParentFolderResource().orElseThrow( AssertionError::new );
		assertThat( bb.getDescriptor() ).isEqualTo( FolderDescriptor.of( fileRepository.getRepositoryId() + ":aa/bb/" ) );
		assertThat( bb.listResources( false ) ).containsExactly( cc );
		assertThat( bb.getFolderName() ).isEqualTo( "bb" );

		FolderResource aa = bb.getParentFolderResource().orElseThrow( AssertionError::new );
		assertThat( aa.getDescriptor() ).isEqualTo( FolderDescriptor.of( fileRepository.getRepositoryId() + ":aa/" ) );
		assertThat( aa.listResources( false ) ).containsExactly( bb );
		assertThat( aa.getFolderName() ).isEqualTo( "aa" );
		assertThat( aa.listResources( true ) ).containsExactlyInAnyOrder( bb, cc, file );

		FolderResource root = aa.getParentFolderResource().orElseThrow( AssertionError::new );
		assertThat( root.getDescriptor() ).isEqualTo( FolderDescriptor.rootFolder( fileRepository.getRepositoryId() ) );
		assertThat( root.listResources( false ) ).contains( aa );
		assertThat( root.getFolderName() ).isEmpty();

		assertThat( root.getParentFolderResource() ).isEmpty();
	}

	@Test
	@SneakyThrows
	void createFolderAndFilesInFolder() {
		FolderResource folder = fileRepository.getFolderResource( FolderDescriptor.of( fileRepository.getRepositoryId() + ":dd/cc/" ) );
		assertThat( folder.exists() ).isTrue();
		folder.create();

		assertThat( folder.exists() ).isTrue();
		assertThat( folder.listResources( false ) ).isEmpty();

		FileResource file = folder.createFileResource();
		assertThat( file.exists() ).isFalse();
		file.copyFrom( RES_TEXTFILE );
		assertThat( file.exists() ).isTrue();

		FileResource other = folder.getFileResource( "/myfile.txt" );
		assertThat( other.exists() ).isFalse();
		other.copyFrom( RES_TEXTFILE );
		assertThat( other.exists() ).isTrue();

		assertThat( folder.listResources( false ) ).containsExactlyInAnyOrder( file, other );

		FileResource nested = folder.getFileResource( "/subFolder/myfile.txt" );
		assertThat( nested.exists() ).isFalse();
		nested.copyFrom( RES_TEXTFILE );
		assertThat( nested.exists() ).isTrue();

		assertThat( folder.listResources( false ) ).containsExactlyInAnyOrder( folder.getFolderResource( "subFolder/" ), file, other );
		assertThat( folder.listResources( true ) ).containsExactlyInAnyOrder( folder.getFolderResource( "subFolder/" ), file, other, nested );

		assertThat( fileRepository.getRootFolderResource().listResources( false ) )
				.contains( folder.getParentFolderResource().orElseThrow( AssertionError::new ) );

		assertThat( folder.findResources( "/**/myfile.txt" ) ).containsExactlyInAnyOrder( other, nested );
		assertThat( folder.findResources( "/**/subFolder/*" ) ).containsExactly( nested );
	}

	@Test
	@SneakyThrows
	void fileInRootFolder() {
		FolderResource root = fileRepository.getRootFolderResource();
		FolderResource folderInRoot = root.getFolderResource( "ee" );

		FileResource fileInRoot = root.createFileResource();
		assertThat( fileInRoot.exists() ).isFalse();
		fileInRoot.copyFrom( RES_TEXTFILE );

		FileResource fileInFolderInRoot = folderInRoot.createFileResource();
		assertThat( fileInFolderInRoot.exists() ).isFalse();
		fileInFolderInRoot.copyFrom( RES_TEXTFILE );

		assertThat( folderInRoot.exists() ).isTrue();
		assertThat( root.listFolders() ).contains( folderInRoot );
		assertThat( root.listFiles() ).contains( fileInRoot ).doesNotContain( fileInFolderInRoot );
		assertThat( folderInRoot.listFiles() ).contains( fileInFolderInRoot );

		assertThat( root.findResources( "/ee" ) ).contains( folderInRoot );
		assertThat( root.findResources( "ee" ) ).contains( folderInRoot );
		assertThat( root.findResources( "/?e/*" ) ).contains( fileInFolderInRoot );
		assertThat( root.findResources( "/ee/*" ) ).contains( fileInFolderInRoot );
	}

	@Override
	@Test
	@Disabled
	void findResourcesAndFilesWithHierarchySetup() {
		super.findResourcesAndFilesWithHierarchySetup();
	}
}
