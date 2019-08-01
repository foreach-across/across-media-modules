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
import com.foreach.across.modules.filemanager.business.FileResource;
import com.foreach.across.modules.filemanager.business.reference.properties.FileReferenceProperties;
import com.foreach.across.modules.filemanager.business.reference.properties.FileReferencePropertiesService;
import com.foreach.across.modules.filemanager.services.FileManager;
import com.foreach.across.modules.filemanager.services.FileRepository;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.properties.PropertiesModule;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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
	public FileReference save( @NotNull MultipartFile file, @NotNull String repositoryId ) {
		return save( file, fileManager.getRepository( repositoryId ) );
	}

	/**
	 * Saves a given {@link MultipartFile} to a specific {@link FileRepository}.
	 *
	 * @param file           to save
	 * @param fileRepository to save the file to
	 * @return {@link FileReference} to the file
	 */
	@SneakyThrows(IOException.class)
	public FileReference save( @NotNull MultipartFile file, @NotNull FileRepository fileRepository ) {
		File tempFile;
		FileReference fileReference = new FileReference();

		try {
			try (InputStream is = file.getInputStream()) {
				fileReference.setHash( DigestUtils.md5DigestAsHex( is ) );
			}
			tempFile = fileManager.createTempFile();
			file.transferTo( tempFile );
		}
		catch ( IOException e ) {
			LOG.error( "Unable to read file {}", file.getOriginalFilename(), e );
			throw new IllegalArgumentException( e );
		}

		// generate file resource with the extension of the original file
		FileDescriptor descriptor = fileRepository.generateFileDescriptor().withExtensionFrom( file.getOriginalFilename() );
		FileResource fileResource = fileRepository.getFileResource( descriptor );
		fileResource.copyFrom( tempFile, true );

		fileReference.setFileDescriptor( fileResource.getDescriptor() );
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
	 * Changes the repositories of the physical files of a collection of references.
	 *
	 * @param fileReferences  collection of references whose descriptors should be updated
	 * @param repositoryId    name of the target repository
	 * @param deleteOriginals true if original files should be deleted after move
	 */
	@Transactional
	@SuppressWarnings("unused")
	public void changeFileRepository( @NonNull Iterable<FileReference> fileReferences, @NonNull String repositoryId, boolean deleteOriginals ) {
		fileReferences.forEach( fr -> changeFileRepository( fr, repositoryId, deleteOriginals ) );
	}

	/**
	 * Changes the repository where the actual physical file of a reference is stored. This will update and save the file reference.
	 * If the physical file is already in that repository, nothing will be done.
	 * <p/>
	 * Note that this wil copy the original file into the new repository, but will not remove the original file when done.
	 * Use {@link #changeFileRepository(FileReference, String, boolean)} if you want to delete the original file.
	 *
	 * @param fileReference to update
	 * @param repositoryId  name of the target repository
	 */
	@Transactional
	@SuppressWarnings("unused")
	public void changeFileRepository( @NonNull FileReference fileReference, @NonNull String repositoryId ) {
		changeFileRepository( fileReference, repositoryId, false );
	}

	/**
	 * Changes the repository where the actual physical file of a reference is stored. This will update and save the file reference.
	 * If the physical file is already in that repository, nothing will be done.
	 * <p/>
	 * Depending on {@code removeOriginal} the original file will be removed after a new one has been uploaded.
	 * Only delete original file descriptors if you are sure they are no longer referenced anywhere.
	 *
	 * @param fileReference to update
	 * @param repositoryId  name of the target repository
	 */
	@Transactional
	@SneakyThrows(IOException.class)
	public void changeFileRepository( @NonNull FileReference fileReference, @NonNull String repositoryId, boolean removeOriginal ) {
		FileDescriptor fileDescriptor = fileReference.getFileDescriptor();

		if ( fileDescriptor != null && !StringUtils.equals( repositoryId, fileDescriptor.getRepositoryId() ) ) {
			LOG.debug( "Moving file '{}' to repository {}", fileDescriptor, repositoryId );

			FileResource originalResource = fileManager.getFileResource( fileDescriptor );
			FileResource newResource = generateTargetFileResource( fileDescriptor, repositoryId );
			newResource.copyFrom( originalResource );

			LOG.debug( "New file descriptor for file '{}': '{}'", fileDescriptor, newResource.getDescriptor() );

			fileReference.setFileDescriptor( newResource.getDescriptor() );
			fileReferenceRepository.save( fileReference );

			if ( removeOriginal ) {
				transactionalDeletePhysicalFile( fileDescriptor );
			}
		}
	}

	private FileResource generateTargetFileResource( FileDescriptor originalDescriptor, String targetRepositoryId ) {
		FileRepository targetRepository = fileManager.getRepository( targetRepositoryId );
		FileDescriptor descriptor = targetRepository.generateFileDescriptor().withExtension( originalDescriptor.getExtension() );
		return targetRepository.getFileResource( descriptor );
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
	 * @param fileReference      to remove
	 * @param deletePhysicalFile whether the physical file should be removed
	 */
	@Transactional
	public void delete( FileReference fileReference, boolean deletePhysicalFile ) {
		FileDescriptor descriptor = fileReference.getFileDescriptor();
		fileReferenceRepository.delete( fileReference );

		if ( deletePhysicalFile ) {
			transactionalDeletePhysicalFile( descriptor );
		}
	}

	private void transactionalDeletePhysicalFile( FileDescriptor fileDescriptor ) {
		TransactionSynchronizationManager.registerSynchronization( new TransactionSynchronizationAdapter()
		{
			@Override
			public void afterCommit() {
				try {
					if ( !fileManager.delete( fileDescriptor ) ) {
						LOG.warn( "Was asked to delete file {} but was possibly not deleted.", fileDescriptor );
					}
				}
				catch ( Exception e ) {
					LOG.warn( "Was asked to delete file {} but an exception occurred and file was most likely not deleted.", fileDescriptor, e );
				}
			}
		} );
	}
}
