package com.foreach.imageserver.services;

import com.foreach.imageserver.core.business.ImageFile;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.services.exceptions.TempStoreOperationException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class TempFileServiceImpl implements TempFileService
{
	private static final Logger LOG = LoggerFactory.getLogger( TempFileServiceImpl.class );

	private File tempDir;

	@Autowired
	public TempFileServiceImpl( @Value("${temp.path}") String path ) {
		this.tempDir = new File( path );

		LOG.info( "Temp file store created: {}", tempDir.getAbsolutePath() );

		if ( !tempDir.exists() && tempDir.mkdirs() ) {
			LOG.info( "Temporary directory created: {}", tempDir.getAbsolutePath() );
		}
	}

	@Override
	public ImageFile createImageFile( ImageType imageType, InputStream stream ) {
		String fileName = UUID.randomUUID().toString() + ".tmp";
		File physical = new File( tempDir, fileName );

		FileOutputStream fos = null;

		try {
			fos = new FileOutputStream( physical );
			IOUtils.copy( stream, fos );

			return new TempImageFile( imageType, physical );
		}
		catch ( IOException ioe ) {
			throw new TempStoreOperationException( ioe );
		}
		finally {
			IOUtils.closeQuietly( fos );
			IOUtils.closeQuietly( stream );
		}
	}

	@Override
	public boolean isTempFile( ImageFile imageFile ) {
		return imageFile instanceof TempImageFile;
	}

	@Override
	public ImageFile move( ImageFile file, File physicalDestination ) {
		if ( !isTempFile( file ) ) {
			throw new TempStoreOperationException( "Trying to move a non temporary ImageFile is not allowed" );
		}

		File physical = ( (TempImageFile) file ).getPhysicalFile();

		if ( !physical.renameTo( physicalDestination ) ) {
			LOG.debug( "Move of {} to {} failed - copying content", physical.getAbsolutePath(),
			           physicalDestination.getAbsolutePath() );

			FileOutputStream fos = null;
			InputStream imageData = null;

			try {
				fos = new FileOutputStream( physicalDestination );
				imageData = file.openContentStream();

				IOUtils.copy( imageData, fos );
			}
			catch ( IOException ioe ) {
				throw new TempStoreOperationException( ioe );
			}
			finally {
				IOUtils.closeQuietly( fos );
				IOUtils.closeQuietly( imageData );
			}

			try {
				if ( !physical.delete() ) {
					LOG.warn( "Failed to delete temporary file: {}", physical.getAbsolutePath() );
				}
			}
			catch ( Exception e ) {
				LOG.warn( "Failed to delete temporary file: {} - {}", physical.getAbsolutePath(), e );
			}
		}

		return new ImageFile( file.getImageType(), physicalDestination );
	}

	public static class TempImageFile extends ImageFile
	{
		TempImageFile( ImageType imageType, File physicalFile ) {
			super( imageType, physicalFile );
		}

		@SuppressWarnings("all")
		public File getPhysicalFile() {
			return super.getPhysicalFile();
		}
	}
}
