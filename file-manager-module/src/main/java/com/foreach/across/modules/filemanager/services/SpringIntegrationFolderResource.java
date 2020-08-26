package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FolderDescriptor;
import com.foreach.across.modules.filemanager.business.FolderResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.integration.file.remote.RemoteFileTemplate;
import org.springframework.integration.file.remote.session.Session;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
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
		Optional<FolderResource> optionalParent = getParentFolderResource();
		if ( optionalParent.isPresent() ) {
			FolderResource parent = optionalParent.get();
			if ( !parent.exists() ) {
				boolean created = parent.create();
				LOG.debug( "Parent for '{}' did not exist, attempted to create parent with result: {}", this.getDescriptor(), created );
			}
		}
		return (boolean) remoteFileTemplate.execute( session -> session.mkdir( getPath() ) );
	}

	protected String getPath() {
		return getPath( folderDescriptor );
	}

	protected static String getPath( FolderDescriptor descriptor ) {
		return StringUtils.prependIfMissing( StringUtils.defaultString( descriptor.getFolderId(), "/" ), "/" );
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		return o != null && ( o instanceof FolderResource && folderDescriptor.equals( ( (FolderResource) o ).getDescriptor() ) );
	}

	@Override
	public int hashCode() {
		return folderDescriptor.hashCode();
	}
}
