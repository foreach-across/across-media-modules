package com.foreach.imageserver.core.repositories;

import com.foreach.imageserver.core.AbstractIntegrationTest;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageProfile;
import com.foreach.imageserver.core.business.ImageType;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ImageRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ImageRepository imageRepository;
	@Autowired
	private ImageProfileRepository imageProfileRepository;

    @Test
    public void insertAndGetById() {
	    ImageProfile imageProfile = new ImageProfile();
	    imageProfile.setName( "dummy_profile" );
	    imageProfileRepository.create( imageProfile );

        Image writtenImage = new Image();
        writtenImage.setExternalId("external_id");
        writtenImage.setDateCreated(new Date());
        writtenImage.setDimensions(dimensions(111, 222));
        writtenImage.setImageType(ImageType.EPS);
	    writtenImage.setImageProfileId( imageProfile.getId() );
	    imageRepository.create(writtenImage);

        Image readImage = imageRepository.getById(writtenImage.getId());
        assertEquals(writtenImage.getId(), readImage.getId());
        assertEquals(writtenImage.getExternalId(), readImage.getExternalId());
	    assertTrue( DateUtils.truncatedEquals( writtenImage.getDateCreated(), readImage.getDateCreated(), Calendar.SECOND ) );
        assertEquals(writtenImage.getDimensions().getWidth(), readImage.getDimensions().getWidth());
        assertEquals(writtenImage.getDimensions().getHeight(), readImage.getDimensions().getHeight());
        assertEquals(writtenImage.getImageType(), readImage.getImageType());
    }

    @Test
    public void insertAndGetByExternalId() {
	    ImageProfile imageProfile = new ImageProfile();
	    imageProfile.setName( "dummy_profile 2" );
	    imageProfileRepository.create( imageProfile );

        Image writtenImage = new Image();
        writtenImage.setExternalId("external_id2");
        writtenImage.setDateCreated(new Date());
        writtenImage.setDimensions(dimensions(111, 222));
        writtenImage.setImageType(ImageType.EPS);
	    writtenImage.setImageProfileId( imageProfile.getId() );
	    imageRepository.create(writtenImage);

        Image readImage = imageRepository.getByExternalId(writtenImage.getExternalId());
        assertEquals(writtenImage.getId(), readImage.getId());
        assertEquals(writtenImage.getExternalId(), readImage.getExternalId());
	    assertTrue( DateUtils.truncatedEquals( writtenImage.getDateCreated(), readImage.getDateCreated(), Calendar.SECOND ) );
        assertEquals(writtenImage.getDimensions().getWidth(), readImage.getDimensions().getWidth());
        assertEquals(writtenImage.getDimensions().getHeight(), readImage.getDimensions().getHeight());
        assertEquals(writtenImage.getImageType(), readImage.getImageType());
    }

    private Dimensions dimensions(int width, int height) {
        Dimensions dimensions = new Dimensions();
        dimensions.setWidth(width);
        dimensions.setHeight(height);
        return dimensions;
    }

}
