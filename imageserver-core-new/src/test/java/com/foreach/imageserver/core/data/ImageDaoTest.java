package com.foreach.imageserver.core.data;

import com.foreach.imageserver.core.AbstractIntegrationTest;
import com.foreach.imageserver.core.business.Image;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ImageDaoTest extends AbstractIntegrationTest {

    @Autowired
    private ImageDao imageDao;

    @Test
    public void insertAndGetById() {
        Image writtenImage = new Image();
        writtenImage.setImageId(3216);
        writtenImage.setRepositoryCode("the_repository_code");
        imageDao.insert(writtenImage);

        Image readImage = imageDao.getById(3216);
        assertEquals(writtenImage.getImageId(), readImage.getImageId());
        assertTrue(momentsAgo(readImage.getDateCreated()));
        assertEquals(writtenImage.getRepositoryCode(), readImage.getRepositoryCode());
    }

    private boolean momentsAgo(Date date) {
        Date now = new Date();
        return (now.getTime() - date.getTime()) < 1000;
    }

}
