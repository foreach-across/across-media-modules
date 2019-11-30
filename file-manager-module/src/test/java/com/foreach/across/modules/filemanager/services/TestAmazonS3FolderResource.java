package com.foreach.across.modules.filemanager.services;

import com.amazonaws.services.s3.AmazonS3;
import com.foreach.across.modules.filemanager.business.*;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SyncTaskExecutor;
import utils.AmazonS3Helper;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

/**
 * @author Arne Vandamme
 * @since 1.4.0
 */
class TestAmazonS3FolderResource
{
	private static final String BUCKET_NAME = "folder-resource-test";
	private static final SyncTaskExecutor TASK_EXECUTOR = new SyncTaskExecutor();

	private static AmazonS3 amazonS3 = AmazonS3Helper.createClientWithBuckets( BUCKET_NAME );

	private FolderDescriptor descriptor;
	private AmazonS3FolderResource resource;
	private String objectName;

	private AmazonS3FolderResource childFolder;
	private AmazonS3FolderResource childFolderInChildFolder;
	private AmazonS3FileResource childFile;
	private AmazonS3FileResource childFileInChildFolder;

	@BeforeEach
	@SneakyThrows
	void resetResource() {
		String parentObjectName = UUID.randomUUID().toString() + "/";
		objectName = parentObjectName + "456/";
		descriptor = FolderDescriptor.of( "my-repo", "123/456" );
		resource = folderResource( descriptor, objectName );
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
	void descriptor() {
		assertThat( resource.getDescriptor() ).isEqualTo( descriptor );
	}

	@Test
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
	void equals() {
		assertThat( resource )
				.isEqualTo( resource )
				.isNotEqualTo( mock( Resource.class ) )
				.isEqualTo( folderResource( descriptor, "123/456/" ) )
				.isNotEqualTo( folderResource( FolderDescriptor.of( "1:2/" ), "123/456/" ) );
	}

	@Test
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
	void create() {
		assertThat( amazonS3.doesObjectExist( BUCKET_NAME, objectName ) ).isFalse();
		assertThat( resource.exists() ).isTrue();
		assertThat( resource.create() ).isTrue();
		assertThat( amazonS3.doesObjectExist( BUCKET_NAME, objectName ) ).isTrue();
		assertThat( resource.exists() ).isTrue();
		assertThat( resource.create() ).isFalse();
		assertThat( resource.exists() ).isTrue();
	}

	@Test
	void rootFolderExistsButCannotBeCreatedOrDeleted() {
		AmazonS3FolderResource rootFolder = folderResource( FolderDescriptor.rootFolder( "my-repo" ), "" );
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

		assertThat( amazonS3.doesObjectExist( BUCKET_NAME, objectName + "childFolder/childFolderInChildFolder/nestedFolder/" ) ).isTrue();
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
		assertThat( resource.exists() ).isTrue();
		assertThat( amazonS3.doesObjectExist( BUCKET_NAME, objectName ) ).isFalse();
		assertThat( resource.delete( false ) ).isTrue();

		assertThat( resource.exists() ).isTrue();
		AmazonS3Helper.createFolder( amazonS3, BUCKET_NAME, objectName );
		assertThat( amazonS3.doesObjectExist( BUCKET_NAME, objectName ) ).isTrue();

		createFileTree();
		assertThat( resource.delete( false ) ).isTrue();

		assertThat( resource.exists() ).isTrue();
		assertThat( amazonS3.doesObjectExist( BUCKET_NAME, objectName ) ).isFalse();
		assertThat( childFile.exists() ).isTrue();
	}

	@Test
	void deleteIfFolderNotEmpty() {
		createFileTree();

		assertThat( resource.exists() ).isTrue();
		assertThat( childFileInChildFolder.exists() ).isTrue();
		assertThat( resource.delete( false ) ).isTrue();

		childFileInChildFolder.resetObjectMetadata();
		assertThat( childFileInChildFolder.exists() ).isTrue();

		AmazonS3Helper.createFolder( amazonS3, BUCKET_NAME, objectName );
		assertThat( amazonS3.doesObjectExist( BUCKET_NAME, objectName ) ).isTrue();

		assertThat( resource.delete( true ) ).isTrue();

		childFileInChildFolder.resetObjectMetadata();
		assertThat( childFileInChildFolder.exists() ).isFalse();
		assertThat( amazonS3.doesObjectExist( BUCKET_NAME, objectName ) ).isFalse();
	}

	@Test
	void deleteChildren() {
		assertThat( resource.exists() ).isTrue();
		assertThat( resource.deleteChildren() ).isFalse();

		createFileTree();

		assertThat( resource.exists() ).isTrue();
		assertThat( childFileInChildFolder.exists() ).isTrue();

		assertThat( resource.deleteChildren() ).isTrue();
		childFileInChildFolder.resetObjectMetadata();
		assertThat( childFileInChildFolder.exists() ).isFalse();
		assertThat( resource.exists() ).isTrue();
	}

	@SneakyThrows
	private void createFileTree() {
		amazonS3.putObject( BUCKET_NAME, objectName + "childFile", "dummy file" );
		childFile = fileResource( FileDescriptor.of( descriptor.getRepositoryId(), descriptor.getFolderId(), "childFile" ), objectName + "childFile" );

		assertThat( childFile.exists() ).isTrue();

		AmazonS3Helper.createFolder( amazonS3, BUCKET_NAME, objectName + "childFolder/" );
		childFolder = folderResource( FolderDescriptor.of( descriptor.getRepositoryId(), descriptor.getFolderId() + "/childFolder" ),
		                              objectName + "childFolder/" );
		assertThat( childFolder.exists() ).isTrue();

		amazonS3.putObject( BUCKET_NAME, objectName + "childFolder/childFileInChildFolder", "" );
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

	private AmazonS3FolderResource folderResource( FolderDescriptor descriptor, String objectName ) {
		return new AmazonS3FolderResource( descriptor, amazonS3, BUCKET_NAME, objectName, TASK_EXECUTOR );
	}

	private AmazonS3FileResource fileResource( FileDescriptor descriptor, String objectName ) {
		return new AmazonS3FileResource( descriptor, amazonS3, BUCKET_NAME, objectName, TASK_EXECUTOR );
	}
}
