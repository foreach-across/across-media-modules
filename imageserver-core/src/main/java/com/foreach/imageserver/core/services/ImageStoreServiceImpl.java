package com.foreach.imageserver.core.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.services.FileManager;
import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.services.exceptions.ImageStoreException;
import com.foreach.imageserver.core.transformers.StreamImageSource;
import com.foreach.imageserver.logging.LogHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static com.foreach.imageserver.core.config.ServicesConfiguration.*;

/**
 * Still to verify and/or implement:
 * - Files.createTempPath needs to be atomic.
 * - Files.copy with option REPLACE_EXISTING should never cause the temp file to not exist.
 * <p>
 * TODO Resolve the above.
 */
@Service
public class ImageStoreServiceImpl implements ImageStoreService
{
	private static final Logger LOG = LoggerFactory.getLogger( ImageStoreServiceImpl.class );

	@Autowired
	private ImageContextService imageContextService;
	@Autowired
	private ImageService imageService;
	@Autowired
	private FileManager fileManager;
	@Autowired
	private DefaultImageFileDescriptorFactory defaultImageFileDescriptorFactory;

	private final Path tempFolder;
	private final Path originalsFolder;
	private final Path variantsFolder;

	private final Set<PosixFilePermission> folderPermissions;
	private final Set<PosixFilePermission> filePermissions;

	public ImageStoreServiceImpl( Path imageStoreFolder,
	                              String folderPermissions,
	                              String filePermissions ) throws IOException {
		this.folderPermissions = toPermissions( folderPermissions );
		this.filePermissions = toPermissions( filePermissions );

		tempFolder = imageStoreFolder.resolve( "temp" );
		originalsFolder = imageStoreFolder.resolve( "originals" );
		variantsFolder = imageStoreFolder.resolve( "variants" );

	}

	@Override
	public void storeOriginalImage( Image image, byte[] imageBytes ) {
		try (InputStream imageStream = new ByteArrayInputStream( imageBytes )) {
			this.storeOriginalImage( image, imageStream );
		}
		catch ( Exception e ) {
			LOG.error(
					"Encountered failure while storing original image - ImageStoreServiceImpl#storeOriginalImage: image={}",
					LogHelper.flatten( image ), e );
			throw new RuntimeException( e );
		}
	}

	@Override
	public void storeOriginalImage( Image image, InputStream imageStream ) {
		writeSafely( imageStream, getOriginalFileDescriptor( image ) );
	}

	@Override
	public StreamImageSource getOriginalImage( Image image ) {
		FileDescriptor fileDescriptor = getOriginalFileDescriptor( image );

		if ( LOG.isDebugEnabled() ) {
			LOG.debug( "Original image {} - expected location {}/{}", image.getId(), fileDescriptor );
		}

		return read( fileDescriptor, image.getImageType() );
	}

	@Override
	public void removeOriginal( Image image ) {
		LOG.info( "Deleting original image file for {}", image );
		FileDescriptor descriptor = getOriginalFileDescriptor( image );
		if ( fileManager.exists( descriptor ) ) {
			boolean deleted = fileManager.delete( descriptor );
			LOG.debug( "Original image file for {} was {} deleted", image, deleted ? "successfully" : "not" );
		}
		else {
			LOG.debug( "Original image file for {} does not exist.", image );
		}
	}

	private FileDescriptor getOriginalFileDescriptor( Image image ) {
		return defaultImageFileDescriptorFactory.createForOriginal( image );
	}

	@Override
	public void storeVariantImage( Image image,
	                               ImageContext context,
	                               ImageResolution imageResolution,
	                               ImageVariant imageVariant,
	                               InputStream imageStream ) {
		if ( image == null || context == null || imageResolution == null || imageVariant == null || imageStream == null ) {
			LOG.warn(
					"Null parameters not allowed - ImageStoreServiceImpl#storeVariantImage: image={}, context={}, imageResolution={}, imageVariant={}, imageStream={}",
					LogHelper.flatten( image, context, imageResolution, imageVariant, imageStream ) );
		}
		writeSafely( imageStream, getVariantsFileDescriptor( image, context, imageResolution, imageVariant ) );
	}

	@Override
	public StreamImageSource getVariantImage( Image image,
	                                          ImageContext context,
	                                          ImageResolution imageResolution,
	                                          ImageVariant imageVariant ) {
		if ( image == null || context == null || imageResolution == null || imageVariant == null ) {
			LOG.warn(
					"Null parameters not allowed - ImageStoreServiceImpl#getVariantImage: image={}, context={}, imageResolution={}, imageVariant={}",
					LogHelper.flatten( image, context, imageResolution, imageVariant ) );
		}
		FileDescriptor fileDescriptor = getVariantsFileDescriptor( image, context, imageResolution, imageVariant );
		return read( fileDescriptor, imageVariant.getOutputType() );
	}

	@Override
	public void removeVariantImage( Image image,
	                                ImageContext context,
	                                ImageResolution imageResolution,
	                                ImageVariant imageVariant ) {
		if ( image == null || context == null || imageResolution == null || imageVariant == null ) {
			LOG.warn(
					"Null parameters not allowed - ImageStoreServiceImpl#removeVariantImage: image={}, context={}, imageResolution={}, imageVariant={}",
					LogHelper.flatten( image, context, imageResolution, imageVariant ) );
		}

		FileDescriptor descriptor = getVariantsFileDescriptor( image, context, imageResolution, imageVariant );
		if ( fileManager.exists( descriptor ) ) {
			boolean deleted = fileManager.delete( descriptor );
			LOG.debug( "Original image file for {} was {} deleted", image, deleted ? "successfully" : "not" );
		}
		else {
			LOG.debug( "Original image file for {} does not exist.", image );
		}
	}

	private FileDescriptor getVariantsFileDescriptor( Image image,
	                                                  ImageContext context,
	                                                  ImageResolution imageResolution, ImageVariant imageVariant ) {
		return defaultImageFileDescriptorFactory.createForVariant( image,context,imageResolution,imageVariant );
	}

	//TODO refactor to use FileManagerModule?
	@Override
	public void removeVariants( Long imageId ) {
		if ( imageId == null || imageId == 0 ) {
			LOG.warn( "Null parameters not allowed - ImageStoreServiceImpl#removeVariants: imageId={}", imageId );
		}

		final String variantFileNamePrefix = variantFileNamePrefix( imageId );

		/**
		 * Experiments have revealed that removing the same set of files from different threads
		 * concurrently should work, provided that we ignore all IOExceptions. I could not quickly
		 * find a good way around this; Files::walkFileTree and Files::deleteIfExists both raise
		 * exceptions when the file they are trying to consider is suddenly missing.
		 *
		 * This was also tested on a linux server on an NFS mount.
		 */
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();

			Image image = imageService.getById( imageId );

			Collection<ImageContext> contexts = imageContextService.getAllContexts();
			for ( ImageContext context : contexts ) {
				// only need to walk the specific variant folder of this image (within this context)
				Path imageSpecificVariantsFolder =
						variantsFolder.resolve( context.getCode() ).resolve( image.getVariantPath() );
				Files.walkFileTree( imageSpecificVariantsFolder, new SimpleFileVisitor<Path>()
				{
					@Override
					public FileVisitResult visitFile( Path file, BasicFileAttributes attrs ) throws IOException {
						if ( file.getFileName().toString().startsWith( variantFileNamePrefix ) ) {
							try {
								Files.deleteIfExists( file );
							}
							catch ( IOException e ) {
								// Unfortunately, we need to ignore this error. (See comment above)
							}
						}
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFileFailed( Path file, IOException exc ) throws IOException {
						// Unfortunately, we need to ignore this error. (See comment above)
						return FileVisitResult.CONTINUE;
					}
				} );
			}

			stopWatch.stop();
			LOG.debug( "Variant cleanup for imageId {} completed in {} ms", imageId, stopWatch.getTime() );
		}
		catch ( IOException e ) {
			// I'm not really sure whether this will ever happen, given that the above implementation catches all
			// io exceptions.
			LOG.error( "Encountered failure while removing variants - ImageStoreServiceImpl#removeVariants: imageId={}",
			           imageId, e );
			throw new ImageStoreException( e );
		}
	}

	private String getFolderName( Image image ) {
		return image.isTemporaryImage() ? "" : image.getOriginalPath();
	}

	private String getFolderName( Image image,
	                              ImageContext context ) {
		return context.getCode() + "/" + image.getVariantPath();
	}

	private String variantFileNamePrefix( long imageId ) {
		return String.valueOf( imageId ) + '-';
	}

	private void writeSafely( InputStream inputStream, FileDescriptor target ) {
		try {
			FileDescriptor temp = fileManager.save( TEMP_REPOSITORY, inputStream );
			fileManager.move( temp, target );
			File file = fileManager.getAsFile( target );
			setFilePermissionsWithoutFailing( file.toPath() );
		}
		catch ( Exception e ) {
			LOG.error( "Error while creating folder - ImageStoreServiceImpl#writeSafely: targetPath={}", target,
			           e );
			throw new ImageStoreException( e );
		}
	}

	private StreamImageSource read( FileDescriptor fileDescriptor, ImageType imageType ) {
		InputStream imageStream = fileManager.getInputStream( fileDescriptor );
		if ( imageStream != null ) {
			return new StreamImageSource( imageType, imageStream );
		}
		return null;
	}

	private void setFilePermissionsWithoutFailing( Path path ) {
		/**
		 * The variant of Files::createTempFile that allows for setting the file permissions in one go doesn't seem
		 * to work on an NFS volume. Fixing the permissions of the temp file before moving it to its final destination
		 * also fails. As a last resort, we blindly try to fix the permissions on the target file. As it may have been
		 * manipulated by external actors in the mean time, we ignore all errors.
		 */

		if ( !CollectionUtils.isEmpty( filePermissions ) ) {
			try {
				Files.setPosixFilePermissions( path, filePermissions );
			}
			catch ( IOException e ) {
				// Must be ignored, see comment above.
			}
		}
	}

	private Set<PosixFilePermission> toPermissions( String permissionsString ) {
		if ( StringUtils.isNotBlank( permissionsString ) ) {
			return PosixFilePermissions.fromString( permissionsString );
		}
		else {
			return Collections.emptySet();
		}
	}

}
