package com.foreach.imageserver.core.repositories;

import com.foreach.imageserver.core.AbstractIntegrationTest;
import com.foreach.imageserver.core.business.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;

public class ImageModificationRepositoryTest extends AbstractIntegrationTest
{

	@Autowired
	private ImageContextRepository contextRepository;
	@Autowired
	private ImageRepository imageRepository;
	@Autowired
	private ImageResolutionRepository imageResolutionRepository;
	@Autowired
	private ImageModificationRepository imageModificationRepository;

	@Test
	public void insertAndGetById() {
		createContext( 1010L, "the_application_code_1010" );
		createImage( 9998, "externalId", new Dimensions( 100, 100 ), ImageType.PNG );
		createImageResolution( -8L, 8888, 1616 );

		Crop writtenCrop = new Crop();
		writtenCrop.setX( 100 );
		writtenCrop.setY( 101 );
		writtenCrop.setWidth( 102 );
		writtenCrop.setHeight( 103 );

		Dimensions writtenDensity = new Dimensions();
		writtenDensity.setWidth( 106 );
		writtenDensity.setHeight( 107 );

		ImageModification writtenImageModification = new ImageModification();
		writtenImageModification.setImageId( 9998 );
		writtenImageModification.setContextId( 1010 );
		writtenImageModification.setResolutionId( -8 );
		writtenImageModification.setCrop( writtenCrop );
		writtenImageModification.setDensity( writtenDensity );

		imageModificationRepository.create( writtenImageModification );

		ImageModification readImageModification = imageModificationRepository.getById( 9998, 1010, -8 );
		assertEquals( writtenImageModification.getImageId(), readImageModification.getImageId() );
		assertEquals( writtenImageModification.getContextId(), readImageModification.getContextId() );
		assertEquals( writtenImageModification.getResolutionId(), readImageModification.getResolutionId() );
		assertEquals( writtenImageModification.getCrop().getX(), readImageModification.getCrop().getX() );
		assertEquals( writtenImageModification.getCrop().getY(), readImageModification.getCrop().getY() );
		assertEquals( writtenImageModification.getCrop().getWidth(), readImageModification.getCrop().getWidth() );
		assertEquals( writtenImageModification.getCrop().getHeight(), readImageModification.getCrop().getHeight() );
		assertEquals( writtenImageModification.getDensity().getWidth(), readImageModification.getDensity().getWidth() );
		assertEquals( writtenImageModification.getDensity().getHeight(),
		              readImageModification.getDensity().getHeight() );
	}

	private void createContext( Long id, String code ) {
		ImageContext context = new ImageContext();
		context.setId( id );
		context.setCode( code );
		contextRepository.save( context );
	}

	@Test
	public void getModifications() {
		createContext( 8010L, "the_application_code_8010" );
		createContext( 8011L, "the_application_code_8011" );

		createImage( 19998, "externalId19998", new Dimensions( 100, 100 ), ImageType.PNG );
		createImage( 19999, "externalId19999", new Dimensions( 100, 100 ), ImageType.PNG );

		createImageResolution( -88L, 1211, 2222 );
		createImageResolution( -99L, 1311, 2222 );
		createImageResolution( -100L, 1411, 2222 );
		createImageResolution( -101L, 1511, 2222 );

		imageModificationRepository.create( someModification( 8010, 19998, -88 ) );
		imageModificationRepository.create( someModification( 8010, 19998, -99 ) );
		imageModificationRepository.create( someModification( 8011, 19998, -88 ) );
		imageModificationRepository.create( someModification( 8010, 19999, -88 ) );

		List<ImageModification> modifications = imageModificationRepository.getModifications( 19998, 8010 );
		assertEquals( 2, modifications.size() );

		assertEquals( 8010, modifications.get( 0 ).getContextId() );
		assertEquals( 19998, modifications.get( 0 ).getImageId() );
		assertEquals( -99, modifications.get( 0 ).getResolutionId() );

		assertEquals( 8010, modifications.get( 1 ).getContextId() );
		assertEquals( 19998, modifications.get( 1 ).getImageId() );
		assertEquals( -88, modifications.get( 1 ).getResolutionId() );
	}

	private void createImageResolution( Long id, int width, int height ) {
		ImageResolution imageResolution = new ImageResolution();
		imageResolution.setId( id );
		imageResolution.setWidth( width );
		imageResolution.setHeight( height );
		imageResolutionRepository.save( imageResolution );
	}

	@Test
	public void getAllModifications() {
		createContext( 7010L, "the_application_code_7010" );
		createContext( 7011L, "the_application_code_7011" );

		createImage( 29998, "externalId_29998", new Dimensions( 100, 100 ), ImageType.PNG );
		createImage( 29999, "externalId_29999", new Dimensions( 100, 100 ), ImageType.PNG );

		createImageResolution( -288L, 1211, 2242 );
		createImageResolution( -299L, 1311, 2242 );

		imageModificationRepository.create( someModification( 7010, 29998, -288 ) );
		imageModificationRepository.create( someModification( 7010, 29998, -299 ) );
		imageModificationRepository.create( someModification( 7011, 29998, -288 ) );
		imageModificationRepository.create( someModification( 7010, 29999, -288 ) );

		List<ImageModification> modifications = imageModificationRepository.getAllModifications( 29998 );
		assertEquals( 3, modifications.size() );

		assertEquals( 7010, modifications.get( 0 ).getContextId() );
		assertEquals( 29998, modifications.get( 0 ).getImageId() );
		assertEquals( -299, modifications.get( 0 ).getResolutionId() );

		assertEquals( 7010, modifications.get( 1 ).getContextId() );
		assertEquals( 29998, modifications.get( 1 ).getImageId() );
		assertEquals( -288, modifications.get( 1 ).getResolutionId() );

		assertEquals( 7011, modifications.get( 2 ).getContextId() );
		assertEquals( 29998, modifications.get( 2 ).getImageId() );
		assertEquals( -288, modifications.get( 2 ).getResolutionId() );
	}

	private void createImage( long id, String externalId, Dimensions dimensions, ImageType imageType ) {
		Image image = new Image();
		image.setId( id );
		image.setExternalId( externalId );
		image.setDimensions( dimensions );
		image.setImageType( imageType );

		Calendar c = Calendar.getInstance();
		c.set( 2012, Calendar.NOVEMBER, 13, 0, 0 );

		image.setDateCreated( c.getTime() );
		image.setImageProfileId( ImageProfile.DEFAULT_PROFILE_ID );
		imageRepository.create( image );
	}

	@Test
	public void updateAndGetById() {
		createContext( 6666L, "code" );
		createImage( 78878, "externalId_78878", new Dimensions( 100, 100 ), ImageType.PNG );
		createImageResolution( 99994L, 934, 9843 );

		ImageModification imageModification = modification( 78878, 6666, 99994, 0, 1, 2, 3, 4, 5 );
		imageModificationRepository.create( imageModification );

		imageModificationRepository.update( modification( 78878, 6666, 99994, 10, 11, 12, 13, 14, 15 ) );

		ImageModification readImageModification = imageModificationRepository.getById( 78878, 6666, 99994 );
		assertEquals( 78878, readImageModification.getImageId() );
		assertEquals( 6666, readImageModification.getContextId() );
		assertEquals( 99994, readImageModification.getResolutionId() );
		assertEquals( 10, readImageModification.getCrop().getX() );
		assertEquals( 11, readImageModification.getCrop().getY() );
		assertEquals( 12, readImageModification.getCrop().getWidth() );
		assertEquals( 13, readImageModification.getCrop().getHeight() );
		assertEquals( 14, readImageModification.getDensity().getWidth() );
		assertEquals( 15, readImageModification.getDensity().getHeight() );
	}

	@Test
	public void hasModification() {
		ImageContext context6010 = new ImageContext();
		context6010.setId( 6010L );
		context6010.setCode( "the_application_code_6010" );
		contextRepository.save( context6010 );

		createImage( 39998, "externalId_39998", new Dimensions( 100, 100 ), ImageType.PNG );
		createImage( 49998, "externalId_49998", new Dimensions( 100, 100 ), ImageType.PNG );

		createImageResolution( 766L, 8473, 4398 );

		imageModificationRepository.create( someModification( 6010, 39998, 766 ) );

		assertTrue( imageModificationRepository.hasModification( 39998 ) );
		assertFalse( imageModificationRepository.hasModification( 49998 ) );
	}

	private ImageModification someModification( int contextId, int imageId, int imageResolutionId ) {
		Crop crop = new Crop();
		crop.setX( 100 );
		crop.setY( 101 );
		crop.setWidth( 102 );
		crop.setHeight( 103 );

		Dimensions density = new Dimensions();
		density.setWidth( 106 );
		density.setHeight( 107 );

		ImageModification modification = new ImageModification();
		modification.setImageId( imageId );
		modification.setContextId( contextId );
		modification.setResolutionId( imageResolutionId );
		modification.setCrop( crop );
		modification.setDensity( density );

		return modification;
	}

	private ImageModification modification( int imageId,
	                                        int contextId,
	                                        int resolutionId,
	                                        int cropX,
	                                        int cropY,
	                                        int cropWidth,
	                                        int cropHeight,
	                                        int densityWidth,
	                                        int densityHeight ) {
		Crop crop = new Crop();
		crop.setX( cropX );
		crop.setY( cropY );
		crop.setWidth( cropWidth );
		crop.setHeight( cropHeight );

		Dimensions density = new Dimensions();
		density.setWidth( densityWidth );
		density.setHeight( densityHeight );

		ImageModification modification = new ImageModification();
		modification.setImageId( imageId );
		modification.setContextId( contextId );
		modification.setResolutionId( resolutionId );
		modification.setCrop( crop );
		modification.setDensity( density );

		return modification;
	}

}
