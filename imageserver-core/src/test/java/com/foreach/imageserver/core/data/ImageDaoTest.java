package com.foreach.imageserver.core.data;

import com.foreach.imageserver.core.AbstractIntegrationTest;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class ImageDaoTest extends AbstractIntegrationTest {

    @Autowired
    private ImageDao imageDao;

    @Test
    public void insertAndGetById() {
        Image writtenImage = new Image();
        writtenImage.setExternalId("external_id");
        writtenImage.setDateCreated(new Date());
        writtenImage.setDimensions(dimensions(111, 222));
        writtenImage.setImageType(ImageType.EPS);
        imageDao.insert(writtenImage);

        Image readImage = imageDao.getById(writtenImage.getId());
        assertEquals(writtenImage.getId(), readImage.getId());
        assertEquals(writtenImage.getExternalId(), readImage.getExternalId());
        assertEquals(writtenImage.getDateCreated(), readImage.getDateCreated());
        assertEquals(writtenImage.getDimensions().getWidth(), readImage.getDimensions().getWidth());
        assertEquals(writtenImage.getDimensions().getHeight(), readImage.getDimensions().getHeight());
        assertEquals(writtenImage.getImageType(), readImage.getImageType());
    }

    @Test
    public void insertAndGetByExternalId() {
        Image writtenImage = new Image();
        writtenImage.setExternalId("external_id");
        writtenImage.setDateCreated(new Date());
        writtenImage.setDimensions(dimensions(111, 222));
        writtenImage.setImageType(ImageType.EPS);
        imageDao.insert(writtenImage);

        Image readImage = imageDao.getByExternalId(writtenImage.getExternalId());
        assertEquals(writtenImage.getId(), readImage.getId());
        assertEquals(writtenImage.getExternalId(), readImage.getExternalId());
        assertEquals(writtenImage.getDateCreated(), readImage.getDateCreated());
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
