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

import com.foreach.across.modules.filemanager.business.*;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

/**
 * A FileRepository implementation that delegates its calls to an underlying implementation.
 * <p/>
 * Using the FileRepositoryDelegate allows runtime replacement of a FileRepository implementation.
 *
 * @see com.foreach.across.modules.filemanager.services.FileManagerImpl
 */
@SuppressWarnings({ "deprecation", "squid:CallToDeprecatedMethod" })
public class FileRepositoryDelegate implements FileRepository
{
	@Setter
	private FileRepository actualImplementation;

	public FileRepository getActualImplementation() {
		return actualImplementation;
	}

	@Override
	public FileResource createFileResource( File originalFile, boolean deleteOriginal ) throws IOException {
		return actualImplementation.createFileResource( originalFile, deleteOriginal );
	}

	@Override
	public FileResource createFileResource( InputStream inputStream ) throws IOException {
		return actualImplementation.createFileResource( inputStream );
	}

	@Override
	public FileResource createFileResource() {
		return actualImplementation.createFileResource();
	}

	@Override
	public FileResource createFileResource( boolean allocateImmediately ) {
		return actualImplementation.createFileResource( allocateImmediately );
	}

	@Override
	public FileResource getFileResource( FileDescriptor descriptor ) {
		return actualImplementation.getFileResource( descriptor );
	}

	@Override
	public FolderResource getRootFolderResource() {
		return actualImplementation.getRootFolderResource();
	}

	@Override
	public FolderResource getFolderResource( FolderDescriptor descriptor ) {
		return actualImplementation.getFolderResource( descriptor );
	}

	@Override
	public FileDescriptor generateFileDescriptor() {
		return actualImplementation.generateFileDescriptor();
	}

	@Override
	public String getRepositoryId() {
		return repository().getRepositoryId();
	}

	@Override
	public FileDescriptor createFile() {
		return repository().createFile();
	}

	@Override
	public FileDescriptor moveInto( File file ) {
		return repository().moveInto( file );
	}

	@Override
	public FileDescriptor save( File file ) {
		return repository().save( file );
	}

	@Override
	public FileDescriptor save( InputStream inputStream ) {
		return repository().save( inputStream );
	}

	@Override
	public void save( FileDescriptor target, InputStream inputStream, boolean replaceExisting ) {
		repository().save( target, inputStream, replaceExisting );
	}

	@Override
	public boolean delete( FileDescriptor descriptor ) {
		return repository().delete( descriptor );
	}

	@Override
	public OutputStream getOutputStream( FileDescriptor descriptor ) {
		return repository().getOutputStream( descriptor );
	}

	@Override
	public InputStream getInputStream( FileDescriptor descriptor ) {
		return repository().getInputStream( descriptor );
	}

	@Override
	public File getAsFile( FileDescriptor descriptor ) {
		return repository().getAsFile( descriptor );
	}

	@Override
	public boolean exists( FileDescriptor descriptor ) {
		return repository().exists( descriptor );
	}

	@Override
	public boolean move( FileDescriptor source, FileDescriptor target ) {
		return repository().move( source, target );
	}

	@Override
	public Collection<FileResource> findFiles( String pattern ) {
		return repository().findFiles( pattern );
	}

	@Override
	public <U extends FileRepositoryResource> Collection<U> findResources( String pattern, Class<U> resourceType ) {
		return repository().findResources( pattern, resourceType );
	}

	@Override
	public Collection<FileRepositoryResource> findResources( String pattern ) {
		return repository().findResources( pattern );
	}

	private FileRepository repository() {
		return actualImplementation;
	}
}
