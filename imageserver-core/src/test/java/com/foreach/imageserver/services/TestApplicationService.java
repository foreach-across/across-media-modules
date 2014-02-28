package com.foreach.imageserver.services;

import com.foreach.imageserver.core.business.Application;
import com.foreach.imageserver.data.ApplicationDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = TestApplicationService.TestConfig.class)
public class TestApplicationService
{
	@Autowired
	private ApplicationService applicationService;

	@Autowired
	private ApplicationDao applicationDao;

	@Test
	public void testGetApplicationById() {
		Application testApplication = new Application();
		testApplication.setId( 5 );
		testApplication.setName( "test" );

		when( applicationDao.getApplicationById( 5 ) ).thenReturn( testApplication );

		Application application = applicationService.getApplicationById( 5 );

		assertSame( testApplication, application );
	}

	@Test
	public void testGetAllApplications() {
		List<Application> expectedApplications = Collections.emptyList();

		when( applicationDao.getAllApplications() ).thenReturn( expectedApplications );
		Collection<Application> applications = applicationService.getAllApplications();

		assertSame( expectedApplications, applications );
	}

	@Configuration
	public static class TestConfig
	{
		@Bean
		public ApplicationService applicationService() {
			return new ApplicationServiceImpl();
		}

		@Bean
		public ApplicationDao applicationDao() {
			return mock( ApplicationDao.class );
		}
	}
}
