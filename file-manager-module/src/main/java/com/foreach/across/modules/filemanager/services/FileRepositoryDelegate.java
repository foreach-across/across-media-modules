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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A FileRepository implementation that delegates its calls to an underlying implementation.
 * <p/>
 * Using the FileRepositoryDelegate allows runtime replacement of a FileRepository implementation.
 *
 * @see com.foreach.across.modules.filemanager.services.FileManagerImpl
 */
public class FileRepositoryDelegate implements FileRepository
{
	private FileRepository actualImplementation;

	public FileRepository getActualImplementation() {
		return actualImplementation;
	}

	public void setActualImplementation( FileRepository actualImplementation ) {
		this.actualImplementation = actualImplementation;
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
	public FileDescriptor save( FileDescriptor target, InputStream inputStream, boolean overwriteExisting ) {
		return repository().save( target, inputStream, overwriteExisting );
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

	private FileRepository repository() {
		return actualImplementation;
	}
}
