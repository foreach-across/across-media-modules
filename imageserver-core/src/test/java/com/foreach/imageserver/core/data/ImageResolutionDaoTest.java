package com.foreach.imageserver.core.data;

import com.foreach.imageserver.core.AbstractIntegrationTest;
import com.foreach.imageserver.core.business.ImageResolution;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.Assert.*;

public class ImageResolutionDaoTest extends AbstractIntegrationTest {

    @Autowired
    private ImageResolutionDao imageResolutionDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void getById() {
        String resolutionSql = "INSERT INTO IMAGE_RESOLUTION ( id, width, height ) VALUES ( ?, ?, ? )";
        jdbcTemplate.update(resolutionSql, 101010, 111, 222);

        ImageResolution imageResolution = imageResolutionDao.getById(101010);
        assertNotNull(imageResolution);
        assertEquals(101010, imageResolution.getId());
        assertEquals(111, imageResolution.getWidth());
        assertEquals(222, imageResolution.getHeight());
    }

    @Test
    public void getForContext() {
        String contextSql = "INSERT INTO CONTEXT ( id, code ) VALUES ( ?, ? )";
        jdbcTemplate.update(contextSql, 10, "the_application_code");
        jdbcTemplate.update(contextSql, 11, "the_other_application_code");

        String resolutionSql = "INSERT INTO IMAGE_RESOLUTION ( id, width, height ) VALUES ( ?, ?, ? )";
        jdbcTemplate.update(resolutionSql, 10, 111, 222);
        jdbcTemplate.update(resolutionSql, 11, null, 333);
        jdbcTemplate.update(resolutionSql, 12, 444, null);
        jdbcTemplate.update(resolutionSql, 14, 555, 666);

        String linkSql = "INSERT INTO CONTEXT_IMAGE_RESOLUTION ( contextId, imageResolutionId ) VALUES ( ?, ? )";
        jdbcTemplate.update(linkSql, 10, 10);
        jdbcTemplate.update(linkSql, 10, 11);
        jdbcTemplate.update(linkSql, 10, 12);
        jdbcTemplate.update(linkSql, 11, 14);
        jdbcTemplate.update(linkSql, 11, 11);
        jdbcTemplate.update(linkSql, 11, 12);

        List<ImageResolution> imageResolutions = imageResolutionDao.getForContext(10);
        assertEquals(3, imageResolutions.size());

        assertEquals(10, imageResolutions.get(0).getId());
        assertEquals(111, imageResolutions.get(0).getWidth());
        assertEquals(222, imageResolutions.get(0).getHeight());

        assertEquals(11, imageResolutions.get(1).getId());
        assertNull(imageResolutions.get(1).getWidth());
        assertEquals(333, imageResolutions.get(1).getHeight());

        assertEquals(12, imageResolutions.get(2).getId());
        assertEquals(444, imageResolutions.get(2).getWidth());
        assertNull(imageResolutions.get(2).getHeight());
    }

}
