package com.foreach.imageserver.test.standalone.config;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.config.EnableAcrossContext;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.debugweb.DebugWebModule;
import com.foreach.across.modules.filemanager.FileManagerModule;
import com.foreach.across.modules.properties.PropertiesModule;
import com.foreach.across.modules.user.UserModule;
import com.foreach.imageserver.admin.ImageServerAdminWebModule;
import com.foreach.imageserver.admin.ImageServerAdminWebModuleSettings;
import com.foreach.imageserver.core.ImageServerCoreModule;
import com.foreach.imageserver.core.ImageServerCoreModuleSettings;
import com.foreach.imageserver.test.standalone.module.StandaloneWebModule;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import javax.sql.DataSource;
import java.io.File;
import java.util.UUID;

@Configuration
@EnableAcrossContext(modules = { DebugWebModule.NAME, UserModule.NAME, FileManagerModule.NAME, PropertiesModule.NAME })
@PropertySource("classpath:application.properties")
public class StandaloneWebConfiguration implements AcrossContextConfigurer
{
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
		context.addModule( imageServerCoreModule() );
		context.addModule( imageServerAdminModule() );
		context.addModule( new StandaloneWebModule() );
	}

	private ImageServerCoreModule imageServerCoreModule() {
		ImageServerCoreModule coreModule = new ImageServerCoreModule();
		coreModule.setProperty( ImageServerCoreModuleSettings.IMAGE_STORE_FOLDER,
		                        new File( System.getProperty( "java.io.tmpdir" ), UUID.randomUUID().toString() ) );
		coreModule.setProperty( ImageServerCoreModuleSettings.PROVIDE_STACKTRACE, true );
		coreModule.setProperty( ImageServerCoreModuleSettings.IMAGEMAGICK_ENABLED, true );
		coreModule.setProperty( ImageServerCoreModuleSettings.IMAGEMAGICK_USE_GRAPHICSMAGICK, true );
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

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
}
