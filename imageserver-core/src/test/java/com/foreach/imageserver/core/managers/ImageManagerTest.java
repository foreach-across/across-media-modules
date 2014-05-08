package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.AbstractIntegrationTest;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageType;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.Assert.*;

public class ImageManagerTest extends AbstractIntegrationTest {

    @Autowired
    private ImageManager imageManager;

    @Autowired
    private CacheManager cacheManager;

    @Test
    public void insertUpdateGet() {
        Image insertedImage = image(new Date(2013, 0, 1), 100, 200, ImageType.GIF);
        imageManager.insert(insertedImage);

        Cache cache = cacheManager.getCache("images");
        assertNotNull(cache);
        assertNull(cache.get("byId-" + insertedImage.getId()));

        Image retrievedImage = imageManager.getById(insertedImage.getId());
        shouldBeEqual(insertedImage, retrievedImage);
        assertSame(retrievedImage, cache.get("byId-" + insertedImage.getId()).getObjectValue());

        insertedImage.setDimensions(new Dimensions(500, 600));
        insertedImage.setImageType(ImageType.EPS);
        imageManager.updateParameters(insertedImage);

        assertNull(cache.get("byId-" + insertedImage.getId()));

        Image retrievedAgainImage = imageManager.getById(insertedImage.getId());
        shouldBeEqual(insertedImage, retrievedAgainImage);
        assertSame(retrievedAgainImage, cache.get("byId-" + insertedImage.getId()).getObjectValue());
    }

    private Image image(Date date, int width, int height, ImageType imageType) {
        Image image = new Image();
        image.setDateCreated(date);
        image.setDimensions(new Dimensions(width, height));
        image.setImageType(imageType);
        return image;
    }

    private void shouldBeEqual(Image lhsImage, Image rhsImage) {
        assertEquals(lhsImage.getId(), rhsImage.getId());
        assertEquals(lhsImage.getDateCreated(), rhsImage.getDateCreated());
        assertEquals(lhsImage.getDimensions().getWidth(), rhsImage.getDimensions().getWidth());
        assertEquals(lhsImage.getDimensions().getHeight(), rhsImage.getDimensions().getHeight());
        assertEquals(lhsImage.getImageType(), rhsImage.getImageType());
    }

}
