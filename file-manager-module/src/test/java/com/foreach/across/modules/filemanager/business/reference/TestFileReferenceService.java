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
import com.foreach.across.modules.filemanager.business.FileResource;
import com.foreach.across.modules.filemanager.business.reference.properties.FileReferenceProperties;
import com.foreach.across.modules.filemanager.business.reference.properties.FileReferencePropertiesService;
import com.foreach.across.modules.filemanager.services.FileManager;
import com.foreach.across.modules.filemanager.services.FileRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Steven Gentens
 * @since 1.3.0
 */
@ExtendWith(MockitoExtension.class)
class TestFileReferenceService
{
	private FileManager fileManager;
	private FileReferenceService fileReferenceService;

	private MultipartFile file;

	@BeforeEach
	void setUp() throws IOException {
		ApplicationEventPublisher applicationEventPublisher = mock( ApplicationEventPublisher.class );
		fileManager = mock( FileManager.class );
		FileReferenceRepository fileReferenceRepository = mock( FileReferenceRepository.class );
		FileReferencePropertiesService fileReferencePropertiesService = mock( FileReferencePropertiesService.class );
		fileReferenceService = new FileReferenceService( fileManager, applicationEventPublisher, fileReferenceRepository, fileReferencePropertiesService );

		FileRepository fileRepository = mock( FileRepository.class );
		when( fileManager.getRepository( FileManager.DEFAULT_REPOSITORY ) ).thenReturn( fileRepository );

		file = mock( MultipartFile.class );
		when( file.getOriginalFilename() ).thenReturn( "my-file.txt" );
		when( file.getContentType() ).thenReturn( "text/pdf" );
		when( file.getSize() ).thenReturn( 5L );
		InputStream inputStream = mock( InputStream.class );
		when( file.getInputStream() ).thenReturn( inputStream );
		when( inputStream.read( any() ) ).thenReturn( -1 );
		FileDescriptor fileDescriptor = FileDescriptor.of( FileManager.TEMP_REPOSITORY, "my-unique-file-name" );

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
	void save() {
		File tempFile = mock( File.class );
		when( fileManager.createTempFile() ).thenReturn( tempFile );
		FileResource fileResource = mock( FileResource.class );

		FileDescriptor generated = FileDescriptor.of( "1:2:3" );
		when( fileManager.getRepository( FileManager.DEFAULT_REPOSITORY ).generateFileDescriptor() ).thenReturn( generated );

		when( fileManager.getRepository( FileManager.DEFAULT_REPOSITORY ).getFileResource( generated.withExtension( "txt" ) ) ).thenReturn( fileResource );
		when( fileResource.getDescriptor() ).thenReturn( FileDescriptor.of( "1:2:3" ) );

		FileReference save = fileReferenceService.save( file, FileManager.DEFAULT_REPOSITORY );
		verify( file ).transferTo( tempFile );

		assertThat( save ).hasNoNullFieldsOrPropertiesExcept( "newEntityId" );
		assertThat( save.getMimeType() ).isEqualTo( file.getContentType() );
		assertThat( save.getFileSize() ).isEqualTo( file.getSize() );
		assertThat( save.getFileDescriptor() ).isEqualTo( FileDescriptor.of( "1:2:3" ) );
	}
}
