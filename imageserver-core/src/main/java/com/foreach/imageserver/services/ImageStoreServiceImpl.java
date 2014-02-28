package com.foreach.imageserver.services;

import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.services.exceptions.ImageStoreOperationException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class ImageStoreServiceImpl implements ImageStoreService
{
	private static final Logger LOG = LoggerFactory.getLogger( ImageStoreServiceImpl.class );
	private static final FastDateFormat PATH_FORMAT = FastDateFormat.getInstance( "/yyyy/MM/dd/" );

	private final File originalBasePath;
	private final File variantBasePath;

	@Autowired
	private TempFileService tempFileService;

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
	public ImageFile saveImage( Image image, ImageFile imageFile ) {
		try {
			File physical = new File( generateFullImagePath( image ) );

			if ( tempFileService.isTempFile( imageFile ) ) {
				return tempFileService.move( imageFile, physical );
			}
			else {
				InputStream imageData = null;
				FileOutputStream fos = new FileOutputStream( physical );

				try {
					imageData = imageFile.openContentStream();
					long contentLength = IOUtils.copy( imageData, fos );
					return new ImageFile( image.getImageType(), physical, contentLength );
				}
				finally {
					fos.flush();
					IOUtils.closeQuietly( fos );
					IOUtils.closeQuietly( imageData );
				}
			}
		}
		catch ( Exception e ) {
			LOG.error( "Unable to save original image {}, exception: {}", image, e );
			throw new ImageStoreOperationException( e );
		}
	}

	@Override
	public ImageFile saveImage( Image image, ImageModifier modifier, ImageFile file ) {
		try {
			File physical = new File( generateFullImagePath( image, modifier ) );

			if ( tempFileService.isTempFile( file ) ) {
				return tempFileService.move( file, physical );
			}
			else {
				FileOutputStream fos = new FileOutputStream( physical );

				InputStream imageData = file.openContentStream();

				try {
					long contentLength = IOUtils.copy( imageData, fos );

					return new ImageFile( file.getImageType(), physical, contentLength );
				}
				finally {
					fos.flush();
					IOUtils.closeQuietly( fos );
					IOUtils.closeQuietly( imageData );
				}
			}
		}
		catch ( Exception e ) {
			LOG.error( "Unable to save variant image {}, exception: {}", image, e );
			throw new ImageStoreOperationException( e );
		}
	}

	@Override
	public void delete( Image image ) {
		try {
			String path = createPathForOriginal( image );
			String fileName = createFileName( image, null );

			File physicalFile = new File( path, fileName );

			if ( physicalFile.exists() && !physicalFile.delete() ) {
				LOG.error( "Could not delete original file {}", physicalFile );
				throw new ImageStoreOperationException( "Could not delete original file" );
			}
		}
		catch ( ImageStoreOperationException isoe ) {
			throw isoe;
		}
		catch ( Exception e ) {
			LOG.warn( "Failed to delete original for image {}, exception: {}", image, e );
			throw new ImageStoreOperationException( e );
		}

		deleteVariants( image );
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
			LOG.warn( "Failed to delete variants for image {}, exception: {}", image, e );
			throw new ImageStoreOperationException( e );
		}
	}

	@Override
	public ImageFile getImageFile( Image image ) {
		return getImageFile( image, null );
	}

	@Override
	public ImageFile getImageFile( Image image, ImageModifier modifier ) {
		try {
			File physicalFile = new File( generateFullImagePath( image, modifier ) );

			if ( physicalFile.exists() ) {
				ImageType imageType = modifier != null ? modifier.getOutput() : image.getImageType();

				return new ImageFile( imageType != null ? imageType : image.getImageType(), physicalFile.length(),
				                      new FileInputStream( physicalFile ) );
			}

			return null;
		}
		catch ( Exception e ) {
			LOG.error( "Was not able to get image file for image {}, exception: {}", image, e );
			throw new ImageStoreOperationException( e );
		}
	}

	@Override
	public String generateFullImagePath( Image image ) {
		return generateFullImagePath( image, null );
	}

	@Override
	public String generateFullImagePath( Image image, ImageModifier modifier ) {
		String path = isOriginalImage( modifier ) ? createPathForOriginal( image ) : createPathForVariant( image );
		String fileName = createFileName( image, modifier );

		return new File( path, fileName ).getAbsolutePath();
	}

	private boolean isOriginalImage( ImageModifier modifier ) {
		return modifier == null || modifier.isEmpty();
	}

	private String createFileName( Image image, ImageModifier modifier ) {
		if ( !isOriginalImage( modifier ) ) {
			ImageType imageType = modifier.getOutput();

			StringBuilder path = new StringBuilder();
			path.append( image.getId() );

			// Output resolution: 100x100
			path.append( "." ).append( modifier.getWidth() ).append( "x" ).append( modifier.getHeight() );

			// Crop
			if ( modifier.hasCrop() ) {
				Crop crop = modifier.getCrop();
				path.append( ".[" ).append( crop.getWidth() ).append( "x" ).append( crop.getHeight() ).append(
						"+" ).append( crop.getX() ).append( "+" ).append( crop.getY() ).append( "]" );
			}

			// Image type extension
			path.append( "." ).append(
					imageType != null ? imageType.getExtension() : image.getImageType().getExtension() );

			return path.toString();
		}
		else {
			return image.getId() + "." + image.getImageType().getExtension();
		}
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
		if ( !path.exists() && path.mkdirs() ) {
			LOG.debug( "Created new file location " + path.getAbsolutePath() );
		}

		return path.getAbsolutePath();
	}
}
