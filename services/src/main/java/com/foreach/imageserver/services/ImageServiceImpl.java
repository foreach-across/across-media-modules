package com.foreach.imageserver.services;

import com.foreach.imageserver.business.*;
import com.foreach.imageserver.data.ImageDao;
import com.foreach.imageserver.data.ImageModificationDao;
import com.foreach.imageserver.services.repositories.RepositoryLookupResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ImageServiceImpl implements ImageService
{
	private static final Logger LOG = LoggerFactory.getLogger( ImageServiceImpl.class );

	@Autowired
	private ImageDao imageDao;

	@Autowired
	private ImageModificationDao modificationDao;

	@Autowired
	private ImageStoreService imageStoreService;

	@Autowired
	private ImageTransformService imageTransformService;

	@Autowired
	private TempFileService tempFileService;

	@Override
	public Image getImageByKey( String key, int applicationId ) {
		return imageDao.getImageByKey( key, applicationId );
	}

	@Transactional
	@Override
	public void save( Image image, RepositoryLookupResult lookupResult ) {
		image.setImageType( lookupResult.getImageType() );

		ImageFile tempFile = tempFileService.createImageFile( lookupResult.getImageType(), lookupResult.getContent() );
		image.setDimensions( imageTransformService.calculateDimensions( tempFile ) );

		boolean isInsert = isNewImage( image );

		if ( isInsert ) {
			image.setFilePath( imageStoreService.generateRelativeImagePath( image ) );
			imageDao.insertImage( image );
		}

		ImageFile savedFile = imageStoreService.saveImage( image, tempFile );

		image.setFileSize( savedFile.getFileSize() );

		imageDao.updateImage( image );
		if ( !isInsert ) {
			imageStoreService.deleteVariants( image );
		}
	}

	private boolean isNewImage( Image image ) {
		return image.getId() <= 0;
	}

	@Transactional
	@Override
	public void registerModification( Image image, Dimensions dimensions, ImageModifier modifier ) {
		//ImageFile imageFile = fetchImageFile( image, modifier );

		Dimensions normalized = dimensions.normalize( image.getDimensions() );
		ImageModification modification = modificationDao.getModification( image.getId(), normalized );

		if ( modification == null ) {
			modification = new ImageModification();
			modification.setImageId( image.getId() );
			modification.setDimensions( normalized );
			modification.setModifier( modifier );

			modificationDao.insertModification( modification );
		}
		else {
			modification.setModifier( modifier );
			modificationDao.updateModification( modification );
		}
	}

	@Override
	public ImageFile fetchImageFile( Image image, ImageModifier modifier ) {
		if ( LOG.isDebugEnabled() ) {
			LOG.debug( "Requesting image {} with modifier {}", image.getId(), modifier );
		}

		ImageModifier normalized = modifier.normalize( image.getDimensions() );
		ImageModifier saveAsModifier = normalized;
		verifyOutputType( image.getImageType(), normalized );

		if ( LOG.isDebugEnabled() ) {
			LOG.debug( "Requesting image {} with normalized modifier {}", image.getId(), normalized );
		}

		ImageFile file = imageStoreService.getImageFile( image, normalized );

		if ( file == null ) {
			ImageModification modification = modificationDao.getModification( image.getId(),
			                                                                  new Dimensions( normalized.getWidth(),
			                                                                                  normalized.getHeight() ) );

			if ( modification != null ) {
				normalized = modification.getModifier().normalize( image.getDimensions() );
				verifyOutputType( image.getImageType(), normalized );

				file = imageStoreService.getImageFile( image, normalized );
			}
		}

		if ( file == null ) {
			ImageFile original = imageStoreService.getImageFile( image );
			ImageFile modified = imageTransformService.apply( original, normalized );

			file = imageStoreService.saveImage( image, saveAsModifier, modified );
		}

		return file;
	}

	private void verifyOutputType( ImageType original, ImageModifier modifier ) {
		if ( modifier.getOutput() == null && !modifier.isEmpty() ) {
			modifier.setOutput( ImageType.getPreferredOutputType( original ) );
		}
	}

	@Transactional
	@Override
	public void delete( Image image, boolean variantsOnly ) {
		if ( variantsOnly ) {
			// Delete physical variant files
			imageStoreService.deleteVariants( image );
		}
		else {
			// First delete the database entry - this avoids requests coming in
			imageDao.deleteImage( image.getId() );

			// Delete the actual physical files
			imageStoreService.delete( image );
		}
	}
}
