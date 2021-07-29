package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import com.foreach.across.modules.filemanager.business.FolderDescriptor;
import com.foreach.across.modules.filemanager.business.FolderResource;
import com.jcraft.jsch.ChannelSftp;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;

/**
 * @author Steven Gentens
 * @since 2.3.0
 */
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
		String path = SpringIntegrationSftpFileResource.getPath( descriptor );
		SFTPFile file = null;
		if ( remoteFileTemplate.exists( path ) ) {
			file = remoteFileTemplate.<SFTPFile, ChannelSftp>executeWithClient( client -> new SFTPFile( remoteFileTemplate, path ) );
		}
		return new SpringIntegrationSftpFileResource( descriptor, file, remoteFileTemplate );
	}

	@Override
	protected FolderResource buildFolderResource( FolderDescriptor descriptor ) {
		return new SpringIntegrationSftpFolderResource( descriptor, remoteFileTemplate );
	}
}
