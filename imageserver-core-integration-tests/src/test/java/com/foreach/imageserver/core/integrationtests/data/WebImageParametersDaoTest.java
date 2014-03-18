package com.foreach.imageserver.core.integrationtests.data;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.business.WebImageParameters;
import com.foreach.imageserver.core.data.WebImageParametersDao;
import com.foreach.imageserver.core.integrationtests.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class WebImageParametersDaoTest extends AbstractIntegrationTest {

    @Autowired
    private WebImageParametersDao webImageParametersDao;

    @Test
    public void insertAndGetByParameters() {
        WebImageParameters writtenParameters = new WebImageParameters();
        writtenParameters.setUrl("dit_is_een_url");
        writtenParameters.setDimensions(dimensions(123, 321));
        writtenParameters.setImageType(ImageType.TIFF);

        webImageParametersDao.insert(writtenParameters);
        assertNotNull(writtenParameters.getId());

        WebImageParameters readParameters = webImageParametersDao.getByParameters("dit_is_een_url");
        assertNotNull(readParameters);
        assertEquals(writtenParameters.getId(), readParameters.getId());
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
