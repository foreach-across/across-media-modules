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
import com.foreach.across.modules.filemanager.business.FileResource;
import com.foreach.across.modules.filemanager.business.FileStorageException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of both FileManager and FileRepositoryRegistry.  The registry deals out
 * delegate implementations ensuring that the actual implementation can be replaced at runtime.
 *
 * @see com.foreach.across.modules.filemanager.services.FileRepositoryDelegate
 */
@Service
public class FileManagerImpl implements FileManager, FileRepositoryRegistry
{
	private FileRepositoryFactory repositoryFactory;
	private Map<String, FileRepositoryDelegate> repositories = new HashMap<>();

	@Override
	public FileResource createFileResource( String repositoryId ) {
		return requireRepository( repositoryId ).createFileResource();
	}

	@Override
	public FileResource createFileResource() {
		return requireRepository( DEFAULT_REPOSITORY ).createFileResource();
	}

	@Override
	public FileResource createFileResource( boolean allocateImmediately ) {
		return requireRepository( DEFAULT_REPOSITORY ).createFileResource( allocateImmediately );
	}

	@Override
	public FileResource createFileResource( File originalFile, boolean deleteOriginal ) throws IOException {
		return requireRepository( DEFAULT_REPOSITORY ).createFileResource( originalFile, deleteOriginal );
	}

	@Override
	public FileResource createFileResource( InputStream inputStream ) throws IOException {
		return requireRepository( DEFAULT_REPOSITORY ).createFileResource( inputStream );
	}

	@Override
	public FileResource getFileResource( FileDescriptor descriptor ) {
		return requireRepository( descriptor.getRepositoryId() ).getFileResource( descriptor );
	}

	@Override
	public FileDescriptor generateFileDescriptor() {
		return requireRepository( DEFAULT_REPOSITORY ).generateFileDescriptor();
	}

	@Override
	public File createTempFile() {
		FileRepository repository = getRepository( TEMP_REPOSITORY );

		if ( repository != null ) {
			FileRepository tempRepository = requireRepository( TEMP_REPOSITORY );
			FileResource tempFileResource = tempRepository.createFileResource( true );

			if ( tempFileResource instanceof FileResource.TargetFile ) {
				return ( (FileResource.TargetFile) tempFileResource ).getTargetFile();
			}
			else {
				throw new FileStorageException(
						String.format( "File repository '%s' registered, but does not provide FileResource.TargetFile implementations. " +
								               "Any FileRepository used for temp files must return FileResource implementations that implement TargetFile.",
						               TEMP_REPOSITORY ) );
			}
		}

		try {
			return File.createTempFile( UUID.randomUUID().toString(), "" );
		}
		catch ( IOException ioe ) {
			throw new FileStorageException( ioe );
		}
	}

	@Override
	public String getRepositoryId() {
		return requireRepository( DEFAULT_REPOSITORY ).getRepositoryId();
	}

	@Override
	public FileDescriptor createFile() {
		return createFile( DEFAULT_REPOSITORY );
	}

	@Override
	public FileDescriptor moveInto( File file ) {
		return moveInto( DEFAULT_REPOSITORY, file );
	}

	@Override
	public FileDescriptor save( File file ) {
		return save( DEFAULT_REPOSITORY, file );
	}

	@Override
	public FileDescriptor save( String repositoryId, File file ) {
		return requireRepository( repositoryId ).save( file );
	}

	@Override
	public FileDescriptor save( InputStream inputStream ) {
		return save( DEFAULT_REPOSITORY, inputStream );
	}

	@Override
	public FileDescriptor save( String repositoryId, InputStream inputStream ) {
		return requireRepository( repositoryId ).save( inputStream );
	}

	@Override
	public void save( FileDescriptor target, InputStream inputStream, boolean replaceExisting ) {
		requireRepository( target.getRepositoryId() ).save( target, inputStream, true );
	}

	@Override
	public FileDescriptor createFile( String repositoryId ) {
		return requireRepository( repositoryId ).createFile();
	}

	@Override
	public FileDescriptor moveInto( String repositoryId, File file ) {
		return requireRepository( repositoryId ).moveInto( file );
	}

	@Override
	public boolean delete( FileDescriptor descriptor ) {
		return requireRepository( descriptor.getRepositoryId() ).delete( descriptor );
	}

	@Override
	public OutputStream getOutputStream( FileDescriptor descriptor ) {
		return requireRepository( descriptor.getRepositoryId() ).getOutputStream( descriptor );
	}

	@Override
	public InputStream getInputStream( FileDescriptor descriptor ) {
		return requireRepository( descriptor.getRepositoryId() ).getInputStream( descriptor );
	}

	@Override
	public File getAsFile( FileDescriptor descriptor ) {
		return requireRepository( descriptor.getRepositoryId() ).getAsFile( descriptor );
	}

	@Override
	public boolean exists( FileDescriptor descriptor ) {
		return requireRepository( descriptor.getRepositoryId() ).exists( descriptor );
	}

	@Override
	public boolean move( FileDescriptor source, FileDescriptor target ) {
		FileRepository sourceRep = requireRepository( source.getRepositoryId() );
		if ( source.getRepositoryId().equals( target.getRepositoryId() ) ) {
			return sourceRep.move( source, target );
		}
		else {
			save( target, sourceRep.getInputStream( source ), true );
			return delete( source );
		}
	}

	@Override
	public void setFileRepositoryFactory( FileRepositoryFactory factory ) {
		this.repositoryFactory = factory;
	}

	@Override
	public FileRepository getRepository( FileDescriptor descriptor ) {
		return getRepository( descriptor.getRepositoryId() );
	}

	@Override
	public FileRepository getRepository( String repositoryId ) {
		FileRepository repository = repositories.get( repositoryId );

		if ( repository == null ) {
			return createAndRegister( repositoryId );
		}

		return repository;
	}

	@Override
	public boolean repositoryExists( String repositoryId ) {
		return repositories.containsKey( repositoryId );
	}

	@Override
	public FileRepository registerRepository( FileRepository fileRepository ) {
		FileRepositoryDelegate delegate = repositories.get( fileRepository.getRepositoryId() );

		if ( delegate == null ) {
			delegate = new FileRepositoryDelegate();
			repositories.put( fileRepository.getRepositoryId(), delegate );
		}

		delegate.setActualImplementation( fileRepository );

		if ( fileRepository instanceof FileManagerAware ) {
			( (FileManagerAware) fileRepository ).setFileManager( this );
		}

		return delegate;
	}

	private FileRepository requireRepository( String repositoryId ) {
		FileRepository repository = getRepository( repositoryId );

		if ( repository == null ) {
			if ( repositoryFactory != null ) {
				throw new FileStorageException( String.format(
						"No FileRepository with id %s available.  The factory did not create an instance.",
						repositoryId ) );
			}

			throw new FileStorageException( String.format(
					"No FileRepository with id %s available, it has not been registered and no factory is available.",
					repositoryId ) );
		}

		return repository;
	}

	private FileRepository createAndRegister( String repositoryId ) {
		if ( repositoryFactory == null ) {
			throw new IllegalArgumentException(
					String.format( "No FileRepository with id %s available and none could be created (no FileRepositoryFactory)", repositoryId ) );
		}

		FileRepository repository = repositoryFactory.create( repositoryId );

		if ( repository != null ) {
			return registerRepository( repository );
		}

		return null;
	}
}
