package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import com.foreach.across.modules.filemanager.business.FolderResource;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;

import java.io.*;
import java.net.URL;

/**
 * @author Steven Gentens
 * @since 2.3.0
 */
@Slf4j
public class SpringIntegrationSftpFileResource extends SpringIntegrationFileResource
{
	private final FileDescriptor fileDescriptor;
	private final SftpRemoteFileTemplate remoteFileTemplate;
	private SFTPFile file;

	SpringIntegrationSftpFileResource( FileDescriptor fileDescriptor,
	                                   SFTPFile file,
	                                   SftpRemoteFileTemplate remoteFileTemplate ) {
		super( fileDescriptor, remoteFileTemplate );
		this.fileDescriptor = fileDescriptor;
		this.file = file;
		this.remoteFileTemplate = remoteFileTemplate;
	}

	@Override
	public FolderResource getFolderResource() {
		return new SpringIntegrationSftpFolderResource( fileDescriptor.getFolderDescriptor(), remoteFileTemplate );
	}

	@Override
	public boolean exists() {
		return getSftpFile().exists();
	}

	@Override
	public boolean delete() {
		boolean deleted = super.delete();
		resetFileMetadata();
		return deleted;
	}

	@Override
	public URL getURL() throws IOException {
		throw new UnsupportedOperationException( "URL is not supported for an FTP FileResource" );
	}

	@Override
	public long contentLength() throws IOException {
		SFTPFile file = getSftpFile();
		if ( !file.exists() ) {
			throw new FileNotFoundException( "Unable to locate file " + fileDescriptor );
		}
		return file.getSize();
	}

	@Override
	public long lastModified() throws IOException {
		SFTPFile file = getSftpFile();
		if ( file == null ) {
			throw new FileNotFoundException( "Unable to locate file " + fileDescriptor );
		}
		return file.getLastModified();
	}

	@Override
	public FileResource createRelative( String relativePath ) {
		throw new UnsupportedOperationException( "Creating relative path is not yet supported" );
	}

	@Override
	public String getDescription() {
		return "axfs [" + fileDescriptor.toString() + "] -> " + String.format( "FTP file[path='%s']", getPath() );
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		boolean shouldCreateFile = !exists();
		Session<ChannelSftp.LsEntry> session = remoteFileTemplate.getSession();
		ChannelSftp client = (ChannelSftp) session.getClientInstance();

		if ( shouldCreateFile ) {
			instantiateAsEmptyFile( client );
		}
		resetFileMetadata();
		try {
			return client.put( getPath() );
		}
		catch ( SftpException e ) {
			LOG.error( "Unexpected error whilst opening an OutputStream for file {}", getPath() );
			throw new IOException( e );
		}
	}

	void resetFileMetadata() {
		this.file = null;
	}

	private Void instantiateAsEmptyFile( ChannelSftp client ) throws IOException {
		try (InputStream bin = new ByteArrayInputStream( new byte[0] )) {
			FolderResource folder = getFolderResource();
			if ( !folder.exists() ) {
				folder.create();
			}

			try {
				client.put( bin, getPath() );//client.storeFile( getPath(), bin );
			}
			catch ( SftpException e ) {
				LOG.error( "Unable to create empty file at {}", getPath() );
				throw new IOException( e );
			}
		}
		return null;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		if ( !exists() ) {
			throw new FileNotFoundException( "Unable to locate file " + fileDescriptor );
		}
		Session<ChannelSftp.LsEntry> session = remoteFileTemplate.getSession();
		ChannelSftp client = (ChannelSftp) session.getClientInstance();
		try {
			return client.get( getPath() );
		}
		catch ( SftpException e ) {
			LOG.error( "Unable to create inputstream for file {} ", getPath() );
			throw new IOException( e );
		}
	}

	private SFTPFile getSftpFile() {
		if ( file == null ) {
			this.file = remoteFileTemplate.executeWithClient( this::fetchFileInfo );
		}
		return file;
	}

	private SFTPFile fetchFileInfo( ChannelSftp client ) {
		return new SFTPFile( remoteFileTemplate /*client*/, getPath() );
	}
}
