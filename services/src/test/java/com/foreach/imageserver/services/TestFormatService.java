package com.foreach.imageserver.services;

import com.foreach.imageserver.business.geometry.Size;
import com.foreach.imageserver.business.image.Dimensions;
import com.foreach.imageserver.business.image.Format;
import com.foreach.imageserver.dao.FormatDao;
import com.foreach.imageserver.dao.VariantImageDao;
import com.foreach.imageserver.dao.selectors.VariantImageSelector;
import com.foreach.shared.utils.InjectUtils;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;

@Ignore
public class TestFormatService extends AbstractServiceTest
{
    private FormatService formatService;
    private FormatDao formatDao;
    private VariantImageDao variantImageDao;

    private Format testFormat;
    private List<Format> testFormats;

    private int testId = 541;
    private int groupId = 32;
    private String testName = "test";

    @Before
    public void prepareForTest()
    {
        formatService = new FormatServiceImpl();

        formatDao = Mockito.mock(FormatDao.class);
        variantImageDao = Mockito.mock(VariantImageDao.class);

        InjectUtils.inject(formatService, "formatDao", formatDao);
        InjectUtils.inject(formatService, "variantImageDao", variantImageDao);

        testFormat = new Format();
        testFormat.setId(testId);
        testFormat.setName(testName);
        testFormat.setDimensions(new Dimensions(400,600));
        testFormat.setGroupId(groupId);
        
        Mockito.when(formatDao.getFormatById(testId)).thenReturn( testFormat );
        
        testFormats = new ArrayList<Format>();
        testFormats.add(testFormat);

        Mockito.when(formatDao.getFormatsByGroupId(groupId)).thenReturn(testFormats);
    }

    @Test
    public void testGetFormatById()
    {
        Format format = formatService.getFormatById( testId );

        Mockito.verify(formatDao, Mockito.times(1)).getFormatById(testId);

        Assert.assertNotNull(format);
        Assert.assertEquals(testId, format.getId());
        Assert.assertEquals(testName, format.getName()) ;
    }

    @Test
    public void testDeleteFormat()
    {
        formatService.deleteFormat( testFormat.getId() );

        Mockito.verify(variantImageDao, Mockito.times(1)).deleteVariantImages(any(VariantImageSelector.class));
        Mockito.verify(formatDao, Mockito.times(1)).deleteFormat(testId);
    }

    @Test
    public void testGetFormatsByGroupId()
    {
        List<Format> formats = formatService.getFormatsByGroupId(groupId);

        Mockito.verify(formatDao, Mockito.times(1)).getFormatsByGroupId(groupId);

        Assert.assertNotNull(formats);
        Assert.assertTrue(formats.size() > 0);
    }

    @Test
    public void TestGetFormatIdForDimension() {
        Size size = new Size(400,600);

        int fetched = formatService.getFormatIdForDimension(size, groupId);

        Assert.assertEquals(fetched, testId);
    }
}
