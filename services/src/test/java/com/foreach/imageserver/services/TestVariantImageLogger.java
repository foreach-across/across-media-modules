package com.foreach.imageserver.services;


import com.foreach.imageserver.business.container.CircularArrayList;
import com.foreach.imageserver.business.image.VariantImage;
import com.foreach.imageserver.dao.VariantImageDao;
import com.foreach.shared.utils.InjectUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyObject;

public class TestVariantImageLogger {

    private VariantImageLoggerImpl variantImageLogger;
    private VariantImageDao variantImageDao;

    private VariantImage testVariantImage;
    private CircularArrayList<VariantImage> mockedVariantImages;
    private List<VariantImage> poppedList;

    private long testId = 1;
    private int formatId = 2;
    private long cropId = 3;
    private int imageId = 4;

    private int logSize = 100;

    @Before
    public void prepareForTest() {
        variantImageLogger = new VariantImageLoggerImpl(logSize);

        variantImageDao = Mockito.mock(VariantImageDao.class);
        mockedVariantImages = Mockito.mock(CircularArrayList.class);

        InjectUtils.inject(variantImageLogger, "variantImageDao", variantImageDao);
        InjectUtils.inject(variantImageLogger, "variantImages", mockedVariantImages);

        testVariantImage = new VariantImage();
        testVariantImage.setId(testId);
        testVariantImage.setImageId(imageId);
        testVariantImage.setFormatId(formatId);
        testVariantImage.setCropId(cropId);

        poppedList = new ArrayList<VariantImage>();
        poppedList.add(testVariantImage);
        poppedList.add(testVariantImage);
        poppedList.add(testVariantImage);
    }

    @Test
    public void pushIsCalled() {
        variantImageLogger.logVariantImage(testVariantImage);

        Mockito.verify(mockedVariantImages, Mockito.times(1)).push(testVariantImage);
    }

    @Test
    public void flushTest() {
        Mockito.when(mockedVariantImages.popAll()).thenReturn(poppedList);

        variantImageLogger.flushLog();

        Mockito.verify(mockedVariantImages, Mockito.times(1)).popAll();
        Mockito.verify(variantImageDao, Mockito.times(3)).updateVariantImageDate((VariantImage)anyObject());
    }
}
