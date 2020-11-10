package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.integration.file.remote.RemoteFileTemplate;

import java.util.Arrays;

@RequiredArgsConstructor
public abstract class SpringIntegrationFileResource implements FileResource
{
	private final FileDescriptor fileDescriptor;
	private final RemoteFileTemplate remoteFileTemplate;

	@Override
	public FileDescriptor getDescriptor() {
		return fileDescriptor;
	}

	@Override
	public boolean delete() {
		if ( !exists() ) {
			return true;
		}
		return remoteFileTemplate.remove( getPath() );
	}

	@Override
	public boolean exists() {
		return remoteFileTemplate.exists( getPath() );
	}

	@Override
	public String getFilename() {
		return fileDescriptor.getFileId();
	}

	protected String getPath() {
		return getPath( fileDescriptor );
	}

	protected static String getPath( FileDescriptor fileDescriptor ) {
		String path = StringUtils.join( Arrays.asList( fileDescriptor.getFolderId(), fileDescriptor.getFileId() ), "/" );
		path = StringUtils.prependIfMissing( path, "/" );
		return path;
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		return o != null && ( o instanceof FileResource && fileDescriptor.equals( ( (FileResource) o ).getDescriptor() ) );
	}

	@Override
	public int hashCode() {
		return fileDescriptor.hashCode();
	}

	@Override
	public String toString() {
		return getDescription();
	}
}
