package com.foreach.across.webapp.taskrunner.config;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.debugweb.DebugWebModule;
import com.foreach.across.modules.hibernate.AcrossHibernateModule;
import com.foreach.across.modules.taskrunner.TaskRunnerModule;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.AcrossWebViewSupport;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author Arne Vandamme
 */
@Configuration
@EnableAcrossContext
public class TestWebConfiguration implements AcrossContextConfigurer
{
	@Bean
	public DataSource acrossDataSource() {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName( "org.hsqldb.jdbc.JDBCDriver" );
		dataSource.setUrl( "jdbc:hsqldb:mem:/hsql/taskRunnerTestWeb" );
		dataSource.setUsername( "sa" );
		dataSource.setPassword( "" );

		return dataSource;
	}

	@Override
	public void configure( AcrossContext context ) {
		context.addModule( acrossWebModule() );
		context.addModule( debugWebModule() );
		context.addModule( acrossHibernateModule() );
		context.addModule( taskRunnerModule() );
		/*context.addModule( propertiesModule() );
		context.addModule( userModule() );
		context.addModule( springSecurityModule() );
		context.addModule( springSecurityAclModule() );
		context.addModule( adminWebModule() );
		*/
	}

	private TaskRunnerModule taskRunnerModule() {
		return new TaskRunnerModule();
	}

	private AcrossHibernateModule acrossHibernateModule() {
		return new AcrossHibernateModule();
	}
//
//	private PropertiesModule propertiesModule() {
//		return new PropertiesModule();
//	}

//	private AdminWebModule adminWebModule() {
//		AdminWebModule adminWebModule = new AdminWebModule();
//		adminWebModule.setRootPath( "/secure" );
//		adminWebModule.setProperty( AdminWebModuleSettings.REMEMBER_ME_KEY, "content-repository-test-web" );
//
//		return adminWebModule;
//	}
//
//	private SpringSecurityModule springSecurityModule() {
//		return new SpringSecurityModule();
//	}
//
//	private SpringSecurityAclModule springSecurityAclModule() {
//		return new SpringSecurityAclModule();
//	}
//
//	private UserModule userModule() {
//		return new UserModule();
//	}

	private AcrossWebModule acrossWebModule() {
		AcrossWebModule webModule = new AcrossWebModule();
		webModule.setViewsResourcePath( "/static" );
		webModule.setSupportViews( AcrossWebViewSupport.THYMELEAF );

		return webModule;
	}

	private DebugWebModule debugWebModule() {
		DebugWebModule debugWebModule = new DebugWebModule();
		debugWebModule.setRootPath( "/debug" );

		return debugWebModule;
	}
}
