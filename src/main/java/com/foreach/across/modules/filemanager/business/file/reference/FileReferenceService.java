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

package com.foreach.across.modules.filemanager.business.file.reference;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.services.FileManager;
import com.foreach.across.modules.filemanager.services.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * Enables the storage of multipart files.
 *
 * @author Steven Gentens
 * @since 1.3.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileReferenceService
{
	private final FileManager fileManager;
	private final ApplicationEventPublisher eventPublisher;
	private final FileReferenceRepository fileReferenceRepository;

	public FileReference save( MultipartFile file ) {
		return save( fileManager.getRepository( FileManager.DEFAULT_REPOSITORY ), file );
	}

	public FileReference save( FileRepository fileRepository, MultipartFile file ) {
		if ( file == null ) {
			return null;
		}

		FileDescriptor savedFile;
		FileReference fileReference = new FileReference();
		try {
			savedFile = fileManager.save( FileManager.TEMP_REPOSITORY, file.getInputStream() );
			fileReference.setFileDescriptor( savedFile );
			fileReference.setHash( DigestUtils.md5Hex( file.getBytes() ) );
		}
		catch ( IOException e ) {
			LOG.error( "Unable to read file {}", file.getOriginalFilename(), e );
			return null;
		}
		FileDescriptor targetDescriptor = getTargetDescriptor( fileRepository, file );
		fileManager.move( savedFile, targetDescriptor );
		fileReference.setFileDescriptor( targetDescriptor );

		fileReference.setName( file.getOriginalFilename() );
		fileReference.setFileSize( file.getSize() );
		fileReference.setMimeType( file.getContentType() );
		modifyFileReference( fileReference );
		return fileReferenceRepository.save( fileReference );
	}

	private FileDescriptor getTargetDescriptor( FileRepository fileRepository, MultipartFile file ) {
		return new FileDescriptor( fileRepository.getRepositoryId(), UUID.randomUUID() + "." + FilenameUtils.getExtension( file.getOriginalFilename() ) );
	}

	private FileReference modifyFileReference( FileReference fileReference ) {
		eventPublisher.publishEvent( fileReference );
		return fileReference;
	}
}
