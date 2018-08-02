package com.foreach.imageserver.core;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.filters.PackageBeanFilter;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.test.AcrossTestConfiguration;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { AbstractIntegrationTest.Config.class })
@EnableTransactionManagement
@WebAppConfiguration
public abstract class AbstractIntegrationTest
{
	@AcrossTestConfiguration
	@Configuration
	public static class Config implements AcrossContextConfigurer
	{
		@Bean
		public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
			PropertySourcesPlaceholderConfigurer propertySources = new PropertySourcesPlaceholderConfigurer();
			propertySources.setIgnoreResourceNotFound( false );
			propertySources.setIgnoreUnresolvablePlaceholders( false );

			return propertySources;
		}

		@Bean
		public DataSourceTransactionManager transactionManager( DataSource dataSource ) {
			return new DataSourceTransactionManager( dataSource );
		}

		@Override
		public void configure( AcrossContext context ) {
			context.addModule( imageServerCoreModule() );
			context.addModule( acrossHibernateJpaModule() );
		}

		public AcrossModule imageServerCoreModule() {
			ImageServerCoreModule module = new ImageServerCoreModule();
			module.setProperty( ImageServerCoreModuleSettings.IMAGE_STORE_FOLDER,
			                    System.getProperty( "java.io.tmpdir" ) );
			module.setExposeFilter( new PackageBeanFilter( "com.foreach.imageserver.core" ) );
			return module;
		}

		public AcrossModule acrossHibernateJpaModule() {
			return new AcrossHibernateJpaModule();
		}

	}
}
