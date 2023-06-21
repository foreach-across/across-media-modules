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
import org.apache.commons.net.ftp.FTPClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.ftp.session.FtpRemoteFileTemplate;
import org.springframework.util.StreamUtils;
import utils.FtpContainer;

import java.io.OutputStream;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;
import static utils.FtpContainer.TEST_PORT;

class TestCachingFileRepositoryLocalAndFtp extends BaseFileRepositoryTest
{
	private SpringIntegrationFtpFileRepository remoteRepository;
	private LocalFileRepository cacheRepository;

	private static final FtpContainer ftpContainer = new FtpContainer();
	private static FtpRemoteFileTemplate template;

	@Override
	FileRepository createRepository() {
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

		FileManagerImpl fileManager = new FileManagerImpl();

		remoteRepository = SpringIntegrationFtpFileRepository.builder()
		                                                     .repositoryId( "ftp-repo" )
		                                                     .remoteFileTemplate( template )
		                                                     .build();
		remoteRepository.setFileManager( fileManager );

		CachingFileRepository repositoryToTest = CachingFileRepository.withGeneratedFileDescriptor()
		                                                              .targetFileRepository( remoteRepository )
		                                                              .cacheRepositoryId( "cache" )
		                                                              .build();
		repositoryToTest.setFileManager( fileManager );

		cacheRepository = LocalFileRepository.builder().repositoryId( "cache" ).rootFolder( rootFolder ).build();
		fileManager.registerRepository( cacheRepository );

		return repositoryToTest;
	}

	@BeforeAll
	@SneakyThrows
	static void init() {
		ftpContainer.start();
	}

	@AfterAll
	static void tearDown() {
		ftpContainer.stop();
	}

	@AfterEach
	void cleanUp() {
		FolderResource rootFolder = remoteRepository.getRootFolderResource();
		if ( !rootFolder.findResources( "*" ).isEmpty() ) {
			assertThat( rootFolder.deleteChildren() ).isTrue();
		}
	}

	@Test
	@SneakyThrows
	void cacheResourceMayBeRemoved() {
		CachedFileResource resource = (CachedFileResource) fileRepository.createFileResource();
		resource.copyFrom( RES_TEXTFILE );

		LocalFileResource cache = (LocalFileResource) resource.getCache();
		SpringIntegrationFtpFileResource target = (SpringIntegrationFtpFileResource) resource.getTarget();

		assertThat( readResource( resource ) ).isEqualTo( "some dummy text" );
		assertThat( readResource( target ) ).isEqualTo( "some dummy text" );
		assertThat( readResource( cache ) ).isEqualTo( "some dummy text" );
		assertThat( resource.contentLength() )
				.isEqualTo( target.contentLength() )
				.isEqualTo( resource.getCache().contentLength() );

		assertThat( cache.getTargetFile().delete() ).isTrue();

		assertThat( cache.exists() ).isFalse();
		assertThat( readResource( target ) ).isEqualTo( "some dummy text" );
		assertThat( cache.exists() ).isFalse();
		assertThat( readResource( resource ) ).isEqualTo( "some dummy text" );
		assertThat( cache.exists() ).isTrue();
		assertThat( readResource( cache ) ).isEqualTo( "some dummy text" );

		resource.flushCache();
		assertThat( cache.exists() ).isFalse();
		assertThat( readResource( resource ) ).isEqualTo( "some dummy text" );
		assertThat( readResource( cache ) ).isEqualTo( "some dummy text" );

		target.delete();
		assertThat( readResource( cache ) ).isEqualTo( "some dummy text" );
		assertThat( readResource( resource ) ).isEqualTo( "some dummy text" );
		assertThat( resource.exists() ).isTrue();
		assertThat( cache.exists() ).isTrue();
		assertThat( target.exists() ).isFalse();

		resource.flushCache();
		assertThat( resource.exists() ).isFalse();
		assertThat( cache.exists() ).isFalse();
		assertThat( target.exists() ).isFalse();
	}

	@Test
	@SneakyThrows
	void updatesGoToCacheAndTargetImmediately() {
		CachedFileResource resource = (CachedFileResource) fileRepository.createFileResource();
		resource.copyFrom( RES_TEXTFILE );

		LocalFileResource cache = (LocalFileResource) resource.getCache();
		SpringIntegrationFtpFileResource target = (SpringIntegrationFtpFileResource) resource.getTarget();

		try (OutputStream os = resource.getOutputStream()) {
			StreamUtils.copy( "updated data", Charset.defaultCharset(), os );
		}

		assertThat( readResource( resource ) ).isEqualTo( "updated data" );
		assertThat( readResource( cache ) ).isEqualTo( "updated data" );
		assertThat( readResource( target ) ).isEqualTo( "updated data" );
	}

	@Test
	@SneakyThrows
	void deleteRemovesBothOriginalAndTarget() {
		CachedFileResource resource = (CachedFileResource) fileRepository.createFileResource();
		resource.copyFrom( RES_TEXTFILE );

		LocalFileResource cache = (LocalFileResource) resource.getCache();
		SpringIntegrationFtpFileResource target = (SpringIntegrationFtpFileResource) resource.getTarget();

		resource.delete();
		assertThat( resource.exists() ).isFalse();
		assertThat( cache.exists() ).isFalse();
		assertThat( target.exists() ).isFalse();
	}

	@Test
	@SneakyThrows
	void individualFileResources() {
		CachedFileResource resource = (CachedFileResource) fileRepository.createFileResource();
		resource.copyFrom( RES_TEXTFILE );

		LocalFileResource cache = (LocalFileResource) resource.getCache();
		SpringIntegrationFtpFileResource target = (SpringIntegrationFtpFileResource) resource.getTarget();

		FileResource remote = remoteRepository.getFileResource( target.getDescriptor() );
		assertThat( remote.exists() ).isTrue();
		assertThat( readResource( remote ) ).isEqualTo( readResource( target ) ).isEqualTo( "some dummy text" );

		FileResource local = cacheRepository.getFileResource( cache.getDescriptor() );
		assertThat( local.exists() ).isTrue();
		assertThat( readResource( local ) ).isEqualTo( readResource( cache ) ).isEqualTo( "some dummy text" );
	}

	@Test
	@SneakyThrows
	void folderIsCreated() {
		FileResource file = fileRepository.getFileResource( FileDescriptor.of( fileRepository.getRepositoryId() + ":aa/bb/cc:myfile" ) );
		assertThat( fileRepository.getFolderResource( FolderDescriptor.of( fileRepository.getRepositoryId() + ":aa/" ) ).exists() ).isFalse();
		assertThat( fileRepository.getFolderResource( FolderDescriptor.of( fileRepository.getRepositoryId() + ":aa/bb/" ) ).exists() ).isFalse();
		assertThat( fileRepository.getFolderResource( FolderDescriptor.of( fileRepository.getRepositoryId() + ":aa/bb/cc/" ) ).exists() ).isFalse();

		file.copyFrom( RES_TEXTFILE );

		assertThat( fileRepository.getFolderResource( FolderDescriptor.of( fileRepository.getRepositoryId() + ":aa/" ) ).exists() ).isTrue();
		assertThat( fileRepository.getFolderResource( FolderDescriptor.of( fileRepository.getRepositoryId() + ":aa/bb/" ) ).exists() ).isTrue();
		assertThat( fileRepository.getFolderResource( FolderDescriptor.of( fileRepository.getRepositoryId() + ":aa/bb/cc/" ) ).exists() ).isTrue();
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