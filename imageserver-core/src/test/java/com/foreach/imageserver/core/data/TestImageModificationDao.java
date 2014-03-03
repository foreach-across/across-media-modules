package com.foreach.imageserver.core.data;

import com.foreach.imageserver.core.DateUtils;
import com.foreach.imageserver.core.business.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.*;

public class TestImageModificationDao extends AbstractDaoTest {
    @Autowired
    private ImageModificationDao imageModificationDao;

    @Test
    public void getKnownImageModifications() {
        Collection<ImageModification> modifications = imageModificationDao.getModificationsForImage(9999001);

        assertNotNull(modifications);
        assertEquals(2, modifications.size());

        // First modification should be smallest width
        Iterator<ImageModification> iterator = modifications.iterator();

        ImageModification modification = iterator.next();
        assertNotNull(modification);
        assertEquals(9999001, modification.getImageId());
        assertEquals(new Dimensions(800, 600), modification.getDimensions());
        assertEquals(1024, modification.getModifier().getWidth());
        assertEquals(768, modification.getModifier().getHeight());
        assertEquals(ImageType.GIF, modification.getModifier().getOutput());
        assertFalse(modification.getModifier().hasCrop());
        assertEquals(new Dimensions(), modification.getModifier().getDensity());
        assertFalse(modification.getModifier().isStretch());
        assertFalse(modification.getModifier().isKeepAspect());
        assertEquals(DateUtils.parseDate("2013-07-31 13:40:33"), modification.getDateCreated());
        assertEquals(DateUtils.parseDate("2013-08-01 13:25:31"), modification.getDateUpdated());

        modification = iterator.next();
        assertNotNull(modification);
        assertEquals(9999001, modification.getImageId());
        assertEquals(new Dimensions(1024, 768), modification.getDimensions());
        assertEquals(1600, modification.getModifier().getWidth());
        assertEquals(1200, modification.getModifier().getHeight());
        assertEquals(ImageType.PNG, modification.getModifier().getOutput());
        assertEquals(new Crop(100, 150, 500, 750, 1000, 2000), modification.getModifier().getCrop());
        assertEquals(new Dimensions(300, 600), modification.getModifier().getDensity());
        assertTrue(modification.getModifier().isStretch());
        assertTrue(modification.getModifier().isKeepAspect());
        assertEquals(DateUtils.parseDate("2013-07-31 13:40:33"), modification.getDateCreated());
        assertEquals(DateUtils.parseDate("2013-08-01 13:25:31"), modification.getDateUpdated());
    }

    @Test
    public void createReadUpdateDelete() {
        ImageModification inserted = createModification();

        imageModificationDao.insertModification(inserted);

        ImageModification fetched =
                imageModificationDao.getModification(inserted.getImageId(), inserted.getDimensions());

        compareModifications(inserted, fetched);
        assertNotNull(fetched.getDateCreated());
        assertNull(fetched.getDateUpdated());

        ImageModification modified = modify(fetched);
        imageModificationDao.updateModification(modified);

        fetched = imageModificationDao.getModification(inserted.getImageId(), inserted.getDimensions());
        compareModifications(modified, fetched);
        assertNotNull(fetched.getDateUpdated());

        imageModificationDao.deleteModification(inserted.getImageId(), inserted.getDimensions());
    }

    private ImageModification createModification() {
        ImageModification mod = new ImageModification();
        mod.setImageId(9999001);
        mod.setDimensions(new Dimensions(640, 480));

        ImageModifier modifier = new ImageModifier();
        modifier.setWidth(500);
        modifier.setHeight(400);
        modifier.setCrop(new Crop(5, 15, 100, 110, 200, 300));
        modifier.setDensity(250, 500);
        modifier.setKeepAspect(false);
        modifier.setStretch(true);

        mod.setModifier(modifier);

        return mod;
    }

    private ImageModification modify(ImageModification modification) {
        ImageModifier modifier = new ImageModifier();
        modifier.setWidth(1900);
        modifier.setHeight(1440);
        modifier.setCrop(new Crop());
        modifier.setDensity(900, 1200);
        modifier.setKeepAspect(true);
        modifier.setStretch(false);
        modifier.setOutput(ImageType.PNG);

        modification.setModifier(modifier);
        return modification;
    }

    private void compareModifications(ImageModification left, ImageModification right) {
        assertEquals(left.getImageId(), right.getImageId());
        assertEquals(left.getDimensions(), right.getDimensions());
        assertEquals(left.getModifier(), right.getModifier());
    }
}
