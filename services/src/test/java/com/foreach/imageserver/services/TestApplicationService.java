package com.foreach.imageserver.services;

import com.foreach.imageserver.business.taxonomy.Application;
import com.foreach.imageserver.dao.ApplicationDao;
import com.foreach.shared.utils.InjectUtils;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

public class TestApplicationService extends AbstractServiceTest
{
    private ApplicationService applicationService;
    private ApplicationDao applicationDao;

    private Application testApplication;
    private List<Application> testApplications;

    private int testId = 1;
    private String testName = "test";

    @Before
    public void prepareForTest()
    {
        applicationService = new ApplicationServiceImpl();

        applicationDao = Mockito.mock(ApplicationDao.class);

        InjectUtils.inject(applicationService, "applicationDao", applicationDao);

        testApplication = new Application();
        testApplication.setId(testId);
        testApplication.setName(testName);
        
        Mockito.when(applicationDao.getApplicationById(testId)).thenReturn( testApplication );
        
        testApplications = new ArrayList<Application>();
        testApplications.add(testApplication);

        Mockito.when(applicationDao.getAllApplications()).thenReturn( testApplications );
    }

    @Test
    public void testGetApplicationById()
    {
        Application application = applicationService.getApplicationById( testId );

        Mockito.verify(applicationDao, Mockito.times(1)).getApplicationById(testId);
        
        Assert.assertNotNull(application);
        Assert.assertEquals(testId, application.getId());
        Assert.assertEquals(testName, application.getName()) ;
    }

    @Test
    public void testGetAllApplications()
    {
        List<Application> applications = applicationService.getAllApplications();

        Mockito.verify(applicationDao, Mockito.times(1)).getAllApplications();

        Assert.assertNotNull(applications);
        Assert.assertTrue(applications.size() > 0);
        Assert.assertEquals(testApplications.size(), applications.size());
    }
}
