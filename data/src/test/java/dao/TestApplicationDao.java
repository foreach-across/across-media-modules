package dao;

import com.foreach.imageserver.business.taxonomy.Application;
import com.foreach.imageserver.dao.ApplicationDao;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

public class TestApplicationDao extends AbstractDaoTest
{
    @Autowired
    private ApplicationDao applicationDao;

    @Test
    public void applicationCrud()
    {
        Application app = createApplication();
        applicationDao.insertApplication( app );
        assertTrue( app.getId() > 0 );

        Application fetched = applicationDao.getApplicationById( app.getId() );
        compareApplications( app, fetched);

        app = modifyApplication( fetched );
        applicationDao.updateApplication( app );
        fetched = applicationDao.getApplicationById( app.getId() );
        compareApplications( app, fetched );

        applicationDao.deleteApplication( app.getId() );
    }

    private Application createApplication()
    {
        Application app = new Application();
        app.setCallbackUrl( "zeCallback" );
        app.setName( "zeName ");
        return app;
    }

    private void compareApplications( Application left, Application right )
    {
        assertEquals( left.getId(), right.getId() );
        assertEquals( left.getName(), right.getName() );
        assertEquals( left.getCallbackUrl(), right.getCallbackUrl() );
    }

    private Application modifyApplication( Application app )
    {
        app.setName( "zanotherName" );
        app.setCallbackUrl( "zanotherCallback" );
        return app;
    }

    @Test
    public void getAllApplicationsTest()
    {
        List<Application> applications = applicationDao.getAllApplications();

        assertTrue( applications.size() > 0 );
    }
}
