package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.services.exceptions.ImageStoreException;
import com.foreach.imageserver.core.transformers.StreamImageSource;
import com.foreach.imageserver.logging.LogHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Still to verify and/or implement:
 * - Files.createTempPath needs to be atomic.
 * - Files.copy with option REPLACE_EXISTING should never cause the temp file to not exist.
 * <p/>
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

		createDirectories( tempFolder );
		createDirectories( originalsFolder );
		createDirectories( variantsFolder );
	}

	@Override
	public void storeOriginalImage( Image image, byte[] imageBytes ) {
		try( InputStream imageStream = new ByteArrayInputStream( imageBytes ) ) {
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
		Path targetPath = getTargetPath( image );
		createFoldersSafely( targetPath.getParent() );
		writeSafely( imageStream, targetPath );
	}

	@Override
	public StreamImageSource getOriginalImage( Image image ) {
		Path targetPath = getTargetPath( image );

		if ( LOG.isDebugEnabled() ) {
			LOG.debug( "Original image {} - expected location {}", image.getId(), targetPath );
		}
		return read( targetPath, image.getImageType() );
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
		Path targetPath = getTargetPath( image, context, imageResolution, imageVariant );
		createFoldersSafely( targetPath.getParent() );
		writeSafely( imageStream, targetPath );
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
		Path targetPath = getTargetPath( image, context, imageResolution, imageVariant );
		return read( targetPath, imageVariant.getOutputType() );
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

		Path targetPath = getTargetPath( image, context, imageResolution, imageVariant );

		try {
			Files.deleteIfExists( targetPath );
		}
		catch ( IOException e ) {
			/**
			 * Unfortunately, we need to ignore this error.
			 *
			 * Experiments have revealed that removing the same file from different threads concurrently should work,
			 * provided that we ignore all IOExceptions. I could not quickly find a good way around this;
			 * Files::deleteIfExists raises exceptions when the file it is trying to consider is suddenly missing.
			 *
			 * This was also tested on a linux server on an NFS mount.
			 */
		}
	}

	@Override
	public void removeVariants( long imageId ) {
		if ( imageId == 0 ) {
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

			Image image = imageService.getById( imageId );

			Collection<ImageContext> contexts = imageContextService.getAllContexts();
			for (ImageContext context: contexts){
				// only need to walk the specific variant folder of this image (within this context)
				Path imageSpecificVariantsFolder  = variantsFolder.resolve( context.getCode() ).resolve( image.getVariantPath() );
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
		}
		catch ( IOException e ) {
			// I'm not really sure whether this will ever happen, given that the above implementation catches all
			// io exceptions.
			LOG.error( "Encountered failure while removing variants - ImageStoreServiceImpl#removeVariants: imageId={}",
			           imageId, e );
			throw new ImageStoreException( e );
		}
	}

	private Path getTargetPath( Image image ) {
		/**
		 * We may at some point need image repositories that cannot re-retrieve their images. For this reason we
		 * create a per-repository parent folder, so we can easily distinguish between repositories.
		 */

		String fileName = constructFileName( image );
		return originalsFolder.resolve( image.getOriginalPath() ).resolve( fileName );
	}

	private Path getTargetPath( Image image,
	                            ImageContext context,
	                            ImageResolution imageResolution,
	                            ImageVariant imageVariant ) {
		if ( image == null || context == null || imageResolution == null || imageVariant == null ) {
			LOG.warn(
					"Null parameters not allowed - ImageStoreServiceImpl#getTargetPath: image={}, context={}, imageResolution={}, imageVariant={}",
					LogHelper.flatten( image, context, imageResolution, imageVariant ) );
		}

		/**
		 * We may at some point need image repositories that cannot re-create their images. For this reason we
		 * create a per-repository parent folder, so we can easily distinguish between repositories.
		 */

		String fileName = constructFileName( image, imageResolution, imageVariant );

		return variantsFolder.resolve( context.getCode() ).resolve( image.getVariantPath() ).resolve( fileName );
	}

	private String constructFileName( Image image, ImageResolution imageResolution, ImageVariant imageVariant ) {
		if ( image == null || imageResolution == null || imageVariant == null ) {
			LOG.warn(
					"Null parameters not allowed - ImageStoreServiceImpl#constructFileName: image={}, modification={}, imageVariant={}",
					LogHelper.flatten( image, imageResolution, imageVariant ) );
		}

		StringBuilder fileNameBuilder = new StringBuilder();
		fileNameBuilder.append( variantFileNamePrefix( image.getId() ) );
		//if (imageResolution.getWidth() != null) {
		fileNameBuilder.append( 'w' );
		fileNameBuilder.append( imageResolution.getWidth() );
		fileNameBuilder.append( '-' );
		//}
		//if (imageResolution.getHeight() != null) {
		fileNameBuilder.append( 'h' );
		fileNameBuilder.append( imageResolution.getHeight() );
		//}
		if (imageVariant.getBoundingBox() != null){
			fileNameBuilder.append( '-' );
			fileNameBuilder.append( "bw" );
			fileNameBuilder.append( imageVariant.getBoundingBox().getWidth() );
			fileNameBuilder.append( '-' );
			fileNameBuilder.append( "bh" );
			fileNameBuilder.append( imageVariant.getBoundingBox().getHeight() );
		}

		fileNameBuilder.append( '.' );
		fileNameBuilder.append( imageVariant.getOutputType().getExtension() );

		return fileNameBuilder.toString();
	}

	private String constructFileName( Image image ) {

		return String.valueOf( image.getId() ) + '.' + image.getImageType().getExtension();
	}

	private String variantFileNamePrefix( long imageId ) {
		return String.valueOf( imageId ) + '-';
	}

	private void writeSafely( InputStream inputStream, Path targetPath ) {
		try {
			Path temporaryPath = Files.createTempFile( tempFolder, "image", ".tmp" );
			Files.copy( inputStream, temporaryPath, StandardCopyOption.REPLACE_EXISTING );
			Files.move( temporaryPath, targetPath, StandardCopyOption.REPLACE_EXISTING,
			            StandardCopyOption.ATOMIC_MOVE );
			setFilePermissionsWithoutFailing( targetPath );
		}
		catch ( IOException e ) {
			LOG.error( "Error while creating folder - ImageStoreServiceImpl#writeSafely: targetPath={}", targetPath,
			           e );
			throw new ImageStoreException( e );
		}
	}

	private StreamImageSource read( Path targetPath, ImageType imageType ) {
		// Do not use .exists() kind of logic here! Open the file and check for an exception instead.
		// This will ensure that we can read the full file contents, even should the file be deleted or replaced while
		// we are busy with it.

		InputStream imageStream = null;
		try {
			imageStream = Files.newInputStream( targetPath );
		}
		catch ( IOException e ) {
			// Let imageStream be null.
		}

		StreamImageSource imageSource = null;
		if ( imageStream != null ) {
			imageSource = new StreamImageSource( imageType, imageStream );
		}

		return imageSource;
	}

	private void createFoldersSafely( Path path ) {
		/**
		 * Although I'm not entirely sure of this, I suspect that createDirectories might cause issues when multiple
		 * actors try to create the same folder structure simultaneously. For this reason, we will simply retry
		 * the creation a few times. This will most likely suffice as we know the folder structure will just be created
		 * once and will be left untouched afterwards.
		 */

		boolean done = false;

		for ( int i = 0; i < 3 && !done; ++i ) {
			done = createFoldersWithoutFailing( path );
			if ( !done ) {
				sleep( 20 );
			}
		}

		if ( !done ) {
			createFoldersAndFail( path );
		}
	}

	private boolean createFoldersWithoutFailing( Path path ) {
		boolean done = false;
		try {
			createDirectories( path );
			done = true;
		}
		catch ( IOException e ) {
			// Ignore failure.
		}
		return done;
	}

	private void createFoldersAndFail( Path path ) {
		try {
			createDirectories( path );
		}
		catch ( IOException e ) {
			LOG.error( "Error while creating folder - ImageStoreServiceImpl#createFoldersAndFail: path={}", path, e );
			throw new ImageStoreException( e );
		}
	}

	private void sleep( long millis ) {
		try {
			Thread.sleep( millis );
		}
		catch ( InterruptedException ie ) {
			// Ignore.
		}
	}

	private void createDirectories( Path path ) throws IOException {
		/**
		 * The variant of Files::createDirectories that allows for setting the file permissions in one go doesn't seem
		 * to work on an NFS volume. Since folders are always created once and then left untouched, we can safely use
		 * two separate calls here.
		 */

		Files.createDirectories( path );
		if ( !CollectionUtils.isEmpty( folderPermissions ) ) {
			Files.setPosixFilePermissions( path, folderPermissions );
		}
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
