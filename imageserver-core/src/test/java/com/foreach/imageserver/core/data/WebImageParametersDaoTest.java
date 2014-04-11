package com.foreach.imageserver.core.data;

import com.foreach.imageserver.core.AbstractIntegrationTest;
import com.foreach.imageserver.core.business.WebImageParameters;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class WebImageParametersDaoTest extends AbstractIntegrationTest {

    @Autowired
    private WebImageParametersDao webImageParametersDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void insertAndGetById() {
        String imageSql = "INSERT INTO IMAGE ( imageId, created, repositoryCode, width, height, imageTypeId ) VALUES ( ?, ?, ?, ?, ?, ? )";
        jdbcTemplate.update(imageSql, 121212, new Date(2012, 11, 13), "the_repository_code", 111, 222, 1);

        WebImageParameters writtenParameters = new WebImageParameters();
        writtenParameters.setImageId(121212);
        writtenParameters.setUrl("dit_is_een_url");

        webImageParametersDao.insert(writtenParameters);

        WebImageParameters readParameters = webImageParametersDao.getById(121212);
        assertNotNull(readParameters);
        assertEquals(writtenParameters.getImageId(), readParameters.getImageId());
        assertEquals(writtenParameters.getUrl(), readParameters.getUrl());
    }

}
