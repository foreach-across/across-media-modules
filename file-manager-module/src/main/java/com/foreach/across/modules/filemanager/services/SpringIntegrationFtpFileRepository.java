package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import com.foreach.across.modules.filemanager.business.FolderDescriptor;
import com.foreach.across.modules.filemanager.business.FolderResource;
import lombok.Builder;
import lombok.NonNull;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.integration.ftp.session.FtpRemoteFileTemplate;

import java.io.IOException;

public class SpringIntegrationFtpFileRepository extends AbstractFileRepository
{
	private final FtpRemoteFileTemplate remoteFileTemplate;

	@Builder
	protected SpringIntegrationFtpFileRepository( @NonNull String repositoryId,
	                                              @NonNull FtpRemoteFileTemplate remoteFileTemplate,
	                                              PathGenerator pathGenerator ) {
		super( repositoryId );
		setPathGenerator( pathGenerator );
		this.remoteFileTemplate = remoteFileTemplate;

	}

	@Override
	protected FileResource buildFileResource( FileDescriptor descriptor ) {
		String path = SpringIntegrationFtpFileResource.getPath( descriptor );
		FTPFile file = null;
		if ( remoteFileTemplate.exists( path ) ) {
			file = remoteFileTemplate.<FTPFile, FTPClient>executeWithClient( client -> {
				try {
					return client.mdtmFile( path );
				}
				catch ( IOException e ) {
					return null;
				}
			} );
		}
		return new SpringIntegrationFtpFileResource( descriptor, file, remoteFileTemplate );
	}

	@Override
	protected FolderResource buildFolderResource( FolderDescriptor descriptor ) {
		return new SpringIntegrationFtpFolderResource( descriptor, remoteFileTemplate );
	}
}
