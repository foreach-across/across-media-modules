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
import com.foreach.across.modules.filemanager.business.reference.properties.FileReferenceProperties;
import com.foreach.across.modules.filemanager.business.reference.properties.FileReferencePropertiesService;
import com.foreach.across.modules.filemanager.services.FileManager;
import com.foreach.across.modules.filemanager.services.FileRepository;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

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
	private FileReferencePropertiesService fileReferencePropertiesService;

	private MultipartFile file;
	private InputStream inputStream;
	private FileDescriptor fileDescriptor;
	private FileDescriptor newDescriptor;

	@Before
	public void setUp() throws IOException {
		applicationEventPublisher = mock( ApplicationEventPublisher.class );
		fileManager = mock( FileManager.class );
		fileReferenceRepository = mock( FileReferenceRepository.class );
		fileReferencePropertiesService = mock( FileReferencePropertiesService.class );
		fileReferenceService = new FileReferenceService( fileManager, applicationEventPublisher, fileReferenceRepository, fileReferencePropertiesService );

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
		when( inputStream.read() ).thenReturn( -1 );
		when( inputStream.available() ).thenReturn( -1 );
		when( inputStream.read( any() ) ).thenReturn( -1 );
		fileDescriptor = FileDescriptor.of( FileManager.TEMP_REPOSITORY, "my-unique-file-name" );
		when( fileManager.save( fileDescriptor.getRepositoryId(), inputStream ) ).thenReturn( fileDescriptor );

		newDescriptor = FileDescriptor.of( FileManager.DEFAULT_REPOSITORY, UUID.randomUUID().toString() );
		when( fileRepository.moveInto( any() )).thenReturn( newDescriptor );

		FileReferenceProperties fileReferenceProperties = mock( FileReferenceProperties.class );
		when( fileReferencePropertiesService.getProperties( any() ) ).thenReturn( fileReferenceProperties );

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
	@SneakyThrows
	public void save() {
		FileReference save = fileReferenceService.save( file, FileManager.DEFAULT_REPOSITORY );

		ArgumentCaptor<File> tempFile = ArgumentCaptor.forClass( File.class );
		verify( file ).transferTo( tempFile.capture() );
		verify( fileManager.getRepository( FileManager.DEFAULT_REPOSITORY ), times( 1 ) ).moveInto( tempFile.getValue() );

		assertThat( save ).hasNoNullFieldsOrPropertiesExcept( "newEntityId" );
		assertThat( save.getMimeType() ).isEqualTo( file.getContentType() );
		assertThat( save.getFileSize() ).isEqualTo( file.getSize() );
		assertThat( save.getFileDescriptor() ).isEqualTo( newDescriptor );
	}
}
