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
import com.foreach.across.modules.filemanager.business.FolderResource;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestFileManager
{
	@Mock
	private FileRepository repository;

	@Mock
	private FileRepositoryFactory factory;

	@InjectMocks
	private FileManagerImpl fileManager = new FileManagerImpl();

	@BeforeEach
	void reset() {
		fileManager.setFileRepositoryFactory( factory );
	}

	@Test
	@SneakyThrows
	void defaultRepositoryIsUsedIfExists() {
		when( repository.getRepositoryId() ).thenReturn( FileManager.DEFAULT_REPOSITORY );
		fileManager.registerRepository( repository );

		fileManager.createFile();
		verify( repository ).createFile();

		File file = new File( "" );
		fileManager.moveInto( file );
		verify( repository ).moveInto( file );

		fileManager.save( file );
		verify( repository ).save( file );

		InputStream inputStream = mock( InputStream.class );
		fileManager.save( inputStream );
		verify( repository ).save( inputStream );

		repository.createFileResource();
		verify( repository ).createFileResource();

		fileManager.createFileResource( true );
		verify( repository ).createFileResource( true );

		InputStream is = mock( InputStream.class );
		fileManager.createFileResource( is );
		verify( repository ).createFileResource( is );

		fileManager.createFileResource( file, true );
		verify( repository ).createFileResource( file, true );

		FileDescriptor fd = FileDescriptor.of( "1:2:3" );
		when( repository.generateFileDescriptor() ).thenReturn( fd );
		Assertions.assertThat( fileManager.generateFileDescriptor() ).isSameAs( fd );
	}

	@Test
	void correctRepositoryIsUsed() {
		FileRepository one = mock( FileRepository.class );
		when( one.getRepositoryId() ).thenReturn( "one" );

		FileRepository two = mock( FileRepository.class );
		when( two.getRepositoryId() ).thenReturn( "two" );

		fileManager.registerRepository( one );
		fileManager.registerRepository( two );

		fileManager.createFile( "one" );
		verify( one ).createFile();
		verify( two, never() ).createFile();

		fileManager.createFile( "two" );
		verify( two ).createFile();

		File fileOne = new File( "a" );
		File fileTwo = new File( "b" );
		fileManager.moveInto( "one", fileOne );
		fileManager.moveInto( "two", fileTwo );
		verify( one ).moveInto( fileOne );
		verify( two ).moveInto( fileTwo );

		fileManager.save( "one", fileOne );
		fileManager.save( "two", fileTwo );
		verify( one ).save( fileOne );
		verify( two ).save( fileTwo );

		InputStream streamOne = mock( InputStream.class );
		InputStream streamTwo = mock( InputStream.class );
		fileManager.save( "one", streamOne );
		fileManager.save( "two", streamTwo );
		verify( one ).save( streamOne );
		verify( two ).save( streamTwo );

		FileDescriptor descriptorOne = new FileDescriptor( "one", null, "a" );
		FileDescriptor descriptorTwo = new FileDescriptor( "two", null, "b" );

		fileManager.exists( descriptorOne );
		fileManager.exists( descriptorTwo );
		verify( one ).exists( descriptorOne );
		verify( two ).exists( descriptorTwo );

		fileManager.delete( descriptorOne );
		fileManager.delete( descriptorTwo );
		verify( one ).delete( descriptorOne );
		verify( two ).delete( descriptorTwo );

		fileManager.getOutputStream( descriptorOne );
		fileManager.getOutputStream( descriptorTwo );
		verify( one ).getOutputStream( descriptorOne );
		verify( two ).getOutputStream( descriptorTwo );

		fileManager.getInputStream( descriptorOne );
		fileManager.getInputStream( descriptorTwo );
		verify( one ).getInputStream( descriptorOne );
		verify( two ).getInputStream( descriptorTwo );

		fileManager.getAsFile( descriptorOne );
		fileManager.getAsFile( descriptorTwo );
		verify( one ).getAsFile( descriptorOne );
		verify( two ).getAsFile( descriptorTwo );

		FileDescriptor renameA = new FileDescriptor( "one", null, "c" );
		FileDescriptor renameB = new FileDescriptor( "two", null, "d" );
		fileManager.move( descriptorOne, renameA );
		fileManager.move( descriptorTwo, renameB );
		verify( one ).move( descriptorOne, renameA );
		verify( two ).move( descriptorTwo, renameB );

		FileDescriptor renameC = new FileDescriptor( "two", null, "e" );
		InputStream streamA = mock( InputStream.class );
		when( one.getInputStream( renameA ) ).thenReturn( streamA );
		when( one.delete( renameA ) ).thenReturn( true );
		fileManager.move( renameA, renameC );
		verify( two ).save( renameC, streamA, true );
		verify( one ).delete( renameA );

		FileDescriptor fd = FileDescriptor.of( "one", "file" );
		FileResource fr = mock( FileResource.class );
		when( one.getFileResource( fd ) ).thenReturn( fr );
		Assertions.assertThat( fileManager.getFileResource( fd ) ).isSameAs( fr );
	}

	@Test
	void factoryIsNotCalledForExistenceCheck() {
		assertFalse( fileManager.repositoryExists( UUID.randomUUID().toString() ) );

		verify( factory, never() ).create( anyString() );
	}

	@Test
	void repositoryExistsIfRegistered() {
		when( repository.getRepositoryId() ).thenReturn( "myrepo" );

		fileManager.registerRepository( repository );

		assertTrue( fileManager.repositoryExists( "myrepo" ) );
		assertNotNull( fileManager.getRepository( "myrepo" ) );
		assertSame( repository, delegate( "myrepo" ) );
	}

	@Test
	void factoryIsCalledIfRepositoryIsNotRegistered() {
		FileRepository fetched = fileManager.getRepository( "non-existing-one" );

		assertNull( fetched );
		verify( factory ).create( "non-existing-one" );

		when( repository.getRepositoryId() ).thenReturn( "creatable" );
		when( factory.create( "creatable" ) ).thenReturn( repository );
		fetched = fileManager.getRepository( "creatable" );

		assertNotNull( fetched );
		assertSame( repository, delegate( "creatable" ) );
	}

	@Test
	void exceptionThrownIfRepositoryNotRegisteredAndCannotBeCreated() {
		fileManager.setFileRepositoryFactory( null );

		assertThatExceptionOfType( IllegalArgumentException.class )
				.isThrownBy( () -> fileManager.getRepository( "non-existing-one" ) );
	}

	@Test
	void updatingTheImplementationOfARepository() {
		when( repository.getRepositoryId() ).thenReturn( "replace" );

		FileRepository other = mock( FileRepository.class );
		when( other.getRepositoryId() ).thenReturn( "replace" );

		assertNotSame( repository, other );

		FileRepository delegate = fileManager.registerRepository( repository );
		FileRepository fetched = fileManager.getRepository( "replace" );

		assertSame( delegate, fetched );
		assertSame( repository, delegate( fetched ) );

		delegate = fileManager.registerRepository( other );
		assertSame( delegate, fetched );
		assertSame( other, delegate( fetched ) );
	}

	@Test
	void fileManagerAwareMethods() {
		AbstractFileRepository repository = mock( AbstractFileRepository.class );
		when( repository.getRepositoryId() ).thenReturn( "123" );
		fileManager.registerRepository( repository );
		verify( repository ).setFileManager( fileManager );
		verify( repository, never() ).shutdown();

		fileManager.shutdown();
		verify( repository ).shutdown();

		Assertions.assertThat( fileManager.listRepositories() ).isEmpty();
	}

	@Test
	@SuppressWarnings("unchecked")
	void findResourcesSearchesInDefaultRepositoryIfNoRepositoryId() {
		when( repository.getRepositoryId() ).thenReturn( FileManager.DEFAULT_REPOSITORY );
		fileManager.registerRepository( repository );

		FileResource file = mock( FileResource.class );
		FolderResource folder = mock( FolderResource.class );
		when( repository.findResources( any() ) ).thenReturn( Collections.singleton( file ) );
		when( repository.findResources( any(), any() ) ).thenReturn( Collections.singleton( folder ) );
		when( repository.findFiles( any() ) ).thenReturn( Collections.singleton( file ) );

		Assertions.assertThat( fileManager.findResources( "**" ) ).containsExactly( file );
		verify( repository ).findResources( "**" );
		Assertions.assertThat( fileManager.findResources( "/my/file.txt" ) ).containsExactly( file );
		verify( repository ).findResources( "/my/file.txt" );

		Assertions.assertThat( fileManager.findResources( "*", FolderResource.class ) ).containsExactly( folder );
		verify( repository ).findResources( "*", FolderResource.class );
		Assertions.assertThat( fileManager.findResources( "/my/file2.txt", FolderResource.class ) ).containsExactly( folder );
		verify( repository ).findResources( "/my/file2.txt", FolderResource.class );

		Assertions.assertThat( fileManager.findFiles( "*/*" ) ).containsExactly( file );
		verify( repository ).findFiles( "*/*" );
		Assertions.assertThat( fileManager.findFiles( "/my/file3.txt" ) ).containsExactly( file );
		verify( repository ).findFiles( "/my/file3.txt" );
	}

	@Test
	void findRequiresRepositoryIdIfResourceProtocolPresent() {
		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> fileManager.findResources( "axfs://**" ) );
		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> fileManager.findResources( "axfs://my/file.txt" ) );
	}

	@Test
	void findResourcesInMatchingRepositories() {
		when( repository.getRepositoryId() ).thenReturn( "repository-1" );
		fileManager.registerRepository( repository );

		FileRepository otherRepository = mock( FileRepository.class );
		when( otherRepository.getRepositoryId() ).thenReturn( "repository-2" );
		fileManager.registerRepository( otherRepository );

		FileResource one = mock( FileResource.class );
		FileResource two = mock( FileResource.class );

		when( repository.findResources( any() ) ).thenReturn( Collections.singleton( one ) );
		when( otherRepository.findResources( any() ) ).thenReturn( Collections.singleton( two ) );

		Assertions.assertThat( fileManager.findResources( "repository-1:*" ) ).containsExactly( one );
		verify( repository ).findResources( "*" );
		verify( otherRepository, never() ).findResources( "*" );
		Assertions.assertThat( fileManager.findResources( "axfs://repository-1:*" ) ).containsExactly( one );

		Assertions.assertThat( fileManager.findResources( "repository-2:*" ) ).containsExactly( two );
		Assertions.assertThat( fileManager.findResources( "unknown:*" ) ).isEmpty();

		Assertions.assertThat( fileManager.findResources( "*:**/*" ) ).containsExactly( one, two );
		verify( repository ).findResources( "**/*" );
		verify( otherRepository ).findResources( "**/*" );
		Assertions.assertThat( fileManager.findResources( "axfs://*:**/*" ) ).containsExactly( one, two );

		Assertions.assertThat( fileManager.findResources( "repository-*:myfolder:file.txt" ) ).containsExactly( one, two );
		verify( repository ).findResources( "myfolder/file.txt" );
		verify( otherRepository ).findResources( "myfolder/file.txt" );

		when( repository.findResources( any(), any() ) ).thenReturn( Collections.singleton( one ) );
		Assertions.assertThat( fileManager.findResources( "repository-1:*", FileResource.class ) ).containsExactly( one );
		verify( repository ).findResources( "*", FileResource.class );

		when( otherRepository.findFiles( any() ) ).thenReturn( Collections.singleton( two ) );
		Assertions.assertThat( fileManager.findFiles( "repository-2:*" ) ).containsExactly( two );
		verify( otherRepository ).findFiles( "*" );
	}

	private FileRepository delegate( String repositoryId ) {
		return delegate( fileManager.getRepository( repositoryId ) );
	}

	private FileRepository delegate( FileRepository repository ) {
		return ( (FileRepositoryDelegate) repository ).getActualImplementation();
	}

}
