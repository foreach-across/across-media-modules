package com.foreach.across.modules.filemanager.business;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author Arne Vandamme
 * @since 1.4.0
 */
class TestFolderDescriptor
{
	@Test
	void properties() {
		FolderDescriptor descriptor = FolderDescriptor.of( "repository", "folder/one" );
		assertThat( descriptor ).isNotNull();
		assertThat( descriptor.getRepositoryId() ).isEqualTo( "repository" );
		assertThat( descriptor.getFolderId() ).isEqualTo( "folder/one" );
		assertThat( descriptor.getUri() ).isEqualTo( "repository:folder/one/" ).isEqualTo( descriptor.toString() );
		assertThat( descriptor.toResourceURI().toString() ).isEqualTo( "axfs://repository:folder/one/" );

		assertThat( descriptor )
				.isEqualTo( FolderDescriptor.of( "repository", "folder/one/" ) )
				.isEqualTo( FolderDescriptor.of( "repository", "/folder/one" ) )
				.isEqualTo( FolderDescriptor.of( "repository", "folder\\one" ) )
				.isNotEqualTo( FolderDescriptor.of( "repository", "folder/two" ) );

		assertThat( descriptor.hashCode() )
				.isEqualTo( FolderDescriptor.of( "repository", "folder/one/" ).hashCode() );
	}

	@Test
	void rootFolder() {
		FolderDescriptor root = FolderDescriptor.rootFolder( "someRepository" );
		assertThat( root.getFolderId() ).isNull();
		assertThat( root.toString() ).isEqualTo( "someRepository:/" );
		assertThat( root.toResourceURI().toString() ).isEqualTo( "axfs://someRepository:/" );

		assertThat( root )
				.isNotNull()
				.isEqualTo( FolderDescriptor.of( "someRepository", null ) )
				.isEqualTo( FolderDescriptor.of( "someRepository", "/" ) )
				.isEqualTo( FolderDescriptor.of( "someRepository", "" ) )
				.isEqualTo( FolderDescriptor.of( "someRepository", "\\" ) );
	}

	@Test
	void fromUri() {
		assertThat( FolderDescriptor.of( "my-repo:/my folder/" ) ).isEqualTo( FolderDescriptor.of( "my-repo", "my folder" ) );
		assertThat( FolderDescriptor.of( "my-repo:my folder/" ) ).isEqualTo( FolderDescriptor.of( "my-repo", "my folder" ) );
		assertThat( FolderDescriptor.of( "my-repo:/my/folder/" ) ).isEqualTo( FolderDescriptor.of( "my-repo", "my/folder" ) );
		assertThat( FolderDescriptor.of( "my-repo:my/folder/" ) ).isEqualTo( FolderDescriptor.of( "my-repo", "my/folder" ) );
		assertThat( FolderDescriptor.of( "axfs://my-repo:my folder/" ) ).isEqualTo( FolderDescriptor.of( "my-repo", "my folder" ) );
		assertThat( FolderDescriptor.of( "axfs://my-repo:my/folder/" ) ).isEqualTo( FolderDescriptor.of( "my-repo", "my/folder" ) );
		assertThat( FolderDescriptor.of( "axfs://my-repo:/" ) ).isEqualTo( FolderDescriptor.rootFolder( "my-repo" ) );
		assertThat( FolderDescriptor.of( "my-repo:/" ) ).isEqualTo( FolderDescriptor.rootFolder( "my-repo" ) );
	}

	@Test
	void fromUriRequiresTrailingSlash() {
		assertThatExceptionOfType( IllegalArgumentException.class )
				.isThrownBy( () -> FolderDescriptor.of( "my-repo:" ) );
		assertThatExceptionOfType( IllegalArgumentException.class )
				.isThrownBy( () -> FolderDescriptor.of( "my-repo:my/folder" ) );
		assertThatExceptionOfType( IllegalArgumentException.class )
				.isThrownBy( () -> FolderDescriptor.of( "axfs://my-repo:my/folder" ) );
	}

	@Test
	void repositoryIdCannotHaveColon() {
		assertThatExceptionOfType( IllegalArgumentException.class )
				.isThrownBy( () -> FolderDescriptor.of( "repo:sitory", "some/folder" ) );
	}

	@Test
	@DisplayName("Folder id should not have colon")
	void folderIdCannotHaveColon() {
		assertThatExceptionOfType( IllegalArgumentException.class )
				.isThrownBy( () -> FolderDescriptor.of( "repository", "some:/folder" ) );
	}

	@Test
	@DisplayName("Folder id should not have path navigation")
	void folderIdCannotHaveDoubleDot() {
		assertThatExceptionOfType( IllegalArgumentException.class )
				.isThrownBy( () -> FolderDescriptor.of( "repository", "some/../folder" ) );
		assertThatExceptionOfType( IllegalArgumentException.class )
				.isThrownBy( () -> FolderDescriptor.of( "repository", "some/../folder" ) );
		assertThatExceptionOfType( IllegalArgumentException.class )
				.isThrownBy( () -> FolderDescriptor.of( "repository", "../somefolder" ) );
		assertThatExceptionOfType( IllegalArgumentException.class )
				.isThrownBy( () -> FolderDescriptor.of( "repository", "./somefolder" ) );
		assertThatExceptionOfType( IllegalArgumentException.class )
				.isThrownBy( () -> FolderDescriptor.of( "repository", "somefolder/." ) );
		assertThatExceptionOfType( IllegalArgumentException.class )
				.isThrownBy( () -> FolderDescriptor.of( "repository", "somefolder/.." ) );

		assertThat( FolderDescriptor.of( "repository", "..folder" ) ).isNotNull();
		assertThat( FolderDescriptor.of( "repository", ".folder" ) ).isNotNull();
	}

	@Test
	@DisplayName("Folder id should not be all whitespace")
	void folderIdCannotBeAllWhitespace() {
		assertThatExceptionOfType( IllegalArgumentException.class )
				.isThrownBy( () -> FolderDescriptor.of( "repository", "\t" ) );
		assertThatExceptionOfType( IllegalArgumentException.class )
				.isThrownBy( () -> FolderDescriptor.of( "repository", "  " ) );
	}

	@Test
	void rootFolderHasNoParent() {
		assertThat( FolderDescriptor.rootFolder( "my-repo" ).getParentFolderDescriptor() ).isEmpty();
	}

	@Test
	void parentFolderDescriptor() {
		FolderDescriptor descriptor = FolderDescriptor.of( "my-repo", "one/two/three" );

		assertThat( descriptor.getParentFolderDescriptor() )
				.contains( FolderDescriptor.of( "my-repo", "one/two" ) )
				.hasValueSatisfying(
						two ->
								assertThat( two.getParentFolderDescriptor() )
										.contains( FolderDescriptor.of( "my-repo", "one" ) )
										.hasValueSatisfying(
												one -> assertThat( one.getParentFolderDescriptor() )
														.contains( FolderDescriptor.rootFolder( "my-repo" ) )

										)
				);
	}

	@Test
	void childFolderDescriptor() {
		FolderDescriptor root = FolderDescriptor.rootFolder( "my-repo" );
		assertThat( root.createFolderDescriptor( "/" ) )
				.isEqualTo( root )
				.isEqualTo( root.createFolderDescriptor( "" ) );

		assertThat( root.createFolderDescriptor( "123" ) )
				.isEqualTo( FolderDescriptor.of( "my-repo", "123" ) )
				.isEqualTo( root.createFolderDescriptor( "/123" ) )
				.isEqualTo( root.createFolderDescriptor( "/123/" ) )
				.isEqualTo( root.createFolderDescriptor( "123/" ) );

		assertThat( root.createFolderDescriptor( "123" ).createFolderDescriptor( "456" ) )
				.isEqualTo( FolderDescriptor.of( "my-repo", "123/456" ) )
				.isEqualTo( root.createFolderDescriptor( "123" ).createFolderDescriptor( "/456" ) )
				.isEqualTo( root.createFolderDescriptor( "123" ).createFolderDescriptor( "/456/" ) )
				.isEqualTo( root.createFolderDescriptor( "123" ).createFolderDescriptor( "456/" ) )
				.isEqualTo( root.createFolderDescriptor( "/123/456" ) )
				.isEqualTo( root.createFolderDescriptor( "/123/456/" ) )
				.isEqualTo( root.createFolderDescriptor( "123/456" ) );
	}

	@Test
	void childFileDescriptor() {
		FolderDescriptor root = FolderDescriptor.rootFolder( "my-repo" );

		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> root.createFileDescriptor( "" ) );
		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> root.createFileDescriptor( "/" ) );
		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> root.createFileDescriptor( "123/" ) );
		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> root.createFolderDescriptor( "123" ).createFileDescriptor( "myfile/" ) );
		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> root.createFolderDescriptor( "123" ).createFileDescriptor( "/" ) );
		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> root.createFolderDescriptor( "123" ).createFileDescriptor( "" ) );

		assertThat( root.createFileDescriptor( "myfile.txt" ) )
				.isEqualTo( FileDescriptor.of( "my-repo", "myfile.txt" ) )
				.isEqualTo( root.createFileDescriptor( "/myfile.txt" ) );

		assertThat( root.createFileDescriptor( "123/456/myfile.txt" ) )
				.isEqualTo( FileDescriptor.of( "my-repo", "123/456", "myfile.txt" ) )
				.isEqualTo( root.createFileDescriptor( "/123/456/myfile.txt" ) )
				.isEqualTo( root.createFolderDescriptor( "123" ).createFileDescriptor( "/456/myfile.txt" ) )
				.isEqualTo( root.createFolderDescriptor( "123" ).createFileDescriptor( "456/myfile.txt" ) )
				.isEqualTo( root.createFolderDescriptor( "123/456" ).createFileDescriptor( "myfile.txt" ) )
				.isEqualTo( root.createFolderDescriptor( "123/456" ).createFileDescriptor( "/myfile.txt" ) );
	}
}
