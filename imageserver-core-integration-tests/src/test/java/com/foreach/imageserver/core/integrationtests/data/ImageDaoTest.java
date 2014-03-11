package com.foreach.imageserver.core.integrationtests.data;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.data.ImageDao;
import com.foreach.imageserver.core.integrationtests.AbstractIntegrationTest;
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
        String applicationSql = "INSERT INTO APPLICATION ( id, name, active, code, created, updated ) VALUES ( ?, ?, ?, ?, ?, ? )";
        jdbcTemplate.update(applicationSql, 1010, "the_application_name", true, "the_application_code", new Date(2012, 11, 13), new Date(2012, 11, 14));

        Image writtenImage = new Image();
        writtenImage.setImageId(3216);
        writtenImage.setApplicationId(1010);
        writtenImage.setDimensions(dimensions(1122, 2211));
        writtenImage.setImageType(ImageType.SVG);
        writtenImage.setRepositoryCode("the_repository_code");
        imageDao.insert(writtenImage);

        Image readImage = imageDao.getById(1010, 3216);
        assertEquals(writtenImage.getImageId(), readImage.getImageId());
        assertEquals(writtenImage.getApplicationId(), readImage.getApplicationId());
        assertEquals(writtenImage.getDimensions().getWidth(), readImage.getDimensions().getWidth());
        assertEquals(writtenImage.getDimensions().getHeight(), readImage.getDimensions().getHeight());
        assertEquals(writtenImage.getImageType(), readImage.getImageType());
        assertTrue(momentsAgo(readImage.getDateCreated()));
        assertEquals(writtenImage.getRepositoryCode(), readImage.getRepositoryCode());
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
