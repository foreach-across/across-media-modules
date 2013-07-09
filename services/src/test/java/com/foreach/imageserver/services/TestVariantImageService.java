package com.foreach.imageserver.services;

import com.foreach.imageserver.business.image.VariantImage;
import com.foreach.imageserver.dao.VariantImageDao;
import com.foreach.imageserver.dao.selectors.VariantImageSelector;
import com.foreach.shared.utils.InjectUtils;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;

@Ignore
public class TestVariantImageService extends AbstractServiceTest {
    private VariantImageService variantImageService;
    private VariantImageDao variantImageDao;

    private VariantImage testVariantImage;
    private List<VariantImage> testVariantImages;

    private long testId = 1;
    private int formatId = 2;
    private long cropId = 3;
    private int imageId = 4;

    @Before
    public void prepareForTest() {
        variantImageService = new VariantImageServiceImpl();

        variantImageDao = Mockito.mock(VariantImageDao.class);

        InjectUtils.inject(variantImageService, "variantImageDao", variantImageDao);

        testVariantImage = new VariantImage();
        testVariantImage.setId(testId);
        testVariantImage.setImageId(imageId);
        testVariantImage.setFormatId(formatId);
        testVariantImage.setCropId(cropId);

        Mockito.when(variantImageDao.getVariantImageById(testId)).thenReturn(testVariantImage);
        Mockito.when(variantImageDao.getVariantImage(any(VariantImageSelector.class))).thenReturn(testVariantImage);

        testVariantImages = new ArrayList<VariantImage>();
        testVariantImages.add(testVariantImage);

        Mockito.when(variantImageDao.getAllVariantsForImage(imageId)).thenReturn(testVariantImages);
        Mockito.when(variantImageDao.getVariantImages(any(VariantImageSelector.class))).thenReturn(testVariantImages);
    }

    @Test
    public void testGetVariantImageById() {
        VariantImage variantImage = variantImageService.getVariantImageById(testId);

        Mockito.verify(variantImageDao, Mockito.times(1)).getVariantImageById(testId);

        Assert.assertNotNull(variantImage);
        Assert.assertEquals(testId, variantImage.getId());
        Assert.assertEquals(imageId, variantImage.getImageId());
        Assert.assertEquals(formatId, variantImage.getFormatId());
        Assert.assertEquals((long) cropId, (long) variantImage.getCropId());
    }

    @Test
    public void testGetVariantImagesByImageId() {
        List<VariantImage> variantImages = variantImageService.getAllVariantsForImage(imageId);

        Mockito.verify(variantImageDao, Mockito.times(1)).getAllVariantsForImage(imageId);

        Assert.assertNotNull(variantImages);
        Assert.assertTrue(variantImages.size() > 0);
        Assert.assertEquals(testVariantImages.size(), variantImages.size());
    }

    @Test
    public void testDeleteVariantImage() {
        variantImageService.deleteVariantImage(testVariantImage);

        Mockito.verify(variantImageDao, Mockito.times(1)).deleteVariantImage(testVariantImage);
    }

    @Test
    public void testGetVariantImageBySelector() {
        VariantImageSelector selector = VariantImageSelector.onNothing();
        VariantImage variantImage = variantImageService.getVariantImage(selector);

        Mockito.verify(variantImageDao, Mockito.times(1)).getVariantImage(any(VariantImageSelector.class));

        Assert.assertNotNull(variantImage);
        Assert.assertEquals(testId, variantImage.getId());
        Assert.assertEquals(imageId, variantImage.getImageId());
        Assert.assertEquals(formatId, variantImage.getFormatId());
        Assert.assertEquals((long) cropId, (long) variantImage.getCropId());
    }

    @Test
    public void testGetVariantImagesBySelector() {
        VariantImageSelector selector = VariantImageSelector.onNothing();
        List<VariantImage> variantImages = variantImageService.getVariantImages(selector);

        Mockito.verify(variantImageDao, Mockito.times(1)).getVariantImages(any(VariantImageSelector.class));

        Assert.assertNotNull(variantImages);
        Assert.assertTrue(variantImages.size() > 0);
        Assert.assertEquals(testVariantImages.size(), variantImages.size());
    }

    @Test
    public void testSaveNewVariantImage() {
        VariantImage newVariantImage = new VariantImage();

        newVariantImage.setImageId(10);
        newVariantImage.setFormatId(56);
        newVariantImage.setCropId((long) 5);

        variantImageService.saveVariantImage(newVariantImage);

        Mockito.verify(variantImageDao, Mockito.times(1)).insertVariantImage(newVariantImage);
        Mockito.verify(variantImageDao, Mockito.never()).updateVariantImage(Matchers.<VariantImage>any());
    }

    @Test
    public void testSaveExistingVariantImage() {
        VariantImage existingVariantImage = new VariantImage();

        existingVariantImage.setId(3546);
        existingVariantImage.setFormatId(56);
        existingVariantImage.setCropId((long) 5);

        variantImageService.saveVariantImage(existingVariantImage);

        Mockito.verify(variantImageDao, Mockito.times(1)).updateVariantImage(existingVariantImage);
        Mockito.verify(variantImageDao, Mockito.never()).insertVariantImage(Matchers.<VariantImage>any());
    }
}

