package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FolderDescriptor;
import com.foreach.across.modules.filemanager.business.FolderResource;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.integration.file.remote.RemoteFileTemplate;
import org.springframework.integration.file.remote.session.Session;

import java.io.IOException;

@RequiredArgsConstructor
public abstract class SpringIntegrationFolderResource implements FolderResource
{
	private final FolderDescriptor folderDescriptor;
	private final RemoteFileTemplate remoteFileTemplate;

	@Override
	public FolderDescriptor getDescriptor() {
		return folderDescriptor;
	}

	@Override
	public boolean exists() {
		return remoteFileTemplate.exists( getPath() );
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean delete( boolean deleteChildren ) {
		return (boolean) remoteFileTemplate.execute( session -> session.rmdir( getPath() ) );
	}

	@Override
	public boolean deleteChildren() {
		try (Session session = remoteFileTemplate.getSession()) {
			String[] fileNames = session.listNames( getPath() );
			boolean allRemoved = true;
			for ( String fileName : fileNames ) {
				boolean removed = session.remove( fileName );
				if ( !removed ) {
					allRemoved = false;
				}
			}
			return allRemoved;
		}
		catch ( IOException e ) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean create() {
		return (boolean) remoteFileTemplate.execute( session -> session.mkdir( getPath() ) );
	}

	private String getPath() {
		return getPath( folderDescriptor );
	}

	private static String getPath( FolderDescriptor descriptor ) {
		return StringUtils.defaultString( descriptor.getFolderId(), "/" );
	}
}
