package com.foreach.imageserver.core.integrationtests.data;

import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.data.ImageResolutionDao;
import com.foreach.imageserver.core.integrationtests.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ImageResolutionDaoTest extends AbstractIntegrationTest {

    @Autowired
    private ImageResolutionDao imageResolutionDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void getForContext() {
        String contextSql = "INSERT INTO CONTEXT ( id, name ) VALUES ( ?, ? )";
        jdbcTemplate.update(contextSql, 10, "the_application_name");
        jdbcTemplate.update(contextSql, 11, "the_other_application_name");

        String resolutionSql = "INSERT INTO IMAGE_RESOLUTION ( id, width, height ) VALUES ( ?, ?, ? )";
        jdbcTemplate.update(resolutionSql, 10, 111, 222);
        jdbcTemplate.update(resolutionSql, 11, null, 333);
        jdbcTemplate.update(resolutionSql, 12, 444, null);
        jdbcTemplate.update(resolutionSql, 14, 555, 666);

        String linkSql = "INSERT INTO CONTEXT_IMAGE_RESOLUTIONS ( contextId, imageResolutionId ) VALUES ( ?, ? )";
        jdbcTemplate.update(linkSql, 10, 10);
        jdbcTemplate.update(linkSql, 10, 11);
        jdbcTemplate.update(linkSql, 10, 12);
        jdbcTemplate.update(linkSql, 11, 14);
        jdbcTemplate.update(linkSql, 11, 11);
        jdbcTemplate.update(linkSql, 11, 12);

        List<ImageResolution> imageResolutions = imageResolutionDao.getForContext(10);
        assertEquals(3, imageResolutions.size());

        assertEquals(10, imageResolutions.get(0).getId().intValue());
        assertEquals(111, imageResolutions.get(0).getWidth().intValue());
        assertEquals(222, imageResolutions.get(0).getHeight().intValue());

        assertEquals(11, imageResolutions.get(1).getId().intValue());
        assertNull(imageResolutions.get(1).getWidth());
        assertEquals(333, imageResolutions.get(1).getHeight().intValue());

        assertEquals(12, imageResolutions.get(2).getId().intValue());
        assertEquals(444, imageResolutions.get(2).getWidth().intValue());
        assertNull(imageResolutions.get(2).getHeight());
    }

}
