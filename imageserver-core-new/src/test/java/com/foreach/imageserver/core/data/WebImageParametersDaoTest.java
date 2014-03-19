package com.foreach.imageserver.core.data;

import com.foreach.imageserver.core.AbstractIntegrationTest;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageType;
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
        String imageSql = "INSERT INTO IMAGE ( imageId, created, repositoryCode ) VALUES ( ?, ?, ? )";
        jdbcTemplate.update(imageSql, 121212, new Date(2012, 11, 13), "the_repository_code");

        WebImageParameters writtenParameters = new WebImageParameters();
        writtenParameters.setImageId(121212);
        writtenParameters.setUrl("dit_is_een_url");
        writtenParameters.setDimensions(dimensions(123, 321));
        writtenParameters.setImageType(ImageType.TIFF);

        webImageParametersDao.insert(writtenParameters);

        WebImageParameters readParameters = webImageParametersDao.getById(121212);
        assertNotNull(readParameters);
        assertEquals(writtenParameters.getImageId(), readParameters.getImageId());
        assertEquals(writtenParameters.getUrl(), readParameters.getUrl());
        assertEquals(writtenParameters.getDimensions().getWidth(), readParameters.getDimensions().getWidth());
        assertEquals(writtenParameters.getDimensions().getHeight(), readParameters.getDimensions().getHeight());
        assertEquals(writtenParameters.getImageType(), readParameters.getImageType());
    }

    private Dimensions dimensions(int width, int height) {
        Dimensions dimensions = new Dimensions();
        dimensions.setWidth(width);
        dimensions.setHeight(height);
        return dimensions;
    }

}
