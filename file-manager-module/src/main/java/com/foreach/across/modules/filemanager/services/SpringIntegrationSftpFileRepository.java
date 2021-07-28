package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import com.foreach.across.modules.filemanager.business.FolderDescriptor;
import com.foreach.across.modules.filemanager.business.FolderResource;
import lombok.Builder;
import lombok.NonNull;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;

import java.io.IOException;

public class SpringIntegrationSftpFileRepository extends AbstractFileRepository
{
	private final SftpRemoteFileTemplate remoteFileTemplate;

	@Builder
	protected SpringIntegrationSftpFileRepository( @NonNull String repositoryId,
	                                               @NonNull SftpRemoteFileTemplate remoteFileTemplate,
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
		return new SpringIntegrationSftpFileResource( descriptor, file, remoteFileTemplate );
	}

	@Override
	protected FolderResource buildFolderResource( FolderDescriptor descriptor ) {
		return new SpringIntegrationSftpFolderResource( descriptor, remoteFileTemplate );
	}
}
