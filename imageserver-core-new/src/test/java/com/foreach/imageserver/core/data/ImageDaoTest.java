package com.foreach.imageserver.core.data;

import com.foreach.imageserver.core.AbstractIntegrationTest;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ImageDaoTest extends AbstractIntegrationTest {

    @Autowired
    private ImageDao imageDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void insertAndGetById() {
        Image writtenImage = new Image();
        writtenImage.setRepositoryCode("the_repository_code");
        writtenImage.setDimensions(dimensions(111, 222));
        writtenImage.setImageType(ImageType.EPS);
        imageDao.insert(writtenImage);

        Image readImage = imageDao.getById(writtenImage.getImageId());
        assertEquals(writtenImage.getImageId(), readImage.getImageId());
        assertTrue(momentsAgo(readImage.getDateCreated()));
        assertEquals(writtenImage.getRepositoryCode(), readImage.getRepositoryCode());
        assertEquals(writtenImage.getDimensions().getWidth(), readImage.getDimensions().getWidth());
        assertEquals(writtenImage.getDimensions().getHeight(), readImage.getDimensions().getHeight());
        assertEquals(writtenImage.getImageType(), readImage.getImageType());
    }

    @Test
    public void updateParameters() {
        String imageSql = "INSERT INTO IMAGE ( imageId, created, repositoryCode, width, height, imageTypeId ) VALUES ( ?, ?, ?, ?, ?, ? )";
        jdbcTemplate.update(imageSql, 9998, new Date(2012, 11, 13), "the_repository_code", 6543, 3456, 1);

        Image updatedImage = new Image();
        updatedImage.setImageId(9998);
        updatedImage.setDimensions(dimensions(999, 888));
        updatedImage.setImageType(ImageType.TIFF);
        imageDao.updateParameters(updatedImage);

        Image readImage = imageDao.getById(9998);
        assertEquals(999, readImage.getDimensions().getWidth());
        assertEquals(888, readImage.getDimensions().getHeight());
        assertEquals(ImageType.TIFF, readImage.getImageType());
    }

    private Dimensions dimensions(int width, int height) {
        Dimensions dimensions = new Dimensions();
        dimensions.setWidth(width);
        dimensions.setHeight(height);
        return dimensions;
    }

    private boolean momentsAgo(Date date) {
        Date now = new Date();
        return (now.getTime() - date.getTime()) < 1000;
    }

}
