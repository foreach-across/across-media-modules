package com.foreach.across.modules.filemanager.services;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.util.StringUtils;

@Slf4j
public class SFTPFile
{
	private final String path;
	private final String fileName;

	private SftpRemoteFileTemplate remoteFileTemplate;

	public SFTPFile( SftpRemoteFileTemplate remoteFileTemplate, String path ) {
		this.remoteFileTemplate = remoteFileTemplate;
		this.path = path;
		this.fileName = StringUtils.getFilename( path );
	}

	public boolean exists() {

		return remoteFileTemplate.<Boolean, ChannelSftp>executeWithClient( client -> {
			try {
				client.stat( path );
			}
			catch ( SftpException e ) {
				if ( e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE ) {
					return false;
					// file doesn't exist
				}
				LOG.error( "Unexpected error when checking whether file at {} exists", path, e );
				return false;
			}
			return true;
		} );

	}

	public boolean isDirectory() {
		return remoteFileTemplate.<Boolean, ChannelSftp>executeWithClient( client -> {
			try {
				return client.stat( path ).isDir();
			}
			catch ( Exception e ) {
			}
			return false;
		} );

	}

	public String getName() {
//		return remoteFileTemplate.<Boolean, ChannelSftp>executeWithClient( client -> {
//			try {
//				return client.stat( path ).;
//			}
//			catch ( Exception e ) {
//			}
//			return false;
//		} );
		return fileName;
	}

	public long getSize() {
		return remoteFileTemplate.<Long, ChannelSftp>executeWithClient( client -> {
			try {
				return client.stat( path ).getSize();
			}
			catch ( Exception e ) {
				LOG.error( "Unexpected error when checking file size for {} ", path, e );
			}
			return -1L;
		} );

	}

	// epochmilli
	public long getLastModified() {
		return remoteFileTemplate.<Long, ChannelSftp>executeWithClient( client -> {
			try {
				return (long) client.stat( path ).getMTime();
			}
			catch ( Exception e ) {
				LOG.error( "Unexpected error when checking last modified for {}", path, e );
			}
			return -1L;
		} );
	}
}
