package com.foreach.imageserver.test.standalone.config;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.adminweb.AdminWebModuleSettings;
import com.foreach.across.modules.debugweb.DebugWebModule;
import com.foreach.across.modules.hibernate.AcrossHibernateModule;
import com.foreach.across.modules.properties.PropertiesModule;
import com.foreach.across.modules.spring.security.SpringSecurityModule;
import com.foreach.across.modules.user.UserModule;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.AcrossWebViewSupport;
import com.foreach.imageserver.admin.ImageServerAdminWebModule;
import com.foreach.imageserver.admin.ImageServerAdminWebModuleSettings;
import com.foreach.imageserver.core.ImageServerCoreModule;
import com.foreach.imageserver.core.ImageServerCoreModuleSettings;
import com.foreach.imageserver.test.standalone.module.StandaloneWebModule;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ServletContext;
import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Configuration
@EnableAcrossContext
public class StandaloneWebConfiguration implements AcrossContextConfigurer
{
	@Autowired
	private ServletContext servletContext;

	@Bean
	public DataSource acrossDataSource() {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName( "org.hsqldb.jdbc.JDBCDriver" );
		dataSource.setUrl( "jdbc:hsqldb:mem:/hsql/standaloneImageServer" );
		dataSource.setUsername( "sa" );
		dataSource.setPassword( "" );

		return dataSource;
	}

	@Override
	public void configure( AcrossContext context ) {
		context.addModule( acrossWebModule() );
		context.addModule( debugWebModule() );
		context.addModule( propertiesModule() );
		context.addModule( userModule() );
		context.addModule( springSecurityModule() );
		context.addModule( adminWebModule() );
		context.addModule( imageServerCoreModule() );
		context.addModule( imageServerAdminModule() );
		context.addModule( acrossHibernateModule() );

		context.addModule( new StandaloneWebModule() );
	}

	private AcrossHibernateModule acrossHibernateModule() {
		return new AcrossHibernateModule();
	}

	private PropertiesModule propertiesModule() {
		return new PropertiesModule();
	}

	private AdminWebModule adminWebModule() {
		AdminWebModule adminWebModule = new AdminWebModule();
		adminWebModule.setRootPath( "/secure" );
		adminWebModule.setProperty( AdminWebModuleSettings.REMEMBER_ME_KEY, "standalone" );

		return adminWebModule;
	}

	private SpringSecurityModule springSecurityModule() {
		return new SpringSecurityModule();
	}

	private UserModule userModule() {
		return new UserModule();
	}

	private AcrossWebModule acrossWebModule() {
		AcrossWebModule webModule = new AcrossWebModule();
		webModule.setViewsResourcePath( "/static" );
		webModule.setSupportViews( AcrossWebViewSupport.JSP, AcrossWebViewSupport.THYMELEAF );

		webModule.setDevelopmentMode( true );

		String deploymentPath = servletContext.getRealPath( "" );
		Path adminPath = Paths.get( deploymentPath, "../../../", "imageserver-admin/src/main/resources/views" );

		webModule.addDevelopmentViews( "imageserver-admin", adminPath.toString() );

		return webModule;
	}

	private DebugWebModule debugWebModule() {
		DebugWebModule debugWebModule = new DebugWebModule();
		debugWebModule.setRootPath( "/debug" );

		return debugWebModule;
	}

	private ImageServerCoreModule imageServerCoreModule() {
		ImageServerCoreModule coreModule = new ImageServerCoreModule();
		coreModule.setProperty( ImageServerCoreModuleSettings.IMAGE_STORE_FOLDER,
		                        new File( System.getProperty( "java.io.tmpdir" ), UUID.randomUUID().toString() ) );
		coreModule.setProperty( ImageServerCoreModuleSettings.PROVIDE_STACKTRACE, true );
		coreModule.setProperty( ImageServerCoreModuleSettings.IMAGEMAGICK_ENABLED, true );
		coreModule.setProperty( ImageServerCoreModuleSettings.IMAGEMAGICK_USE_GRAPHICSMAGICK, true );
		coreModule.setProperty( ImageServerCoreModuleSettings.IMAGEMAGICK_PATH,
		                        "c:/Program Files/GraphicsMagick-1.3.19-Q8" );

		coreModule.setProperty( ImageServerCoreModuleSettings.ROOT_PATH, "/resources/images" );
		coreModule.setProperty( ImageServerCoreModuleSettings.ACCESS_TOKEN, "standalone-access-token" );

		return coreModule;
	}

	private ImageServerAdminWebModule imageServerAdminModule() {
		ImageServerAdminWebModule imageServerAdminWebModule = new ImageServerAdminWebModule();
		imageServerAdminWebModule.setProperty( ImageServerAdminWebModuleSettings.IMAGE_SERVER_URL,
		                                       "/resources/images" );
		imageServerAdminWebModule.setProperty( ImageServerAdminWebModuleSettings.ACCESS_TOKEN,
		                                       "standalone-access-token" );

		return imageServerAdminWebModule;
	}
}
