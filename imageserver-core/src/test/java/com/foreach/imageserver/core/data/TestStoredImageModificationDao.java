package com.foreach.imageserver.core.data;

import com.foreach.imageserver.core.DateUtils;
import com.foreach.imageserver.core.business.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.*;

public class TestStoredImageModificationDao extends AbstractDaoTest {

    @Autowired
    private StoredImageModificationDao storedImageModificationDao;

    @Test
    public void getKnownImageModifications() {
        Collection<StoredImageModification> modifications = storedImageModificationDao.getModificationsForImage(9999001);

        assertNotNull(modifications);
        assertEquals(2, modifications.size());

        // First modification should be smallest width
        Iterator<StoredImageModification> iterator = modifications.iterator();

        StoredImageModification modification = iterator.next();
        assertNotNull(modification);
        assertEquals(9999001, modification.getImageId());
        assertEquals(1024, modification.getModification().getVariant().getWidth());
        assertEquals(768, modification.getModification().getVariant().getHeight());
        assertEquals(ImageType.GIF, modification.getModification().getVariant().getOutput());
        assertFalse(modification.getModification().hasCrop());
        assertEquals(new Dimensions(), modification.getModification().getVariant().getDensity());
        assertFalse(modification.getModification().getVariant().isStretch());
        assertFalse(modification.getModification().getVariant().isKeepAspect());
        assertEquals(DateUtils.parseDate("2013-07-31 13:40:33"), modification.getDateCreated());
        assertEquals(DateUtils.parseDate("2013-08-01 13:25:31"), modification.getDateUpdated());

        modification = iterator.next();
        assertNotNull(modification);
        assertEquals(9999001, modification.getImageId());
        assertEquals(1600, modification.getModification().getVariant().getWidth());
        assertEquals(1200, modification.getModification().getVariant().getHeight());
        assertEquals(ImageType.PNG, modification.getModification().getVariant().getOutput());
        assertEquals(new Crop(100, 150, 500, 750, 1000, 2000), modification.getModification().getCrop());
        assertEquals(new Dimensions(300, 600), modification.getModification().getVariant().getDensity());
        assertTrue(modification.getModification().getVariant().isStretch());
        assertTrue(modification.getModification().getVariant().isKeepAspect());
        assertEquals(DateUtils.parseDate("2013-07-31 13:40:33"), modification.getDateCreated());
        assertEquals(DateUtils.parseDate("2013-08-01 13:25:31"), modification.getDateUpdated());
    }

    @Test
    public void createReadUpdateDelete() {
        StoredImageModification inserted = createModification();

        storedImageModificationDao.insertModification(inserted);

        StoredImageModification fetched =
                storedImageModificationDao.getModification(inserted.getImageId(), inserted.getModification().getVariant());

        compareModifications(inserted, fetched);
        assertNotNull(fetched.getDateCreated());
        assertNull(fetched.getDateUpdated());

        StoredImageModification modified = modify(fetched);
        storedImageModificationDao.updateModification(modified);

        fetched = storedImageModificationDao.getModification(inserted.getImageId(), inserted.getModification().getVariant());
        compareModifications(modified, fetched);
        assertNotNull(fetched.getDateUpdated());

        storedImageModificationDao.deleteModification(inserted.getImageId(), inserted.getModification().getVariant());
    }

    private StoredImageModification createModification() {
        StoredImageModification mod = new StoredImageModification();
        mod.setImageId(9999002);

        ImageModification modifier = new ImageModification();
        modifier.getVariant().setWidth(500);
        modifier.getVariant().setHeight(400);
        modifier.setCrop(new Crop(5, 15, 100, 110, 200, 300));
        modifier.getVariant().setDensity(250, 500);
        modifier.getVariant().setKeepAspect(false);
        modifier.getVariant().setStretch(true);

        mod.setModification(modifier);

        return mod;
    }

    private StoredImageModification modify(StoredImageModification modification) {
        modification.getModification().getCrop().setX(10);
        modification.getModification().getCrop().setY(10);
        modification.getModification().getCrop().setWidth(100);
        modification.getModification().getCrop().setHeight(100);
        return modification;
    }

    private void compareModifications(StoredImageModification left, StoredImageModification right) {
        assertEquals(left.getImageId(), right.getImageId());
        assertEquals(left.getModification(), right.getModification());
    }
}
