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

package com.foreach.across.modules.filemanager.business.reference;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.services.FileManager;
import com.foreach.across.modules.filemanager.services.FileRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Steven Gentens
 * @since 1.3.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestFileReferenceService
{
	private ApplicationEventPublisher applicationEventPublisher;
	private FileManager fileManager;
	private FileReferenceRepository fileReferenceRepository;
	private FileReferenceService fileReferenceService;

	private MultipartFile file;
	private InputStream inputStream;
	private FileDescriptor fileDescriptor;

	@Before
	public void setUp() throws IOException {
		applicationEventPublisher = mock( ApplicationEventPublisher.class );
		fileManager = mock( FileManager.class );
		fileReferenceRepository = mock( FileReferenceRepository.class );
		fileReferenceService = new FileReferenceService( fileManager, applicationEventPublisher, fileReferenceRepository );

		FileRepository fileRepository = mock( FileRepository.class );
		when( fileManager.getRepository( FileManager.DEFAULT_REPOSITORY ) ).thenReturn( fileRepository );
		when( fileRepository.getRepositoryId() ).thenReturn( FileManager.DEFAULT_REPOSITORY );

		file = mock( MultipartFile.class );
		when( file.getBytes() ).thenReturn( new byte[5] );
		when( file.getOriginalFilename() ).thenReturn( "my-file.txt" );
		when( file.getContentType() ).thenReturn( "text/pdf" );
		when( file.getSize() ).thenReturn( 5L );
		inputStream = mock( InputStream.class );
		when( file.getInputStream() ).thenReturn( inputStream );

		fileDescriptor = new FileDescriptor( FileManager.TEMP_REPOSITORY, "my-unique-file-name" );
		when( fileManager.save( fileDescriptor.getRepositoryId(), inputStream ) ).thenReturn( fileDescriptor );

		when( fileReferenceRepository.save( any( FileReference.class ) ) )
				.thenAnswer( (Answer<FileReference>) invocation -> {
					FileReference fileReference = (FileReference) invocation.getArguments()[0];
					fileReference.setId( 1L );
					fileReference.setCreatedBy( "John Doe" );
					fileReference.setCreatedDate( new Date() );

					fileReference.setLastModifiedBy( "John Doe" );
					fileReference.setLastModifiedDate( new Date() );
					return fileReference;
				} );

	}

	@Test
	public void noRepositoryDefinedSavesToDefaultRepository() {
		fileReferenceService.save( file );
		verify( fileManager, times( 1 ) ).save( FileManager.TEMP_REPOSITORY, inputStream );
	}

	@Test
	public void save() {
		FileReference save = fileReferenceService.save( fileManager.getRepository( FileManager.DEFAULT_REPOSITORY ), file );
		verify( fileManager, times( 1 ) ).save( FileManager.TEMP_REPOSITORY, inputStream );
		ArgumentCaptor<FileDescriptor> argumentCaptor = ArgumentCaptor.forClass( FileDescriptor.class );

		verify( fileManager, times( 1 ) ).move( eq( fileDescriptor ), argumentCaptor.capture() );
		assertThat( argumentCaptor.getValue().getFileId() ).endsWith( ".txt" );
		assertThat( argumentCaptor.getValue().getRepositoryId() ).isEqualTo( FileManager.DEFAULT_REPOSITORY );

		assertThat( save ).hasNoNullFieldsOrPropertiesExcept( "newEntityId" );
		assertThat( save.getMimeType() ).isEqualTo( file.getContentType() );
		assertThat( save.getFileSize() ).isEqualTo( file.getSize() );
		assertThat( save.getFileDescriptor() ).isEqualTo( argumentCaptor.getValue() );
	}

	@Test
	public void exists() {
		FileReference fileReference = new FileReference();
		FileDescriptor fileDescriptor = new FileDescriptor( "some-repository", "my-file.txt" );
		fileReference.setFileDescriptor( fileDescriptor );

		fileReferenceService.existsAsFile( fileReference );
		verify( fileManager, times( 1 ) ).exists( fileDescriptor );
	}

	@Test
	public void getFile() {
		FileReference fileReference = new FileReference();
		FileDescriptor fileDescriptor = new FileDescriptor( "some-repository", "my-file.txt" );
		fileReference.setFileDescriptor( fileDescriptor );

		fileReferenceService.getFile( fileReference );
		verify( fileManager, times( 1 ) ).getAsFile( fileDescriptor );
	}

	@Test
	public void getInputStream() {
		FileReference fileReference = new FileReference();
		FileDescriptor fileDescriptor = new FileDescriptor( "some-repository", "my-file.txt" );
		fileReference.setFileDescriptor( fileDescriptor );

		fileReferenceService.getInputStream( fileReference );
		verify( fileManager, times( 1 ) ).getInputStream( fileDescriptor );
	}

	@Test
	public void delete() {
		FileReference fileReference = new FileReference();
		FileDescriptor fileDescriptor = new FileDescriptor( "some-repository", "my-file.txt" );
		fileReference.setFileDescriptor( fileDescriptor );

		fileReferenceService.delete( fileReference );
		verify( fileManager, times( 1 ) ).delete( fileDescriptor );
	}
}
