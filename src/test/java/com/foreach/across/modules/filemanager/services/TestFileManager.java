package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestFileManager
{
	private FileManagerImpl fileManager;

	private FileRepository repository;
	private FileRepositoryFactory factory;

	@Before
	public void reset() {
		fileManager = new FileManagerImpl();
		repository = mock( FileRepository.class );
		factory = mock( FileRepositoryFactory.class );

		fileManager.setFileRepositoryFactory( factory );
	}

	@Test
	public void defaultRepositoryIsUsedIfExists() {
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
	}

	@Test
	public void correctRepositoryIsUsed() {
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
		File mockFile = mock( File.class );
		when( one.getAsFile( renameA ) ).thenReturn( mockFile );
		InputStream streamA = mock( InputStream.class );
		when( one.getInputStream( renameA ) ).thenReturn( streamA );
		FileDescriptor tempDescriptor = new FileDescriptor( "two", null, "temp" );
		when( two.save( streamA ) ).thenReturn( tempDescriptor );
		when( two.move( tempDescriptor, renameC ) ).thenReturn( true );
		when( one.delete( renameA ) ).thenReturn( true );
		fileManager.move( renameA, renameC );
		verify( two ).save( streamA );
		verify( two ).move( tempDescriptor, renameC );
		verify( one ).delete( renameA );
	}

	@Test
	public void factoryIsNotCalledForExistenceCheck() {
		assertFalse( fileManager.repositoryExists( UUID.randomUUID().toString() ) );

		verify( factory, never() ).create( anyString() );
	}

	@Test
	public void repositoryExistsIfRegistered() {
		when( repository.getRepositoryId() ).thenReturn( "myrepo" );

		fileManager.registerRepository( repository );

		assertTrue( fileManager.repositoryExists( "myrepo" ) );
		assertNotNull( fileManager.getRepository( "myrepo" ) );
		assertSame( repository, delegate( "myrepo" ) );
	}

	@Test
	public void factoryIsCalledIfRepositoryIsNotRegistered() {
		FileRepository fetched = fileManager.getRepository( "non-existing-one" );

		assertNull( fetched );
		verify( factory ).create( "non-existing-one" );

		when( factory.create( "creatable" ) ).thenReturn( repository );
		fetched = fileManager.getRepository( "creatable" );

		assertNotNull( fetched );
		assertSame( repository, delegate( "creatable" ) );
	}

	@Test
	public void updatingTheImplementationOfARepository() {
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

	private FileRepository delegate( String repositoryId ) {
		return delegate( fileManager.getRepository( repositoryId ) );
	}

	private FileRepository delegate( FileRepository repository ) {
		return ( (FileRepositoryDelegate) repository ).getActualImplementation();
	}

}
