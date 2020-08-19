package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import com.foreach.across.modules.filemanager.business.FolderResource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.integration.ftp.session.FtpRemoteFileTemplate;

import java.io.*;
import java.net.URL;

import static org.apache.commons.io.FileUtils.ONE_GB;

@Slf4j
public class SpringIntegrationFtpFileResource extends SpringIntegrationFileResource
{
	private final FileDescriptor fileDescriptor;
	private final FtpRemoteFileTemplate remoteFileTemplate;
	private FTPFile file;

	SpringIntegrationFtpFileResource( FileDescriptor fileDescriptor,
	                                  FTPFile file,
	                                  FtpRemoteFileTemplate remoteFileTemplate ) {
		super( fileDescriptor, remoteFileTemplate );
		this.fileDescriptor = fileDescriptor;
		this.file = file;
		this.remoteFileTemplate = remoteFileTemplate;
	}

	@Override
	public FolderResource getFolderResource() {
		return null;
	}

	@Override
	public URL getURL() throws IOException {
		throw new UnsupportedOperationException( "URL is not supported for an FTP FileResource" );
	}

	@Override
	public long contentLength() throws IOException {
		return file == null ? 0 : file.getSize();
	}

	@Override
	public long lastModified() throws IOException {
		return file == null ? 0 : file.getTimestamp().toInstant().toEpochMilli();
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
		if ( !exists() || file == null ) {
			remoteFileTemplate.<Void, FTPClient>executeWithClient( this::instantiateAsEmptyFile );
		}
		return remoteFileTemplate.execute( session -> {
			InputStream in = session.readRaw( getPath() );
			OutputStream out = new ByteArrayOutputStream();
			if ( contentLength() > 1.5 * ONE_GB ) {
				IOUtils.copyLarge( in, out );
			}
			else {
				IOUtils.copy( in, out );
			}
			in.close();
			session.finalizeRaw();
			return out;
		} );
	}

	private Void instantiateAsEmptyFile( FTPClient client ) {
		try (InputStream bin = new ByteArrayInputStream( new byte[0] )) {
			client.storeFile( getPath(), bin );

			this.file = client.mdtmFile( getPath() );
		}
		catch ( IOException e ) {
			LOG.error( "Unable to create empty file for descriptor {}", fileDescriptor, e );
		}
		return null;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return remoteFileTemplate.execute( session -> session.readRaw( getPath() ) );
	}
}
