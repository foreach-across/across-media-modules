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
import com.foreach.across.modules.filemanager.business.FileRepositoryResource;
import com.foreach.across.modules.filemanager.business.FileResource;
import com.foreach.across.modules.filemanager.business.FolderResource;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Base class with tests scenarios that should run on all implementations of FileRepository
 */
abstract class BaseFileRepositoryTest
{
	static final Resource RES_TEXTFILE = new ClassPathResource( "textfile.txt" );

	private File fileOne;

	@TempDir
	File tempDir;

	String rootFolder;
	FileRepository fileRepository;

	@BeforeEach
	void create() throws IOException {
		fileOne = new File( tempDir, UUID.randomUUID().toString() + ".txt" );
		rootFolder = tempDir.toPath().resolve( UUID.randomUUID().toString() ).toString();

		try (FileWriter w = new FileWriter( fileOne )) {
			IOUtils.copy( RES_TEXTFILE.getInputStream(), w, Charset.defaultCharset() );
			w.flush();
		}

		fileRepository = createRepository();
	}

	abstract FileRepository createRepository();

	// create file creates an existing, empty file, which is writable
	@Test
	void moveIntoDeletesTheOriginalFile() {
		assertTrue( fileOne.exists() );

		FileDescriptor descriptor = fileRepository.moveInto( fileOne );
		assertNotNull( descriptor );
		assertTrue( fileRepository.exists( descriptor ) );

		assertFalse( fileOne.exists() );

		// original file extension is copied
		assertTrue( descriptor.getFileId().endsWith( ".txt" ) );
		assertEquals( "txt", descriptor.getExtension() );

		assertTrue( fileRepository.delete( descriptor ) );
		assertFalse( fileRepository.exists( descriptor ) );
	}

	@Test
	void savingFileAlwaysCreatesANewFile() {
		FileDescriptor descriptorOne = fileRepository.save( fileOne );
		FileDescriptor descriptorTwo = fileRepository.save( fileOne );

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
	void savingInputStreamAlwaysCreatesANewFile() throws IOException {
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
	void savingWithTargetFileDescriptorOverwritesExistingFile() throws IOException {
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

	@Test
	void savingWithTargetFileDescriptorForADifferentRepositoryThrowsAnError() {
		FileDescriptor target = FileDescriptor.of( "non-existing-repository-id", UUID.randomUUID().toString() );
		assertThatExceptionOfType( IllegalArgumentException.class )
				.isThrownBy( () -> fileRepository.save( target, RES_TEXTFILE.getInputStream(), true ) );
	}

	@Test
	void savingWithTargetFileDescriptorWithoutOverwritingThrowsExceptionIfFileExists() throws IOException {
		FileDescriptor target = FileDescriptor.of( fileRepository.getRepositoryId(), UUID.randomUUID().toString() );
		fileRepository.save( target, RES_TEXTFILE.getInputStream(), false );

		assertThatExceptionOfType( IllegalArgumentException.class )
				.isThrownBy( () -> fileRepository.save( target, RES_TEXTFILE.getInputStream(), false ) );
	}

	@Test
	void readInputStream() throws IOException {
		FileDescriptor descriptor = fileRepository.save( RES_TEXTFILE.getInputStream() );

		assertEquals( "some dummy text", read( descriptor ) );
	}

	@Test
	@SneakyThrows
	void createFileResource() {
		FileResource resource = fileRepository.createFileResource();
		Assertions.assertThat( resource.exists() ).isFalse();
		Assertions.assertThat( resource ).isNotNull();

		resource.copyFrom( RES_TEXTFILE );

		Assertions.assertThat( readResource( resource ) )
		          .isEqualTo( "some dummy text" )
		          .isEqualTo( readResource( fileRepository.getFileResource( resource.getDescriptor() ) ) );
	}

	@Test
	@SneakyThrows
	void createAndAllocateFileResource() {
		FileResource resource = fileRepository.createFileResource( true );
		Assertions.assertThat( resource.exists() ).isNotNull();
		try (InputStream is = resource.getInputStream()) {
			Assertions.assertThat( is ).isNotNull();
		}
	}

	@SneakyThrows
	String readResource( FileResource resource ) {
		try (InputStream is = resource.getInputStream()) {
			return StreamUtils.copyToString( is, Charset.defaultCharset() );
		}
	}

	@Test
	void writeToNewFile() throws IOException {
		FileDescriptor descriptor = fileRepository.createFile();

		try (InputStream is = fileRepository.getInputStream( descriptor )) {
			assertNotNull( is );
		}

		try (OutputStream os = fileRepository.getOutputStream( descriptor )) {
			try (PrintWriter pw = new PrintWriter( os )) {
				pw.print( "original text" );
				pw.flush();
				// after the try with resources, the printWriter is closed, which also closes the underlying output stream
				os.flush();
			}
		}

		assertEquals( "original text", read( descriptor ) );
	}

	@Test
	void modifyExistingFile() throws IOException {
		FileDescriptor descriptor = fileRepository.save( fileOne );

		assertEquals( "some dummy text", read( descriptor ) );

		try (OutputStream os = fileRepository.getOutputStream( descriptor )) {
			try (PrintWriter pw = new PrintWriter( os )) {
				pw.print( "modified text" );
				pw.flush();
				// after the try with resources, the printWriter is closed, which also closes the underlying output stream
				os.flush();
			}
		}

		assertEquals( "modified text", read( descriptor ) );
	}

	@Test
	void renameFileRenamesTheFile() {
		FileDescriptor original = fileRepository.save( fileOne );
		String renamedName = UUID.randomUUID().toString();
		FileDescriptor renamed = FileDescriptor.of( fileRepository.getRepositoryId(), null, renamedName );

		assertTrue( fileRepository.move( original, renamed ) );
		assertTrue( fileRepository.exists( renamed ) );
		assertFalse( fileRepository.exists( original ) );
	}

	@Test
	void renameFileCreatesDirectoriesIfNecessary() {
		FileDescriptor original = fileRepository.save( fileOne );
		String renamedName = UUID.randomUUID().toString();
		String renamedDir = UUID.randomUUID().toString();
		FileDescriptor renamed = FileDescriptor.of( fileRepository.getRepositoryId(), renamedDir, renamedName );

		assertTrue( fileRepository.move( original, renamed ) );
		assertTrue( fileRepository.exists( renamed ) );
		assertFalse( fileRepository.exists( original ) );
	}

	@Test
	void renameFileToDifferentRepositoryThrowsIllegalArgument() {
		FileDescriptor original = fileRepository.save( fileOne );
		String renamedName = UUID.randomUUID().toString();
		String renamedDir = UUID.randomUUID().toString();
		FileDescriptor renamed = FileDescriptor.of( "foo", renamedDir, renamedName );

		assertThatExceptionOfType( IllegalArgumentException.class )
				.isThrownBy( () -> fileRepository.move( original, renamed ) );
	}

	@Test
	@SneakyThrows
	void findResourcesAndFiles() {
		FolderResource rootFolder = fileRepository.getRootFolderResource();
		FolderResource folder = rootFolder.getFolderResource( "xx" );
		FileResource file = folder.getFileResource( "findme.txt" );
		file.copyFrom( RES_TEXTFILE );

		assertThat( folder.exists() ).isTrue();
		assertThat( file.exists() ).isTrue();

		assertThat( fileRepository.findFiles( "**" ) ).contains( file );
		assertThat( fileRepository.findFiles( "xx/*" ) ).containsExactly( file );
		assertThat( fileRepository.findFiles( "xx/*.txt" ) ).containsExactly( file );
		assertThat( fileRepository.findFiles( "/xx/findme.txt" ) ).containsExactly( file );
		assertThat( fileRepository.findFiles( "**/findme.txt" ) ).contains( file );

		assertThat( fileRepository.findResources( "**" ) ).contains( file, folder );
		assertThat( fileRepository.findResources( "**", FileResource.class ) ).contains( file );
		assertThat( fileRepository.findResources( "**", FolderResource.class ) ).contains( folder );
		assertThat( fileRepository.findResources( "xx/*" ) ).containsExactly( file );
		assertThat( fileRepository.findResources( "xx/" ) ).containsExactly( folder );
	}

	@Test
	@SneakyThrows
	void findResourcesAndFilesWithHierarchySetup() {
		FolderResource rootFolder = fileRepository.getRootFolderResource();
		if ( !rootFolder.findResources( "*" ).isEmpty() ) {
			assertThat( rootFolder.deleteChildren() ).isTrue();
		}

		FolderResource level1 = rootFolder.getFolderResource( "/child1" );
		FolderResource level1_1 = rootFolder.getFolderResource( "/child1/child1" );
		FolderResource level1_1_1 = rootFolder.getFolderResource( "/child1/child1/child1" );
		FolderResource level2 = rootFolder.getFolderResource( "/child2" );
		FolderResource level2_1 = rootFolder.getFolderResource( "/child2/child1" );
		FolderResource level2_1_1 = rootFolder.getFolderResource( "/child2/child1/child1" );
		FolderResource level2_1_2 = rootFolder.getFolderResource( "/child2/child1/child2" );
		FolderResource level2_2 = rootFolder.getFolderResource( "/child2/child2" );

		List<FolderResource> folders = Arrays.asList( level1, level1_1, level1_1_1, level2, level2_1, level2_1_1, level2_1_2, level2_2 );
		folders.forEach( FolderResource::create );

		FileResource text = rootFolder.getFileResource( "text.txt" );
		FileResource level1_1_text = level1_1.getFileResource( "text.txt" );
		FileResource level1_1_1_text = level1_1_1.getFileResource( "text.txt" );
		FileResource level1_1_1_png = level1_1_1.getFileResource( "image.png" );
		FileResource level2_1_1_png = level2_1_1.getFileResource( "image.png" );
		FileResource level2_1_2_png = level2_1_2.getFileResource( "image.png" );
		FileResource level2_1_2_png2 = level2_1_2.getFileResource( "image2.png" );
		FileResource level2_2_png = level2_2.getFileResource( "image.png" );
		List<FileResource> files = Arrays.asList( text, level1_1_text, level1_1_1_text, level1_1_1_png, level2_1_1_png, level2_1_2_png,
		                                          level2_1_2_png2, level2_2_png );
		for ( int i = 0; i < files.size(); i++ ) {
			saveEmptyFile( files.get( i ) );
		}

		// findself
		Collection<FileRepositoryResource> self = rootFolder.findResources( "/" );
		assertThat( self )
				.hasSize( 1 )
				.containsExactlyInAnyOrder( rootFolder );

		// find everything
		Collection<FileRepositoryResource> allResources = rootFolder.findResources( "**" );
		assertThat( allResources )
				.hasSize( 16 )
				.containsExactlyInAnyOrder( level1, text, level1_1, level1_1_text, level1_1_1, level1_1_1_text, level1_1_1_png, level2, level2_1, level2_1_1,
				                            level2_1_1_png, level2_1_2, level2_1_2_png, level2_1_2_png2, level2_2, level2_2_png );

		// find all folders
		Collection<FileRepositoryResource> allFolders = rootFolder.findResources( "**/" );
		assertThat( allFolders )
				.hasSize( 8 )
				.containsExactlyInAnyOrder( level1, level1_1, level1_1_1, level2, level2_1, level2_1_1, level2_1_2, level2_2 );

		// find first level folders
		Collection<FileRepositoryResource> firstLevelFolders = rootFolder.findResources( "*/" );
		assertThat( firstLevelFolders )
				.hasSize( 2 )
				.containsExactlyInAnyOrder( level1, level2 );

		// find first level files and folders
		Collection<FileRepositoryResource> firstLevelFilesAndFolders = rootFolder.findResources( "*" );
		assertThat( firstLevelFilesAndFolders )
				.hasSize( 3 )
				.containsExactlyInAnyOrder( text, level1, level2 );

		// find all first second level folders
		Collection<FileRepositoryResource> child1FoldersOnSecondLevel = rootFolder.findResources( "*/child1/" );
		assertThat( child1FoldersOnSecondLevel )
				.hasSize( 2 )
				.containsExactlyInAnyOrder( level1_1, level2_1 );

		// find all child2 folders
		Collection<FileRepositoryResource> allChild2Folders = rootFolder.findResources( "**/child2" );
		assertThat( allChild2Folders )
				.hasSize( 3 )
				.containsExactlyInAnyOrder( level2, level2_1_2, level2_2 );

		// find all png files in child1 folders
		Collection<FileRepositoryResource> allPngFilesInAnyChild1Folder = rootFolder.findResources( "**/child1/**/*.png" );
		assertThat( allPngFilesInAnyChild1Folder )
				.hasSize( 4 )
				.containsExactlyInAnyOrder( level1_1_1_png, level2_1_1_png, level2_1_2_png, level2_1_2_png2 );

		allPngFilesInAnyChild1Folder = rootFolder.findResources( "**/child1/**.png" );
		assertThat( allPngFilesInAnyChild1Folder )
				.hasSize( 2 )
				.containsExactlyInAnyOrder( level1_1_1_png, level2_1_1_png );

		assertThat( rootFolder.deleteChildren() ).isTrue();
	}

	void saveEmptyFile( FileResource resource ) throws IOException {
		OutputStream outputStream = resource.getOutputStream();
		outputStream.close();
	}

	private String read( FileDescriptor descriptor ) throws IOException {
		try (InputStream is = fileRepository.getInputStream( descriptor )) {
			char[] text = new char[1024];
			IOUtils.read( new InputStreamReader( is ), text );

			return new String( text ).trim();
		}
	}
}
