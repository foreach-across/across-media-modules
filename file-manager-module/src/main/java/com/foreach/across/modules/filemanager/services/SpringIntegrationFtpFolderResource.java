package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.integration.ftp.session.FtpRemoteFileTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class SpringIntegrationFtpFolderResource extends SpringIntegrationFolderResource
{
	private final FolderDescriptor folderDescriptor;
	private final FtpRemoteFileTemplate remoteFileTemplate;

	SpringIntegrationFtpFolderResource( @NonNull FolderDescriptor folderDescriptor,
	                                    @NonNull FtpRemoteFileTemplate remoteFileTemplate ) {
		super( folderDescriptor, remoteFileTemplate );
		this.folderDescriptor = folderDescriptor;
		this.remoteFileTemplate = remoteFileTemplate;
	}

	@Override
	public Optional<FolderResource> getParentFolderResource() {
		return folderDescriptor.getParentFolderDescriptor()
		                       .map( fd -> new SpringIntegrationFtpFolderResource( fd, remoteFileTemplate ) );
	}

	@Override
	public FileRepositoryResource getResource( String relativePath ) {
		if ( relativePath.endsWith( "/" ) ) {
			return new SpringIntegrationFtpFolderResource( folderDescriptor.createFolderDescriptor( relativePath ), remoteFileTemplate );
		}

		FileDescriptor fileDescriptor = folderDescriptor.createFileDescriptor( relativePath );
		String actualPath = SpringIntegrationFtpFileResource.getPath( fileDescriptor );
		FTPFile ftpFile = remoteFileTemplate.exists( actualPath )
				? remoteFileTemplate.<FTPFile, FTPClient>executeWithClient( client -> retrieveRemoteFile( client, actualPath ) )
				: null;

		return new SpringIntegrationFtpFileResource( fileDescriptor, ftpFile, remoteFileTemplate );
	}

	private FTPFile retrieveRemoteFile( FTPClient client, String path ) {
		try {
			return client.mlistFile( path );
		}
		catch ( IOException e ) {
			LOG.error( "Unexpected exception whilst retrieving file for path '{}'", path, e );
			return null;
		}
	}

	@Override
	public Collection<FileRepositoryResource> findResources( @NonNull String pattern ) {
		List<FolderResource> folders =
				remoteFileTemplate.<List<FolderResource>, FTPClient>executeWithClient( client -> retrieveFoldersForPath( client, pattern ) );
		List<FileResource> files =
				remoteFileTemplate.<List<FileResource>, FTPClient>executeWithClient( client -> retrieveFilesForPath( client, pattern ) );
		return Stream.concat( folders.stream(), files.stream() ).collect( Collectors.toList() );
	}

	private List<FolderResource> retrieveFoldersForPath( FTPClient client, String pattern ) {
		FTPFile[] ftpFiles = null;
		try {
			ftpFiles = client.listDirectories();
		}
		catch ( IOException e ) {
			LOG.error( "Unexpected error whilst listing directories for path '{}'. Falling back to no directories found.", pattern, e );
			ftpFiles = new FTPFile[0];
		}

		return Arrays.stream( ftpFiles )
		             .map( file -> new SpringIntegrationFtpFolderResource(
				             FolderDescriptor.rootFolder( folderDescriptor.getRepositoryId() ).createFolderDescriptor( file.getName() ), remoteFileTemplate ) )
		             .collect( Collectors.toList() );
	}

	private List<FileResource> retrieveFilesForPath( FTPClient client, String pattern ) {
		FTPFile[] ftpFiles = null;
		try {
			ftpFiles = client.listFiles();
		}
		catch ( IOException e ) {
			LOG.error( "Unexpected error whilst listing directories for path '{}'. Falling back to no directories found.", pattern, e );
			ftpFiles = new FTPFile[0];
		}

		return Arrays.stream( ftpFiles )
		             .map( file -> new SpringIntegrationFtpFileResource( FileDescriptor.of( folderDescriptor.getRepositoryId(), "", file.getName() ), file,
		                                                                 remoteFileTemplate ) )
		             .collect( Collectors.toList() );
	}

}
