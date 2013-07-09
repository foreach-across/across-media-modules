package com.foreach.imageserver.services;

import com.foreach.imageserver.business.image.Crop;
import com.foreach.imageserver.dao.CropDao;
import com.foreach.imageserver.services.crop.CropService;
import com.foreach.imageserver.services.crop.CropServiceImpl;
import com.foreach.shared.utils.InjectUtils;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

@Ignore
public class TestCropService extends AbstractServiceTest
{
    private CropService cropService;
    private CropDao cropDao;

    private Crop testCrop;

    private long cropId = 1;

    @Before
    public void prepareForTest()
    {
        cropService = new CropServiceImpl();

        cropDao = Mockito.mock(CropDao.class);

        InjectUtils.inject(cropService, "cropDao", cropDao);

        testCrop = new Crop();
        testCrop.setId(cropId);
//        testCrop.setName("test");

        Mockito.when(cropDao.getCropById(cropId)).thenReturn( testCrop );
    }

    @Test
    public void testGetCropById()
    {
        Crop crop = cropService.getCropById(cropId);

        Mockito.verify(cropDao, Mockito.times(1)).getCropById(cropId);

        Assert.assertNotNull(crop);
        Assert.assertEquals(cropId, crop.getId());
    }

    @Test
    public void testSaveNewCrop()
    {
        Crop newCrop = new Crop();

        newCrop.setImageId( 1L );

        cropService.saveCrop( newCrop );

        Mockito.verify(cropDao, Mockito.times(1)).insertCrop( newCrop );
        Mockito.verify(cropDao, Mockito.never()).updateCrop( Matchers.<Crop>any() );
    }

    @Test
    public void testSaveExistingCrop()
    {
        Crop existingCrop = new Crop();

        existingCrop.setId( 3546L );
        existingCrop.setImageId( 1L );

        cropService.saveCrop( existingCrop );

        Mockito.verify(cropDao, Mockito.times(1)).updateCrop( existingCrop );
        Mockito.verify(cropDao, Mockito.never()).insertCrop( Matchers.<Crop>any() );
    }
}
