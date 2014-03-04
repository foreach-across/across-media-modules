package com.foreach.imageserver.core.data;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageType;
import com.foreach.imageserver.core.business.ImageVariant;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestRegisterImageVariantDao extends AbstractDaoTest {

    @Autowired
    private ImageVariantDao imageVariantDao;

    @Test
    public void testInsertAndRetrieve() {
        int applicationId = 1;

        ImageVariant imageVariant1 = new ImageVariant();
        imageVariant1.setWidth(100);

        ImageVariant imageVariant2 = new ImageVariant();
        imageVariant2.setWidth(100);
        imageVariant2.setHeight(100);
        imageVariant2.setKeepAspect(false);
        imageVariant2.setDensity(new Dimensions(1, 2));
        imageVariant2.setOutput(ImageType.EPS);
        imageVariant2.setStretch(true);

        imageVariantDao.insertVariant(applicationId, imageVariant1);
        imageVariantDao.insertVariant(applicationId, imageVariant2);

        List<ImageVariant> variants = imageVariantDao.getVariantsForApplication(applicationId);

        assertNotNull(variants);
        assertEquals(2, variants.size());

        ImageVariant retrievedVariant1 = variants.get(0);
        ImageVariant retrievedVariant2 = variants.get(1);

        assertEquals(imageVariant1, retrievedVariant1);
        assertEquals(imageVariant2, retrievedVariant2);
    }

}
