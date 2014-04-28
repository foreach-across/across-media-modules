package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.data.ImageResolutionDao;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

public class ContextServiceTest {

    public static final int CONTEXT_ID = 2;
    private ContextService contextService;

    @Before
    public void setUp() {
        contextService = new ContextServiceImpl();
        ImageResolutionDao imageResolutionDao = Mockito.mock(ImageResolutionDao.class);

        List<ImageResolution> imageResolutions = new ArrayList<ImageResolution>();
        imageResolutions.add(createImageResolution(1, 10, 20));
        imageResolutions.add(createImageResolution(2, 20, 20));
        imageResolutions.add(createImageResolution(3, 30, 20));
        imageResolutions.add(createImageResolution(4, 40, 20));
        imageResolutions.add(createImageResolution(5, 50, 20));

        imageResolutions.add(createImageResolution(6, 10, 50));
        imageResolutions.add(createImageResolution(7, 20, 50));
        imageResolutions.add(createImageResolution(8, 30, 50));
        imageResolutions.add(createImageResolution(9, 40, 50));
        imageResolutions.add(createImageResolution(10, 50, 50));

        imageResolutions.add(createImageResolution(11, 10, 70));
        imageResolutions.add(createImageResolution(12, 20, 70));
        imageResolutions.add(createImageResolution(13, 30, 70));
        imageResolutions.add(createImageResolution(14, 40, 70));
        imageResolutions.add(createImageResolution(15, 50, 70));

        Mockito.doReturn(imageResolutions).when(imageResolutionDao).getForContext(CONTEXT_ID);
        ReflectionTestUtils.setField(contextService, "imageResolutionDao", imageResolutionDao);
    }

    @Test
    public void getImageResolution_forWidthHeight() {
        ImageResolution imageResolution = contextService.getImageResolution(CONTEXT_ID, 28, 48); //-> 30,50
        Assert.assertNotNull(imageResolution);
        Assert.assertEquals(8, (int) imageResolution.getId());
    }

    @Test
    public void getImageResolution_ExactWidthHeight() {
        ImageResolution imageResolution = contextService.getImageResolution(CONTEXT_ID, 30, 48); //-> 30,50
        Assert.assertNotNull(imageResolution);
        Assert.assertEquals(8, (int) imageResolution.getId());
    }

    @Test
    public void getImageResolution_ExactWidthExactHeight() {
        ImageResolution imageResolution = contextService.getImageResolution(CONTEXT_ID, 30, 50); //-> 30,50
        Assert.assertNotNull(imageResolution);
        Assert.assertEquals(8, (int) imageResolution.getId());
    }

    @Test
    public void getImageResolution_TooBigWidthHeight() {
        ImageResolution imageResolution = contextService.getImageResolution(CONTEXT_ID, 70, 48); //-> null
        Assert.assertNull(imageResolution);
    }

    @Test
    public void getImageResolution_TooBigWidthExactHeight() {
        ImageResolution imageResolution = contextService.getImageResolution(CONTEXT_ID, 70, 50); //-> null
        Assert.assertNull(imageResolution);
    }

    @Test
    public void getImageResolution_WidthAndTooBigHeight() {
        ImageResolution imageResolution = contextService.getImageResolution(CONTEXT_ID, 30, 80); //-> null
        Assert.assertNull(imageResolution);
    }

    @Test
    public void getImageResolution_ExactWidthAndTooBigHeight() {
        ImageResolution imageResolution = contextService.getImageResolution(CONTEXT_ID, 30, 80); //-> null
        Assert.assertNull(imageResolution);
    }

    @Test
    public void getImageResolution_WidthNoHeight() {
        // gets resolution closest to 3/2 width/height ratio: : 50/50=2.5 is closer than 50/20 or 50/70=0.71
        ImageResolution imageResolution = contextService.getImageResolution(CONTEXT_ID, 48, 0); //-> 50,50
        Assert.assertNotNull(imageResolution);
        Assert.assertEquals(10, (int) imageResolution.getId());
    }

    @Test
    public void getImageResolution_ExactWidthNoHeight() {
        // gets resolution closest to 3/2 width/height ratio: 50/50=2.5 is closer than 50/20 or 50/70=0.71
        ImageResolution imageResolution = contextService.getImageResolution(CONTEXT_ID, 50, 0); //-> 50,50
        Assert.assertNotNull(imageResolution);
        Assert.assertEquals(10, (int) imageResolution.getId());
    }

    @Test(expected = ImageResolutionException.class)
    public void getImageResolution_NoWidthNoHeight() {
        ImageResolution imageResolution = contextService.getImageResolution(CONTEXT_ID, 0, 0);
    }

    private ImageResolution createImageResolution(int id, int width, int height) {
        ImageResolution imageResolution = new ImageResolution();
        imageResolution.setId(id);
        imageResolution.setWidth(width);
        imageResolution.setHeight(height);
        return imageResolution;
    }
}
