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
	public boolean rename( FileDescriptor original, FileDescriptor renamed ) {
		return repository().rename( original, renamed );
	}

	private FileRepository repository() {
		return actualImplementation;
	}
}
