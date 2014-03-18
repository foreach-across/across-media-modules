package com.foreach.imageserver.core.integrationtests.data;

import com.foreach.imageserver.core.business.Context;
import com.foreach.imageserver.core.data.ContextDao;
import com.foreach.imageserver.core.integrationtests.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.Assert.assertEquals;

public class ContextDaoTest extends AbstractIntegrationTest {

    @Autowired
    private ContextDao contextDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void getById() {
        String sql = "INSERT INTO CONTEXT ( id, name ) VALUES ( ?, ? )";
        jdbcTemplate.update(sql, 10, "the_application_name");

        Context context = contextDao.getById(10);

        assertEquals(10, context.getId());
        assertEquals("the_application_name", context.getName());
    }
}
