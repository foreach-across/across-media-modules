package com.foreach.imageserver.core.integrationtests.data;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.business.WebOriginalImage;
import com.foreach.imageserver.core.data.WebOriginalImageDao;
import com.foreach.imageserver.core.integrationtests.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class WebOriginalImageDaoTest extends AbstractIntegrationTest {

    @Autowired
    private WebOriginalImageDao webOriginalImageDao;

    @Test
    public void insertAndGetByParameters() {
        WebOriginalImage writtenParameters = new WebOriginalImage();
        writtenParameters.setUrl("dit_is_een_url");
        writtenParameters.setDimensions(dimensions(123, 321));
        writtenParameters.setImageType(ImageType.TIFF);

        webOriginalImageDao.insert(writtenParameters);
        assertNotNull(writtenParameters.getId());

        WebOriginalImage readParameters = webOriginalImageDao.getByParameters("dit_is_een_url");
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
