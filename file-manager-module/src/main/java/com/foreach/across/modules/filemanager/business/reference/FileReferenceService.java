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

import com.foreach.across.core.annotations.ConditionalOnAcrossModule;
import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.reference.properties.FileReferenceProperties;
import com.foreach.across.modules.filemanager.business.reference.properties.FileReferencePropertiesService;
import com.foreach.across.modules.filemanager.services.FileManager;
import com.foreach.across.modules.filemanager.services.FileRepository;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.properties.PropertiesModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.jdo.annotations.Transactional;
import javax.validation.constraints.NotNull;
import java.io.IOException;
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
@ConditionalOnAcrossModule(allOf = { AcrossHibernateJpaModule.NAME, PropertiesModule.NAME })
public class FileReferenceService
{
	private final FileManager fileManager;
	private final ApplicationEventPublisher eventPublisher;
	private final FileReferenceRepository fileReferenceRepository;
	private final FileReferencePropertiesService fileReferencePropertiesService;

	/**
	 * Saves a given {@link MultipartFile} to the default repository.
	 *
	 * @param file to save
	 * @return {@link FileReference} to the file
	 */
	public FileReference save( @NotNull MultipartFile file ) {
		return save( fileManager.getRepository( FileManager.DEFAULT_REPOSITORY ), file );
	}

	/**
	 * Saves a given {@link MultipartFile} to a specific {@link FileRepository}.
	 *
	 * @param fileRepository to save the file to
	 * @param file           to save
	 * @return {@link FileReference} to the file
	 */
	public FileReference save( @NotNull FileRepository fileRepository, @NotNull MultipartFile file ) {

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
		FileReferenceCreationEvent fileReferenceCreationEvent = modifyFileReference( fileReference );
		fileReference = fileReferenceRepository.save( fileReferenceCreationEvent.getFileReference() );
		saveFileReferenceProperties( fileReference, fileReferenceCreationEvent.getFileReferenceProperties() );
		return fileReference;
	}

	private void saveFileReferenceProperties( FileReference fileReference, FileReferenceProperties fileReferenceProperties ) {
		FileReferenceProperties properties = fileReferencePropertiesService.getProperties( fileReference.getId() );
		properties.putAll( fileReferenceProperties );
		fileReferencePropertiesService.saveProperties( properties );
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
	 * Sends out a {@link FileReferenceCreationEvent} for a given {@link FileReference} that can be used to modify the properties upon creation.
	 *
	 * @param fileReference that is to be created
	 * @return modified {@link FileReferenceCreationEvent}
	 */
	private FileReferenceCreationEvent modifyFileReference( FileReference fileReference ) {
		FileReferenceProperties properties = fileReferencePropertiesService.getProperties( 0L );
		FileReferenceCreationEvent event = new FileReferenceCreationEvent( fileReference, properties );
		eventPublisher.publishEvent( event );
		return event;
	}

	/**
	 * Removes a {@link FileReference}. Optionally deletes the physical file if the {@link FileReference} has been deleted.
	 *
	 * @param fileReference to remove
	 * @param deletePhysicalFile whether the physical file should be removed
	 */
	@Transactional
	public void delete( FileReference fileReference, boolean deletePhysicalFile ) {
		if ( deletePhysicalFile ) {
			TransactionSynchronizationManager.registerSynchronization( new TransactionSynchronizationAdapter()
			{
				@Override
				public void afterCommit() {
					fileManager.delete( fileReference.getFileDescriptor() );
				}
			} );
		}
		fileReferenceRepository.delete( fileReference.getId() );
	}
}
