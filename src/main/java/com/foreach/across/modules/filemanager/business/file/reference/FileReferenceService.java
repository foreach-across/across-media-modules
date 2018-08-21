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

import com.foreach.across.core.annotations.ConditionalOnAcrossModule;
import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.services.FileManager;
import com.foreach.across.modules.filemanager.services.FileRepository;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Central point for working with {@link FileReference}s.
 * Allows for {@link MultipartFile}s to be stored as well as the retrieval/removal of files.
 *
 * @author Steven Gentens
 * @since 1.3.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnAcrossModule(allOf = AcrossHibernateJpaModule.NAME)
public class FileReferenceService
{
	private final FileManager fileManager;
	private final ApplicationEventPublisher eventPublisher;
	private final FileReferenceRepository fileReferenceRepository;

	/**
	 * Saves a given {@link MultipartFile} to the default repository.
	 *
	 * @param file to save
	 * @return {@link FileReference} to the file
	 */
	public FileReference save( MultipartFile file ) {
		return save( fileManager.getRepository( FileManager.DEFAULT_REPOSITORY ), file );
	}

	/**
	 * Saves a given {@link MultipartFile} to a specific {@link FileRepository}.
	 *
	 * @param fileRepository to save the file to
	 * @param file           to save
	 * @return {@link FileReference} to the file
	 */
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
		FileDescriptor targetDescriptor = createTargetDescriptor( fileRepository, file );
		fileManager.move( savedFile, targetDescriptor );
		fileReference.setFileDescriptor( targetDescriptor );

		fileReference.setName( file.getOriginalFilename() );
		fileReference.setFileSize( file.getSize() );
		fileReference.setMimeType( file.getContentType() );
		modifyFileReference( fileReference );
		return fileReferenceRepository.save( fileReference );
	}

	/**
	 * Creates a {@link FileDescriptor} for the {@link FileRepository} in which the file should be saved.
	 *
	 * @param fileRepository where the file should be stored
	 * @param file           to store
	 * @return location where the file should be stored.
	 */
	private FileDescriptor createTargetDescriptor( FileRepository fileRepository, MultipartFile file ) {
		return new FileDescriptor( fileRepository.getRepositoryId(), UUID.randomUUID() + "." + FilenameUtils.getExtension( file.getOriginalFilename() ) );
	}

	/**
	 * Sends out an event for a given FileReference that can be used to modify the properties upon creation.
	 *
	 * @param fileReference that has been created
	 * @return modified {@link FileReference}
	 */
	private FileReference modifyFileReference( FileReference fileReference ) {
		eventPublisher.publishEvent( fileReference );
		return fileReference;
	}

	/**
	 * Checks whether the referenced file is still stored.
	 *
	 * @param fileReference to check
	 * @return {@code true} if the file exists, {@code false} if not
	 */
	public boolean existsAsFile( FileReference fileReference ) {
		return fileManager.exists( fileReference.getFileDescriptor() );
	}

	/**
	 * Retrieves a referenced file.
	 *
	 * @param fileReference to retrieve
	 * @return the referenced file
	 */
	public File getFile( FileReference fileReference ) {
		return fileManager.getAsFile( fileReference.getFileDescriptor() );
	}

	/**
	 * Creates an {@link InputStream} for the referenced file.
	 *
	 * @param fileReference to retrieve
	 * @return {@link InputStream} of the referenced file.
	 */
	public InputStream getInputStream( FileReference fileReference ) {
		return fileManager.getInputStream( fileReference.getFileDescriptor() );
	}

	/**
	 * Removes a referenced file as well as its {@link FileReference} if the file was successfully deleted.
	 *
	 * @param fileReference to remove
	 * @return {@code true} if the file was successfully deleted, {@code false} if the delete failed or the file does not exist.
	 */
	public boolean delete( FileReference fileReference ) {
		boolean delete = fileManager.delete( fileReference.getFileDescriptor() );
		if ( delete ) {
			fileReferenceRepository.delete( fileReference.getId() );
		}
		return delete;
	}
}
