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

package test.domain;

import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.entity.EntityAttributes;
import com.foreach.across.modules.entity.EntityModule;
import com.foreach.across.modules.entity.registry.EntityConfiguration;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyRegistry;
import com.foreach.across.modules.webcms.WebCmsModule;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticle;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticleType;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentType;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.config.WebCmsMultiDomainConfiguration;
import com.foreach.across.modules.webcms.domain.image.WebCmsImage;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.WebCmsPageType;
import com.foreach.across.test.AcrossTestConfiguration;
import com.foreach.across.test.AcrossWebAppConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 * @since 0.0.3
 */
@RunWith(SpringJUnit4ClassRunner.class)
@AcrossWebAppConfiguration
public class TestMultiDomainManagementPerEntityConfiguration extends AbstractMultiDomainTest
{
	@Test
	public void domainPropertyShouldBeDisabledForAllDomainIgnoredTypes() {
		Stream.of( WebCmsPageType.class, WebCmsComponentType.class, WebCmsImage.class )
		      .forEach( this::assertDomainPropertyDisabled );
	}

	@Test
	public void domainPropertyShouldBeAvailableForAllDomainBoundTypes() {
		Stream.of( WebCmsArticle.class, WebCmsPage.class, WebCmsArticleType.class )
		      .forEach( this::assertDomainPropertyAvailable );
	}

	@Test
	public void defaultDomainOptionQueryIsAllDomainsThatAreAccessibleForTheUser() {
		assertOptionQuery( WebCmsDomain.class, "id in (accessibleDomains())" );
	}

	@Test
	public void defaultDomainBoundObjectOptionQueryIsFilteredOnAccessibleDomains() {
		Stream.of( WebCmsArticle.class, WebCmsPage.class, WebCmsArticleType.class )
		      .forEach( type -> assertOptionQuery( type, "domain in (accessibleDomains())" ) );
	}

	@Test
	public void entityPropertiesReferringDomainBoundObjectHaveTheirCustomOptionQueryAppended() {
		EntityPropertyRegistry propertyRegistry = entityRegistry.getEntityConfiguration( WebCmsArticle.class )
		                                                        .getPropertyRegistry();
		assertEquals(
				"(published = TRUE) and (domain in (accessibleDomains()))",
				propertyRegistry
						.getProperty( "publication" )
						.getAttribute( EntityAttributes.OPTIONS_ENTITY_QUERY )
		);
		assertNull( propertyRegistry.getProperty( "articleType" ).getAttribute( EntityAttributes.OPTIONS_ENTITY_QUERY ) );
	}

	@AcrossTestConfiguration(modules = { AdminWebModule.NAME, EntityModule.NAME, WebCmsModule.NAME })
	@Configuration
	protected static class Config
	{
		@Bean
		public WebCmsMultiDomainConfiguration multiDomainConfiguration() {
			return WebCmsMultiDomainConfiguration.managementPerEntity()
			                                     .domainBoundTypes( WebCmsArticleType.class )
			                                     .domainIgnoredTypes( WebCmsImage.class )
			                                     .build();
		}
	}
}
