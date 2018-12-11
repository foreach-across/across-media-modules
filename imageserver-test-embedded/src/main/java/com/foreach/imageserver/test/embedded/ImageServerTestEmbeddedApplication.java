package com.foreach.imageserver.test.embedded;

import com.foreach.across.config.AcrossApplication;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.filemanager.FileManagerModule;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.properties.PropertiesModule;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.imageserver.admin.ImageServerAdminWebModule;
import com.foreach.imageserver.core.ImageServerCoreModule;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;

import java.util.Collections;

@AcrossApplication(
		modules = {
				AcrossWebModule.NAME,
				AdminWebModule.NAME,
				AcrossHibernateJpaModule.NAME,
				PropertiesModule.NAME,
				FileManagerModule.NAME
		}
)
public class ImageServerTestEmbeddedApplication
{
	public static void main( String[] args ) {
		SpringApplication springApplication = new SpringApplication( ImageServerTestEmbeddedApplication.class );
		springApplication.setDefaultProperties( Collections.singletonMap( "spring.config.location", "${user.home}/dev-configs/imageserver-embedded.yml" ) );
		springApplication.run( args );
	}

	@Bean
	public ImageServerCoreModule imageServerCoreModule() {
		return new ImageServerCoreModule();
	}

	@Bean
	public ImageServerAdminWebModule imageServerAdminWebModule() {
		return new ImageServerAdminWebModule();
	}
}
