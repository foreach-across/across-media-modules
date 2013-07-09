package com.foreach.imageserver.services;

import com.foreach.imageserver.business.Image;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;

@Service
public class ImageStoreServiceImpl implements ImageStoreService
{
	private static final Logger LOG = LoggerFactory.getLogger( ImageStoreServiceImpl.class );
	private static final FastDateFormat PATH_FORMAT = FastDateFormat.getInstance( "/yyyy/MM/dd/" );

	private final File originalBasePath;
	private final File variantBasePath;

	@Autowired
	public ImageStoreServiceImpl( @Value("${store.original.path}") String originalBasePath,
	                              @Value("${store.variant.path}") String variantBasePath ) {
		this.originalBasePath = new File( originalBasePath );
		this.variantBasePath = new File( variantBasePath );

		LOG.info( "Image store original file location: {}", originalBasePath );
		LOG.info( "Image store variant file location: {}", variantBasePath );

		if ( StringUtils.equalsIgnoreCase( this.originalBasePath.getAbsolutePath(),
		                                   this.variantBasePath.getAbsolutePath() ) ) {
			throw new ImageStoreOperationException(
					"Original and variant directory are identical - this is not allowed." );
		}
	}

	@Override
	public String generateRelativeImagePath( Image image ) {
		return PATH_FORMAT.format( image.getDateCreated() );
	}

	@Override
	public long saveImage( Image image, InputStream imageData ) {
		String path = createPathForOriginal( image );
		String fileName = createFileName( image );

		try {
			FileOutputStream fos = new FileOutputStream( path + "/" + fileName );

			try {
				return IOUtils.copy( imageData, fos );
			}
			finally {
				fos.flush();
				IOUtils.closeQuietly( fos );
				IOUtils.closeQuietly( imageData );
			}
		}
		catch ( Exception ioe ) {
			LOG.error( "Unable to save image: ", ioe );
			throw new ImageStoreOperationException( ioe );
		}
	}

	@Override
	public void deleteVariants( Image image ) {
		try {
			String path = createPathForVariant( image );
			final String fileNamePrefix = image.getId() + ".";

			File[] variants = new File( path ).listFiles( new FilenameFilter()
			{
				@Override
				public boolean accept( File dir, String name ) {
					return StringUtils.startsWithIgnoreCase( name, fileNamePrefix );
				}
			} );

			for ( File variant : variants ) {
				if ( !variant.delete() ) {
					LOG.warn( "Could not delete variant file {}", variant );
				}
			}
		}
		catch ( Exception e ) {
			LOG.warn( "Failed to delete variants for image ", e );
			throw new ImageStoreOperationException( e );
		}
	}

	private String createFileName( Image image ) {
		return image.getId() + "." + image.getImageType().getExtension();
	}

	private String createPathForOriginal( Image image ) {
		return createPathForImage( originalBasePath, image );
	}

	private String createPathForVariant( Image image ) {
		return createPathForImage( variantBasePath, image );
	}

	private String createPathForImage( File basePath, Image image ) {
		File path = new File( basePath.getAbsolutePath() + "/" + image.getApplicationId() +
				                      "/" + image.getFilePath() );
		if ( !path.exists() ) {
			if ( path.mkdirs() ) {
				LOG.debug( "Created new file location " + path.getAbsolutePath() );
			}
		}

		return path.getAbsolutePath();
	}
}
