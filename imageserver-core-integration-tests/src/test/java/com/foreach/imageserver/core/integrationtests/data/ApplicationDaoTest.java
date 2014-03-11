package com.foreach.imageserver.core.integrationtests.data;

import com.foreach.imageserver.core.business.Application;
import com.foreach.imageserver.core.data.ApplicationDao;
import com.foreach.imageserver.core.integrationtests.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ApplicationDaoTest extends AbstractIntegrationTest {

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void getById() {
        String sql = "INSERT INTO APPLICATION ( id, name, active, code, created, updated ) VALUES ( ?, ?, ?, ?, ?, ? )";
        jdbcTemplate.update(sql, 10, "the_application_name", true, "the_application_code", new Date(2012, 11, 13), new Date(2012, 11, 14));

        Application application = applicationDao.getById(10);

        assertEquals(10, application.getId());
        assertEquals("the_application_name", application.getName());
        assertTrue(application.isActive());
        assertEquals("the_application_code", application.getCode());
        assertEquals(new Date(2012, 11, 13), application.getDateCreated());
        assertEquals(new Date(2012, 11, 14), application.getDateUpdated());
    }
}
