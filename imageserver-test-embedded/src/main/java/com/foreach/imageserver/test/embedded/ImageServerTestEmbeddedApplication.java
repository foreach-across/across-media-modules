package com.foreach.imageserver.test.embedded;

import com.foreach.across.config.AcrossApplication;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.applicationinfo.ApplicationInfoModule;
import com.foreach.across.modules.debugweb.DebugWebModule;
import com.foreach.across.modules.entity.EntityModule;
import com.foreach.across.modules.filemanager.FileManagerModule;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.properties.PropertiesModule;
import com.foreach.across.modules.user.UserModule;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.common.spring.context.ApplicationInfo;
import com.foreach.imageserver.admin.ImageServerAdminWebModule;
import com.foreach.imageserver.core.ImageServerCoreModule;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;

import java.util.Collections;

@AcrossApplication(
		modules = {
				AcrossWebModule.NAME,
				ApplicationInfoModule.NAME,
				AdminWebModule.NAME,
				DebugWebModule.NAME,
				AcrossHibernateJpaModule.NAME,
				EntityModule.NAME,
				PropertiesModule.NAME,
				FileManagerModule.NAME,
				UserModule.NAME
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
	public ImageServerAdminWebModule imageServerAdminWebModule(){
		return new ImageServerAdminWebModule();
	}
}
