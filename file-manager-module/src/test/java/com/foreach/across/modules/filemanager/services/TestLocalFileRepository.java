/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import com.foreach.across.modules.filemanager.business.FolderDescriptor;
import com.foreach.across.modules.filemanager.business.FolderResource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestLocalFileRepository extends BaseFileRepositoryTest
{
	public FileRepository createRepository() {
		return LocalFileRepository.builder().repositoryId( "default" ).rootFolder( rootFolder ).build();
	}

	@Test
	@SneakyThrows
	void folderIsCreated() {
		FileResource file = fileRepository.getFileResource( FileDescriptor.of( "default:aa/bb/cc:myfile" ) );
		assertThat( fileRepository.getFolderResource( FolderDescriptor.of( "default:aa/" ) ).exists() ).isFalse();
		assertThat( fileRepository.getFolderResource( FolderDescriptor.of( "default:aa/bb/" ) ).exists() ).isFalse();
		assertThat( fileRepository.getFolderResource( FolderDescriptor.of( "default:aa/bb/cc/" ) ).exists() ).isFalse();

		file.copyFrom( RES_TEXTFILE );

		assertThat( file.exists() ).isTrue();

		FolderResource cc = file.getFolderResource();
		assertThat( cc.getDescriptor() ).isEqualTo( FolderDescriptor.of( "default:aa/bb/cc/" ) );
		assertThat( cc.listResources( false ) ).containsExactly( file );
		assertThat( cc.getFolderName() ).isEqualTo( "cc" );

		FolderResource bb = cc.getParentFolderResource().orElseThrow( AssertionError::new );
		assertThat( bb.getDescriptor() ).isEqualTo( FolderDescriptor.of( "default:aa/bb/" ) );
		assertThat( bb.listResources( false ) ).containsExactly( cc );
		assertThat( bb.getFolderName() ).isEqualTo( "bb" );

		FolderResource aa = bb.getParentFolderResource().orElseThrow( AssertionError::new );
		assertThat( aa.getDescriptor() ).isEqualTo( FolderDescriptor.of( "default:aa/" ) );
		assertThat( aa.listResources( false ) ).containsExactly( bb );
		assertThat( aa.getFolderName() ).isEqualTo( "aa" );
		assertThat( aa.listResources( true ) ).containsExactlyInAnyOrder( bb, cc, file );

		FolderResource root = aa.getParentFolderResource().orElseThrow( AssertionError::new );
		assertThat( root.getDescriptor() ).isEqualTo( FolderDescriptor.rootFolder( "default" ) );
		assertThat( root.listResources( false ) ).contains( aa );
		assertThat( root.getFolderName() ).isEmpty();

		assertThat( root.getParentFolderResource() ).isEmpty();
	}

	@Test
	@SneakyThrows
	void createFolderAndFilesInFolder() {
		FolderResource folder = fileRepository.getFolderResource( FolderDescriptor.of( "default:dd/cc/" ) );
		assertThat( folder.exists() ).isFalse();
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
}