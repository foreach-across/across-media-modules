package com.foreach.imageserver.core.data;

import com.foreach.imageserver.core.DateUtils;
import com.foreach.imageserver.core.business.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.*;

public class TestImageVariantDao extends AbstractDaoTest {
    @Autowired
    private ImageVariantDao imageVariantDao;

    @Test
    public void getKnownImageModifications() {
        Collection<StoredImageVariant> modifications = imageVariantDao.getVariantsForImage(9999001);

        assertNotNull(modifications);
        assertEquals(2, modifications.size());

        // First modification should be smallest width
        Iterator<StoredImageVariant> iterator = modifications.iterator();

        StoredImageVariant modification = iterator.next();
        assertNotNull(modification);
        assertEquals(9999001, modification.getImageId());
        assertEquals(1024, modification.getVariant().getModifier().getWidth());
        assertEquals(768, modification.getVariant().getModifier().getHeight());
        assertEquals(ImageType.GIF, modification.getVariant().getModifier().getOutput());
        assertFalse(modification.getVariant().hasCrop());
        assertEquals(new Dimensions(), modification.getVariant().getModifier().getDensity());
        assertFalse(modification.getVariant().getModifier().isStretch());
        assertFalse(modification.getVariant().getModifier().isKeepAspect());
        assertEquals(DateUtils.parseDate("2013-07-31 13:40:33"), modification.getDateCreated());
        assertEquals(DateUtils.parseDate("2013-08-01 13:25:31"), modification.getDateUpdated());

        modification = iterator.next();
        assertNotNull(modification);
        assertEquals(9999001, modification.getImageId());
        assertEquals(1600, modification.getVariant().getModifier().getWidth());
        assertEquals(1200, modification.getVariant().getModifier().getHeight());
        assertEquals(ImageType.PNG, modification.getVariant().getModifier().getOutput());
        assertEquals(new Crop(100, 150, 500, 750, 1000, 2000), modification.getVariant().getCrop());
        assertEquals(new Dimensions(300, 600), modification.getVariant().getModifier().getDensity());
        assertTrue(modification.getVariant().getModifier().isStretch());
        assertTrue(modification.getVariant().getModifier().isKeepAspect());
        assertEquals(DateUtils.parseDate("2013-07-31 13:40:33"), modification.getDateCreated());
        assertEquals(DateUtils.parseDate("2013-08-01 13:25:31"), modification.getDateUpdated());
    }

    @Test
    public void createReadUpdateDelete() {
        StoredImageVariant inserted = createModification();

        imageVariantDao.insertVariant(inserted);

        StoredImageVariant fetched =
                imageVariantDao.getVariant(inserted.getImageId(), inserted.getVariant().getModifier());

        compareModifications(inserted, fetched);
        assertNotNull(fetched.getDateCreated());
        assertNull(fetched.getDateUpdated());

        StoredImageVariant modified = modify(fetched);
        imageVariantDao.updateVariant(modified);

        fetched = imageVariantDao.getVariant(inserted.getImageId(), inserted.getVariant().getModifier());
        compareModifications(modified, fetched);
        assertNotNull(fetched.getDateUpdated());

        imageVariantDao.deleteVariant(inserted.getImageId(), inserted.getVariant().getModifier());
    }

    private StoredImageVariant createModification() {
        StoredImageVariant mod = new StoredImageVariant();
        mod.setImageId(9999001);

        ImageVariant modifier = new ImageVariant();
        modifier.getModifier().setWidth(500);
        modifier.getModifier().setHeight(400);
        modifier.setCrop(new Crop(5, 15, 100, 110, 200, 300));
        modifier.getModifier().setDensity(250, 500);
        modifier.getModifier().setKeepAspect(false);
        modifier.getModifier().setStretch(true);

        mod.setVariant(modifier);

        return mod;
    }

    private StoredImageVariant modify(StoredImageVariant modification) {
        ImageVariant modifier = new ImageVariant();
        modifier.getModifier().setWidth(1900);
        modifier.getModifier().setHeight(1440);
        modifier.setCrop(new Crop());
        modifier.getModifier().setDensity(900, 1200);
        modifier.getModifier().setKeepAspect(true);
        modifier.getModifier().setStretch(false);
        modifier.getModifier().setOutput(ImageType.PNG);

        modification.setVariant(modifier);
        return modification;
    }

    private void compareModifications(StoredImageVariant left, StoredImageVariant right) {
        assertEquals(left.getImageId(), right.getImageId());
        assertEquals(left.getVariant(), right.getVariant());
    }
}
