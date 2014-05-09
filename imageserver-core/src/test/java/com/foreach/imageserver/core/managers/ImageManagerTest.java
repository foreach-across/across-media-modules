package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.AbstractIntegrationTest;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageType;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Date;

import static org.junit.Assert.*;

public class ImageManagerTest extends AbstractIntegrationTest {

    @Autowired
    private ImageManager imageManager;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void insertGetById() {
        Image insertedImage = image("externalId", new Date(2013, 0, 1), 100, 200, ImageType.GIF);
        imageManager.insert(insertedImage);

        Cache cache = cacheManager.getCache("images");
        assertNotNull(cache);
        assertNull(cache.get("byId-" + insertedImage.getId()));

        Image retrievedImage = imageManager.getById(insertedImage.getId());
        shouldBeEqual(insertedImage, retrievedImage);
        assertSame(retrievedImage, cache.get("byId-" + insertedImage.getId()).get());

        jdbcTemplate.execute("DELETE FROM IMAGE");

        Image retrievedAgainImage = imageManager.getById(insertedImage.getId());
        shouldBeEqual(insertedImage, retrievedAgainImage);
        assertSame(retrievedImage, retrievedAgainImage);
    }

    @Test
    public void insertGetByExternalId() {
        // Things will go wrong if this null result is cached.
        assertNull(imageManager.getByExternalId("externalId"));

        Image insertedImage = image("externalId", new Date(2013, 0, 1), 100, 200, ImageType.GIF);
        imageManager.insert(insertedImage);

        Cache cache = cacheManager.getCache("images");
        assertNotNull(cache);
        assertNull(cache.get("byExternalId-" + insertedImage.getExternalId()));

        Image retrievedImage = imageManager.getByExternalId(insertedImage.getExternalId());
        shouldBeEqual(insertedImage, retrievedImage);
        assertSame(retrievedImage, cache.get("byExternalId-" + insertedImage.getExternalId()).get());

        jdbcTemplate.execute("DELETE FROM IMAGE");

        Image retrievedAgainImage = imageManager.getByExternalId(insertedImage.getExternalId());
        shouldBeEqual(insertedImage, retrievedAgainImage);
        assertSame(retrievedImage, retrievedAgainImage);
    }

    private Image image(String externalId, Date date, int width, int height, ImageType imageType) {
        Image image = new Image();
        image.setExternalId(externalId);
        image.setDateCreated(date);
        image.setDimensions(new Dimensions(width, height));
        image.setImageType(imageType);
        return image;
    }

    private void shouldBeEqual(Image lhsImage, Image rhsImage) {
        assertEquals(lhsImage.getId(), rhsImage.getId());
        assertEquals(lhsImage.getExternalId(), rhsImage.getExternalId());
        assertEquals(lhsImage.getDateCreated(), rhsImage.getDateCreated());
        assertEquals(lhsImage.getDimensions().getWidth(), rhsImage.getDimensions().getWidth());
        assertEquals(lhsImage.getDimensions().getHeight(), rhsImage.getDimensions().getHeight());
        assertEquals(lhsImage.getImageType(), rhsImage.getImageType());
    }

}
