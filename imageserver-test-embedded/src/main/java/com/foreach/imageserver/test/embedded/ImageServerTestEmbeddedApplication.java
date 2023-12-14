package com.foreach.imageserver.test.embedded;

import com.foreach.across.config.AcrossApplication;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.debugweb.DebugWebModule;
import com.foreach.across.modules.filemanager.FileManagerModule;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.properties.PropertiesModule;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.imageserver.admin.ImageServerAdminWebModule;
import com.foreach.imageserver.core.ImageServerCoreModule;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.util.Collections;

@AcrossApplication(
		modules = {
				AcrossWebModule.NAME,
				AdminWebModule.NAME,
				AcrossHibernateJpaModule.NAME,
				PropertiesModule.NAME,
				FileManagerModule.NAME,
				DebugWebModule.NAME
//				, LoggingModule.NAME
		}
)
public class ImageServerTestEmbeddedApplication
{
	public static void main( String[] args ) {
		SpringApplication springApplication = new SpringApplication( ImageServerTestEmbeddedApplication.class );
		springApplication.setDefaultProperties(
				Collections.singletonMap( "spring.config.additional-location", "optional:${user.home}/dev-configs/imageserver-embedded.yml" ) );
		springApplication.run( args );
	}

	@Bean
	public ImageServerCoreModule imageServerCoreModule() {
		return new ImageServerCoreModule();
	}

	@Bean
	public GenericContainer imageServerTestContainer() {
		GenericContainer container = new GenericContainer<>(
				new ImageFromDockerfile()
						.withDockerfileFromBuilder( builder ->
								                            builder
										                            // This doesn't need maven, but we're already running in a container based on
										                            // maven:3.9-eclipse-temurin-8, so at least we don't have to download another image (in CI)
										                            .from( "maven:3.9-eclipse-temurin-8" )
										                            .run( "apt-get update && apt-get install -y ghostscript graphicsmagick" )
										                            .cmd( "tail -f /dev/null" )
										                            .build() ) )
				.withCreateContainerCmdModifier( createContainerCmd -> {
					createContainerCmd.withHostName( "imageserver-container" );
					createContainerCmd.withName( "imageserver-container" );
				} );
		container.start();
		return container;
	}

	@Bean
	public ImageServerAdminWebModule imageServerAdminWebModule() {
		return new ImageServerAdminWebModule();
	}
}
