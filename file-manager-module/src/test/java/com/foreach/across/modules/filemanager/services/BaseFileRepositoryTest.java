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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Base class with tests scenarios that should run on all implementations of FileRepository
 */
public abstract class BaseFileRepositoryTest
{
	protected static final Resource RES_TEXTFILE = new ClassPathResource( "textfile.txt" );

	protected static final String TEMP_DIR = System.getProperty( "java.io.tmpdir" );
	protected static final String ROOT_DIR = Paths.get( TEMP_DIR, UUID.randomUUID().toString() ).toString();

	private static final File FILE_ONE = new File( TEMP_DIR, UUID.randomUUID().toString() );

	protected FileRepository fileRepository;

	@Before
	public void create() throws IOException {
		cleanup();

		assertFalse( FILE_ONE.exists() );

		try (FileWriter w = new FileWriter( FILE_ONE )) {
			IOUtils.copy( RES_TEXTFILE.getInputStream(), w );
			w.flush();
		}
		createRepository();
	}

	abstract void createRepository();

	@After
	public void cleanup() {
		try {
			if ( FILE_ONE.exists() ) {
				FILE_ONE.delete();
			}

			FileUtils.deleteDirectory( new File( ROOT_DIR ) );
		}
		catch ( Exception e ) {
			System.err.println( "Unit test could not cleanup files nicely" );
		}
	}

	@Test
	public void moveIntoDeletesTheOriginalFile() {
		assertTrue( FILE_ONE.exists() );

		FileDescriptor descriptor = fileRepository.moveInto( FILE_ONE );
		assertNotNull( descriptor );
		assertTrue( fileRepository.exists( descriptor ) );

		assertFalse( FILE_ONE.exists() );

		assertTrue( fileRepository.delete( descriptor ) );
		assertFalse( fileRepository.exists( descriptor ) );
	}

	@Test
	public void savingFileAlwaysCreatesANewFile() {
		FileDescriptor descriptorOne = fileRepository.save( FILE_ONE );
		FileDescriptor descriptorTwo = fileRepository.save( FILE_ONE );

		assertNotEquals( descriptorOne, descriptorTwo );

		File one = fileRepository.getAsFile( descriptorOne );
		File two = fileRepository.getAsFile( descriptorTwo );

		assertNotEquals( one, two );
		assertTrue( fileRepository.exists( descriptorOne ) );
		assertTrue( fileRepository.exists( descriptorTwo ) );

		fileRepository.delete( descriptorOne );
		assertFalse( fileRepository.exists( descriptorOne ) );
		assertTrue( fileRepository.exists( descriptorTwo ) );

	}

	@Test
	public void savingInputStreamAlwaysCreatesANewFile() throws IOException {
		FileDescriptor descriptorOne = fileRepository.save( RES_TEXTFILE.getInputStream() );
		FileDescriptor descriptorTwo = fileRepository.save( RES_TEXTFILE.getInputStream() );

		assertNotEquals( descriptorOne, descriptorTwo );

		File one = fileRepository.getAsFile( descriptorOne );
		File two = fileRepository.getAsFile( descriptorTwo );

		assertNotEquals( one, two );
		assertTrue( fileRepository.exists( descriptorOne ) );
		assertTrue( fileRepository.exists( descriptorTwo ) );

		fileRepository.delete( descriptorOne );
		assertFalse( fileRepository.exists( descriptorOne ) );
		assertTrue( fileRepository.exists( descriptorTwo ) );

	}

	@Test
	public void savingWithTargetFileDescriptorOverwritesExistingFile() throws IOException {
		FileDescriptor target = FileDescriptor.of( fileRepository.getRepositoryId(), UUID.randomUUID().toString() );
		fileRepository.save( target, RES_TEXTFILE.getInputStream(), true );

		assertTrue( fileRepository.exists( target ) );

		File saved = fileRepository.getAsFile( target );
		File original = RES_TEXTFILE.getFile();
		assertArrayEquals( Files.readAllBytes( saved.toPath() ), Files.readAllBytes( original.toPath() ) );

		// execute save again to verify no error is thrown
		fileRepository.save( target, RES_TEXTFILE.getInputStream(), true );
		fileRepository.delete( target );
		assertFalse( fileRepository.exists( target ) );
	}

	@Test(expected = IllegalArgumentException.class)
	public void savingWithTargetFileDescriptorForADifferentRepositoryThrowsAnError() throws IOException {
		FileDescriptor target = FileDescriptor.of( "non-existing-repository-id", UUID.randomUUID().toString() );
		fileRepository.save( target, RES_TEXTFILE.getInputStream(), true );
	}

	@Test(expected = IllegalArgumentException.class)
	public void savingWithTargetFileDescriptorWithoutOverwritingThrowsExceptionIfFileExists() throws IOException {
		FileDescriptor target = FileDescriptor.of( fileRepository.getRepositoryId(), UUID.randomUUID().toString() );
		fileRepository.save( target, RES_TEXTFILE.getInputStream(), false );

		fileRepository.save( target, RES_TEXTFILE.getInputStream(), false );
	}

	@Test
	public void readInputStream() throws IOException {
		FileDescriptor descriptor = fileRepository.save( RES_TEXTFILE.getInputStream() );

		assertEquals( "some dummy text", read( descriptor ) );
	}

	@Test
	public void writeToNewFile() throws IOException {
		FileDescriptor descriptor = fileRepository.createFile();

		try (InputStream is = fileRepository.getInputStream( descriptor )) {
			assertNotNull( is );
		}

		try (OutputStream os = fileRepository.getOutputStream( descriptor )) {
			try (PrintWriter pw = new PrintWriter( os )) {
				pw.print( "original text" );
				pw.flush();
			}
			os.flush();
		}

		assertEquals( "original text", read( descriptor ) );
	}

	@Test
	public void modifyExistingFile() throws IOException {
		FileDescriptor descriptor = fileRepository.save( FILE_ONE );

		assertEquals( "some dummy text", read( descriptor ) );

		try (OutputStream os = fileRepository.getOutputStream( descriptor )) {
			try (PrintWriter pw = new PrintWriter( os )) {
				pw.print( "modified text" );
				pw.flush();
			}
			os.flush();
		}

		assertEquals( "modified text", read( descriptor ) );
	}

	@Test
	public void renameFileRenamesTheFile() {
		FileDescriptor original = fileRepository.save( FILE_ONE );
		String renamedName = UUID.randomUUID().toString();
		FileDescriptor renamed = new FileDescriptor( fileRepository.getRepositoryId(), null, renamedName );

		assertTrue( fileRepository.move( original, renamed ) );
		assertTrue( fileRepository.exists( renamed ) );
		assertFalse( fileRepository.exists( original ) );
	}

	@Test
	public void renameFileCreatesDirectoriesIfNecessary() {
		FileDescriptor original = fileRepository.save( FILE_ONE );
		String renamedName = UUID.randomUUID().toString();
		String renamedDir = UUID.randomUUID().toString();
		FileDescriptor renamed = new FileDescriptor( fileRepository.getRepositoryId(), renamedDir, renamedName );

		assertTrue( fileRepository.move( original, renamed ) );
		assertTrue( fileRepository.exists( renamed ) );
		assertFalse( fileRepository.exists( original ) );
	}

	@Test(expected = IllegalArgumentException.class)
	public void renameFileToDifferentRepositoryThrowsIllegalArgument() {
		FileDescriptor original = fileRepository.save( FILE_ONE );
		String renamedName = UUID.randomUUID().toString();
		String renamedDir = UUID.randomUUID().toString();
		FileDescriptor renamed = new FileDescriptor( "foo", renamedDir, renamedName );

		fileRepository.move( original, renamed );
	}

	private String read( FileDescriptor descriptor ) throws IOException {
		try (InputStream is = fileRepository.getInputStream( descriptor )) {
			char[] text = new char[1024];
			IOUtils.read( new InputStreamReader( is ), text );

			return new String( text ).trim();
		}
	}
}
