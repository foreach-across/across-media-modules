/*
 * Copyright 2017 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package webapps.admin;

import com.foreach.across.config.AcrossApplication;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.debugweb.DebugWebModule;
import com.foreach.across.modules.ehcache.EhcacheModule;
import com.foreach.across.modules.entity.EntityModule;
import com.foreach.across.modules.user.UserModule;
import com.foreach.across.modules.user.UserModuleSettings;
import com.foreach.across.modules.webcms.WebCmsModule;
import com.foreach.imageserver.admin.ImageServerAdminWebModule;
import com.foreach.imageserver.admin.ImageServerAdminWebModuleSettings;
import com.foreach.imageserver.core.ImageServerCoreModule;
import com.foreach.imageserver.core.ImageServerCoreModuleSettings;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.h2.H2ConsoleAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

import java.io.File;
import java.util.UUID;

/**
 * Main application for a website with the administration UI active, allowing dynamic addition of pages.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@AcrossApplication(
		modules = {
				WebCmsModule.NAME,
				EntityModule.NAME,
				AdminWebModule.NAME,
				DebugWebModule.NAME,
				EhcacheModule.NAME
		}
)
@Import({ DataSourceAutoConfiguration.class, H2ConsoleAutoConfiguration.class })
public class AdminWebCmsApplication
{
	@Bean
	public UserModule userModule() {
		UserModule userModule = new UserModule();
		userModule.setProperty( UserModuleSettings.PASSWORD_ENCODER, NoOpPasswordEncoder.getInstance() );
		return userModule;
	}

	@Bean
	public ImageServerCoreModule imageServerCoreModule() {
		ImageServerCoreModule coreModule = new ImageServerCoreModule();
		coreModule.setProperty( ImageServerCoreModuleSettings.IMAGE_STORE_FOLDER,
		                        new File( "../db/images" ) );
		                        //new File( System.getProperty( "java.io.tmpdir" ), UUID.randomUUID().toString() ) );
		coreModule.setProperty( ImageServerCoreModuleSettings.PROVIDE_STACKTRACE, true );
		coreModule.setProperty( ImageServerCoreModuleSettings.IMAGEMAGICK_ENABLED, true );
		coreModule.setProperty( ImageServerCoreModuleSettings.IMAGEMAGICK_USE_GRAPHICSMAGICK, true );
		//coreModule.setProperty( ImageServerCoreModuleSettings.IMAGEMAGICK_PATH, imageMagickPath );

		coreModule.setProperty( ImageServerCoreModuleSettings.ROOT_PATH, "/resources/images" );
		coreModule.setProperty( ImageServerCoreModuleSettings.ACCESS_TOKEN, "standalone-access-token" );
		coreModule.setProperty( ImageServerCoreModuleSettings.CREATE_LOCAL_CLIENT, true );
		coreModule.setProperty( ImageServerCoreModuleSettings.IMAGE_SERVER_URL, "http://localhost:8080/resources/images" );
		coreModule.setProperty( ImageServerCoreModuleSettings.MD5_HASH_TOKEN, "imageserver" );

		return coreModule;
	}

	@Bean
	public ImageServerAdminWebModule imageServerAdminModule() {
		ImageServerAdminWebModule imageServerAdminWebModule = new ImageServerAdminWebModule();
		imageServerAdminWebModule.setProperty( ImageServerAdminWebModuleSettings.IMAGE_SERVER_URL,
		                                       "/resources/images" );
		imageServerAdminWebModule.setProperty( ImageServerAdminWebModuleSettings.ACCESS_TOKEN,
		                                       "standalone-access-token" );

		return imageServerAdminWebModule;
	}

	public static void main( String[] args ) {
		SpringApplication.run( AdminWebCmsApplication.class, args );
	}
}
