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
import com.foreach.across.modules.ehcache.EhcacheModuleSettings;
import com.foreach.across.modules.entity.EntityModule;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.user.UserModule;
import com.foreach.across.modules.user.UserModuleSettings;
import com.foreach.across.modules.webcms.WebCmsModule;
import com.foreach.across.modules.webcms.domain.domain.config.WebCmsMultiDomainConfiguration;
import com.foreach.across.modules.webcms.domain.image.WebCmsImage;
import com.foreach.imageserver.admin.ImageServerAdminWebModule;
import com.foreach.imageserver.core.ImageServerCoreModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.h2.H2ConsoleAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

import java.util.Collections;

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
public class WebCmsTestApplication
{
	public static void main( String[] args ) {
		SpringApplication springApplication = new SpringApplication( WebCmsTestApplication.class );
		springApplication.setDefaultProperties( Collections.singletonMap( "spring.config.location", "${user.home}/dev-configs/wcm-test-application.yml" ) );
		springApplication.run( args );
	}

	@Bean
	public UserModule userModule() {
		UserModule userModule = new UserModule();
		userModule.setProperty( UserModuleSettings.PASSWORD_ENCODER, NoOpPasswordEncoder.getInstance() );
		return userModule;
	}

	@Bean
	public EhcacheModule ehcacheModule() {
		EhcacheModule ehcacheModule = new EhcacheModule();
		ehcacheModule.setProperty( EhcacheModuleSettings.CACHE_MANAGER_NAME, "testWebCmsCacheManager" );
		ehcacheModule.setProperty( EhcacheModuleSettings.CONFIGURATION_RESOURCE,
		                           new ClassPathResource( "ehcache.xml" ) );
		return ehcacheModule;
	}

	@Bean
	public AcrossHibernateJpaModule acrossHibernateJpaModule() {
		AcrossHibernateJpaModule acrossHibernateJpaModule = new AcrossHibernateJpaModule();
		acrossHibernateJpaModule.setHibernateProperty( "hibernate.cache.use_second_level_cache", "true" );
		acrossHibernateJpaModule.setHibernateProperty( "hibernate.cache.region.factory_class",
		                                               "org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory" );
		return acrossHibernateJpaModule;
	}

	@Bean
	@Profile("local-imageserver")
	public ImageServerCoreModule imageServerCoreModule() {
		return new ImageServerCoreModule();
	}

	@Bean
	@Profile("local-imageserver")
	public ImageServerAdminWebModule imageServerAdminModule() {
		return new ImageServerAdminWebModule();
	}

	@Bean("multiDomainConfiguration")
	@Profile("domain-per-entity")
	public WebCmsMultiDomainConfiguration multiDomainPerEntity() {
		return WebCmsMultiDomainConfiguration.managementPerEntity()
		                                     .domainIgnoredTypes( WebCmsImage.class )
		                                     .build();
	}

	@Bean("multiDomainConfiguration")
	@Profile("domain-per-domain")
	public WebCmsMultiDomainConfiguration multiDomainPerDomain() {
		return WebCmsMultiDomainConfiguration.managementPerDomain()
		                                     .domainIgnoredTypes( WebCmsImage.class )
		                                     .build();
	}
}
