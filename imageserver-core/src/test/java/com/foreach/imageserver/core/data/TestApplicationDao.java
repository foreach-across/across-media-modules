package com.foreach.imageserver.core.data;

import com.foreach.imageserver.core.business.Application;
import com.foreach.imageserver.core.DateUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.*;

public class TestApplicationDao extends AbstractDaoTest
{
	@Autowired
	private ApplicationDao applicationDao;

	@Test
	public void getKnownApplication() {
		Application application = applicationDao.getApplicationById( 9999 );

		assertNotNull( application );
		assertEquals( 9999, application.getId() );
		assertEquals( "Unit Test Application", application.getName() );
		assertEquals( "30EB4EF0-E2E2-11E2-A28F-0800200C9A66", application.getCode() );
		assertTrue( application.isActive() );
		assertEquals( DateUtils.parseDate( "2013-07-02 08:40:33" ), application.getDateCreated() );
		assertEquals( DateUtils.parseDate( "2013-07-03 11:13:12" ), application.getDateUpdated() );
	}

	@Test
	public void getAllApplications() {
		List<Application> applications = applicationDao.getAllApplications();

		assertTrue( applications.size() > 0 );
		Application existing = new Application();
		existing.setId( 9999 );
		assertTrue( applications.contains( existing ) );
	}

	@Ignore
	@Test
	public void applicationCrud() {
		Application app = createApplication();
		applicationDao.insertApplication( app );
		assertTrue( app.getId() > 0 );

		Application fetched = applicationDao.getApplicationById( app.getId() );
		compareApplications( app, fetched );

		app = modifyApplication( fetched );
		applicationDao.updateApplication( app );
		fetched = applicationDao.getApplicationById( app.getId() );
		compareApplications( app, fetched );

		applicationDao.deleteApplication( app.getId() );
	}

	private Application createApplication() {
		Application app = new Application();
		app.setCallbackUrl( "zeCallback" );
		app.setName( "zeName " );
		return app;
	}

	private void compareApplications( Application left, Application right ) {
		assertEquals( left.getId(), right.getId() );
		assertEquals( left.getName(), right.getName() );
		assertEquals( left.getCallbackUrl(), right.getCallbackUrl() );
	}

	private Application modifyApplication( Application app ) {
		app.setName( "zanotherName" );
		app.setCallbackUrl( "zanotherCallback" );
		return app;
	}
}
