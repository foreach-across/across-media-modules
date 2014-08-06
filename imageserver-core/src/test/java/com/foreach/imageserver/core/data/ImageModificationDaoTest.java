package com.foreach.imageserver.core.data;

import com.foreach.imageserver.core.AbstractIntegrationTest;
import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.repositories.ContextRepository;
import com.foreach.imageserver.core.repositories.ImageModificationRepository;
import com.foreach.imageserver.core.repositories.ImageRepository;
import com.foreach.imageserver.core.repositories.ImageResolutionRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class ImageModificationDaoTest extends AbstractIntegrationTest {

	@Autowired
	private ContextRepository contextRepository;
	@Autowired
	private ImageRepository imageRepository;
	@Autowired
	private ImageResolutionRepository imageResolutionRepository;
	@Autowired
	private ImageModificationRepository imageModificationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void insertAndGetById() {
	    Context context = new Context();
	    context.setId( 1010 );
	    context.setCode( "the_application_code_1010" );
	    contextRepository.create( context );

	    Image image = new Image();
	    image.setId( 9998 );
	    image.setExternalId( "externalId" );
	    image.setDimensions( new Dimensions( 100, 100 ) );
	    image.setImageType( ImageType.PNG );
	    image.setDateCreated( new Date( 2012, 11, 13 ) );
	    image.setImageProfileId( ImageProfile.DEFAULT_PROFILE_ID );
	    imageRepository.create( image );

	    ImageResolution imageResolution = new ImageResolution();
	    imageResolution.setId( -8 );
	    imageResolution.setWidth( 8888 );
	    imageResolution.setHeight( 1616 );
	    imageResolutionRepository.create( imageResolution );


        Crop writtenCrop = new Crop();
        writtenCrop.setX(100);
        writtenCrop.setY(101);
        writtenCrop.setWidth(102);
        writtenCrop.setHeight(103);

        Dimensions writtenDensity = new Dimensions();
        writtenDensity.setWidth(106);
        writtenDensity.setHeight(107);

        ImageModification writtenImageModification = new ImageModification();
        writtenImageModification.setImageId(9998);
        writtenImageModification.setContextId(1010);
        writtenImageModification.setResolutionId(-8);
        writtenImageModification.setCrop(writtenCrop);
        writtenImageModification.setDensity(writtenDensity);

	    imageModificationRepository.create( writtenImageModification );

        ImageModification readImageModification = imageModificationRepository.getById(9998, 1010, -8);
        assertEquals(writtenImageModification.getImageId(), readImageModification.getImageId());
        assertEquals(writtenImageModification.getContextId(), readImageModification.getContextId());
        assertEquals(writtenImageModification.getResolutionId(), readImageModification.getResolutionId());
        assertEquals(writtenImageModification.getCrop().getX(), readImageModification.getCrop().getX());
        assertEquals(writtenImageModification.getCrop().getY(), readImageModification.getCrop().getY());
        assertEquals(writtenImageModification.getCrop().getWidth(), readImageModification.getCrop().getWidth());
        assertEquals(writtenImageModification.getCrop().getHeight(), readImageModification.getCrop().getHeight());
        assertEquals(writtenImageModification.getDensity().getWidth(), readImageModification.getDensity().getWidth());
        assertEquals(writtenImageModification.getDensity().getHeight(), readImageModification.getDensity().getHeight());
    }

    @Test
    public void getModifications() {
	    Context context8010 = new Context();
	    context8010.setId( 8010 );
	    context8010.setCode( "the_application_code_8010" );
	    contextRepository.create( context8010 );

	    Context context8011 = new Context();
	    context8011.setId( 8011 );
	    context8011.setCode( "the_application_code_8011" );
	    contextRepository.create( context8011 );

	    Image image19998 = new Image();
	    image19998.setId( 19998 );
	    image19998.setExternalId( "externalId1" );
	    image19998.setDimensions( new Dimensions( 100, 100 ) );
	    image19998.setImageType( ImageType.PNG );
	    image19998.setDateCreated( new Date(2012, 11, 13) );
	    image19998.setImageProfileId( ImageProfile.DEFAULT_PROFILE_ID );
	    imageRepository.create( image19998 );

	    Image image19999 = new Image();
	    image19999.setId( 19999 );
	    image19999.setExternalId( "externalId2" );
	    image19999.setDimensions( new Dimensions( 100, 100 ) );
	    image19999.setImageType( ImageType.PNG );
	    image19999.setDateCreated( new Date(2012, 11, 13) );
	    image19999.setImageProfileId( ImageProfile.DEFAULT_PROFILE_ID );
	    imageRepository.create( image19999 );

		createImageResolution( -88, 1211, 2222);
	    createImageResolution( -99, 1311, 2222);
	    createImageResolution( -100, 1411, 2222);
	    createImageResolution( -101, 1511, 2222);

	    imageModificationRepository.create( someModification( 8010, 19998, -88 ) );
	    imageModificationRepository.create( someModification( 8010, 19998, -99 ) );
	    imageModificationRepository.create( someModification( 8011, 19998, -88 ) );
	    imageModificationRepository.create( someModification( 8010, 19999, -88 ) );

        List<ImageModification> modifications = imageModificationRepository.getModifications(19998, 8010);
        assertEquals(2, modifications.size());

	    assertEquals(8010, modifications.get(0).getContextId());
	    assertEquals(19998, modifications.get(0).getImageId());
	    assertEquals(-99, modifications.get(0).getResolutionId());

        assertEquals(8010, modifications.get(1).getContextId());
        assertEquals(19998, modifications.get(1).getImageId());
        assertEquals(-88, modifications.get(1).getResolutionId());
    }

	private void createImageResolution( int id, int width, int height ) {
		ImageResolution imageResolution = new ImageResolution();
		imageResolution.setId( id );
		imageResolution.setWidth( width );
		imageResolution.setHeight( height );
		imageResolutionRepository.create( imageResolution );
	}

	@Test
    public void getAllModifications() {
        String contextSql = "INSERT INTO CONTEXT ( id, code ) VALUES ( ?, ? )";
        jdbcTemplate.update(contextSql, 1010, "the_application_code");
        jdbcTemplate.update(contextSql, 1011, "the_application_code_1");

        String imageSql = "INSERT INTO IMAGE ( id, externalId, created, width, height, imageTypeId ) VALUES ( ?, ?, ?, 100, 100, 1 )";
        jdbcTemplate.update(imageSql, 9998, "externalId1", new Date(2012, 11, 13));
        jdbcTemplate.update(imageSql, 9999, "externalId2", new Date(2012, 11, 13));

        String imageResolutionSql = "INSERT INTO IMAGE_RESOLUTION ( id, width, height ) VALUES ( ?, ?, ? )";
        jdbcTemplate.update(imageResolutionSql, 8, 1111, 2222);
        jdbcTemplate.update(imageResolutionSql, 9, 1111, 2222);

	    imageModificationRepository.create( someModification( 1010, 9998, 8 ) );
	    imageModificationRepository.create( someModification( 1010, 9998, 9 ) );
	    imageModificationRepository.create( someModification( 1011, 9998, 8 ) );
	    imageModificationRepository.create( someModification( 1010, 9999, 8 ) );

        List<ImageModification> modifications = imageModificationRepository.getAllModifications(9998);
        assertEquals(3, modifications.size());

        assertEquals(1010, modifications.get(0).getContextId());
        assertEquals(9998, modifications.get(0).getImageId());
        assertEquals(8, modifications.get(0).getResolutionId());

        assertEquals(1010, modifications.get(1).getContextId());
        assertEquals(9998, modifications.get(1).getImageId());
        assertEquals(9, modifications.get(1).getResolutionId());

        assertEquals(1011, modifications.get(2).getContextId());
        assertEquals(9998, modifications.get(2).getImageId());
        assertEquals(8, modifications.get(2).getResolutionId());
    }

    @Test
    public void updateAndGetById() {
        String contextSql = "INSERT INTO CONTEXT ( id, code ) VALUES ( ?, ? )";
        jdbcTemplate.update(contextSql, 1010, "code");

        String imageSql = "INSERT INTO IMAGE ( id, externalId, created, width, height, imageTypeId ) VALUES ( ?, ?, ?, 100, 100, 1 )";
        jdbcTemplate.update(imageSql, 9998, "externalId", new Date(2012, 11, 13));

        String imageResolutionSql = "INSERT INTO IMAGE_RESOLUTION ( id, width, height ) VALUES ( ?, ?, ? )";
        jdbcTemplate.update(imageResolutionSql, 8, 1111, 2222);

        String modificationSql = "INSERT INTO IMAGE_MODIFICATION ( imageId, contextId, resolutionId, densityWidth, densityHeight, cropX, cropY, cropWidth, cropHeight ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ? )";
        jdbcTemplate.update(modificationSql, 9998, 1010, 8, 0, 1, 2, 3, 4, 5);

	    imageModificationRepository.update(modification(9998, 1010, 8, 10, 11, 12, 13, 14, 15));

        ImageModification readImageModification = imageModificationRepository.getById(9998, 1010, 8);
        assertEquals(9998, readImageModification.getImageId());
        assertEquals(1010, readImageModification.getContextId());
        assertEquals(8, readImageModification.getResolutionId());
        assertEquals(10, readImageModification.getCrop().getX());
        assertEquals(11, readImageModification.getCrop().getY());
        assertEquals(12, readImageModification.getCrop().getWidth());
        assertEquals(13, readImageModification.getCrop().getHeight());
        assertEquals(14, readImageModification.getDensity().getWidth());
        assertEquals(15, readImageModification.getDensity().getHeight());
    }

    @Test
    public void hasModification() {
        String applicationSql = "INSERT INTO CONTEXT ( id, code ) VALUES ( ?, ? )";
        jdbcTemplate.update(applicationSql, 1, "the_application_code");

        String imageSql = "INSERT INTO IMAGE ( id, externalId, created, width, height, imageTypeId ) VALUES ( ?, ?, ?, 100, 100, 1 )";
        jdbcTemplate.update(imageSql, 1, "externalId1", new Date(2012, 11, 13));
        jdbcTemplate.update(imageSql, 2, "externalId2", new Date(2012, 11, 13));

        String imageResolutionSql = "INSERT INTO IMAGE_RESOLUTION ( id, width, height ) VALUES ( ?, ?, ? )";
        jdbcTemplate.update(imageResolutionSql, 1, 100, 200);

	    imageModificationRepository.create( someModification( 1, 1, 1 ) );

        assertTrue(imageModificationRepository.hasModification(1));
        assertFalse(imageModificationRepository.hasModification(2));
    }

    private ImageModification someModification(int contextId, int imageId, int imageResolutionId) {
        Crop crop = new Crop();
        crop.setX(100);
        crop.setY(101);
        crop.setWidth(102);
        crop.setHeight(103);

        Dimensions density = new Dimensions();
        density.setWidth(106);
        density.setHeight(107);

        ImageModification modification = new ImageModification();
        modification.setImageId(imageId);
        modification.setContextId(contextId);
        modification.setResolutionId(imageResolutionId);
        modification.setCrop(crop);
        modification.setDensity(density);

        return modification;
    }

    private ImageModification modification(int imageId, int contextId, int resolutionId, int cropX, int cropY, int cropWidth, int cropHeight, int densityWidth, int densityHeight) {
        Crop crop = new Crop();
        crop.setX(cropX);
        crop.setY(cropY);
        crop.setWidth(cropWidth);
        crop.setHeight(cropHeight);

        Dimensions density = new Dimensions();
        density.setWidth(densityWidth);
        density.setHeight(densityHeight);

        ImageModification modification = new ImageModification();
        modification.setImageId(imageId);
        modification.setContextId(contextId);
        modification.setResolutionId(resolutionId);
        modification.setCrop(crop);
        modification.setDensity(density);

        return modification;
    }

}
