package com.foreach.across.modules.filemanager.services;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SFTPFile
{
	private final ChannelSftp client;
	private final String path;

	public SFTPFile( ChannelSftp client, String path ) {

		this.client = client;
		this.path = path;
	}

	public boolean exists() {
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

	}

	public boolean isFile() {
		try {
			return !client.stat( path ).isDir();
		}
		catch ( Exception e ) {
			LOG.error( "Unexpected error when checking whether file at {} is a file", path, e );
		}
		return false;
	}

	public boolean isDirectory() {
		try {
			client.stat( path );
		}
		catch ( Exception e ) {
			return false;
		}
		return true;
	}

	public String getName() {
		return null;
	}

	public long getSize() {
		try {
			return client.stat( path ).getSize();
		}
		catch ( Exception e ) {
			LOG.error( "Unexpected error when checking file size for {} ", path, e );
		}
		return -1L;
	}

	// epochmilli
	public long getLastModified() {
		try {
			return client.stat( path ).getMTime();
		}
		catch ( Exception e ) {
			LOG.error( "Unexpected error when checking last modified for {}", path, e );
		}
		return -1L;
	}
}
