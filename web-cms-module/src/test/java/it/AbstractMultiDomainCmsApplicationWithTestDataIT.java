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

package it;

import com.foreach.across.modules.webcms.domain.article.WebCmsArticle;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.domain.config.WebCmsMultiDomainConfiguration;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenu;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.publication.WebCmsPublication;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifier;
import com.foreach.across.test.AcrossTestConfiguration;
import com.foreach.across.test.AcrossWebAppConfiguration;
import modules.multidomaintest.CmsMultiDomainTestModule;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Base class for all multi-domain integration tests using the same application context with a same backing db.
 *
 * @author Steven Gentens
 * @since 0.0.3
 */
@ExtendWith(SpringExtension.class)
@AcrossWebAppConfiguration(classes = AbstractMultiDomainCmsApplicationWithTestDataIT.Config.class)
public abstract class AbstractMultiDomainCmsApplicationWithTestDataIT extends AbstractMockMvcTest
{
	@AcrossTestConfiguration
	protected static class Config extends DynamicDataSourceConfigurer
	{
		@Bean
		public WebCmsMultiDomainConfiguration multiDomainConfiguration() {
			return WebCmsMultiDomainConfiguration.managementPerEntity()
			                                     .domainBoundTypes(
					                                     WebCmsPage.class, WebCmsMenu.class, WebCmsPublication.class,
					                                     WebCmsComponent.class, WebCmsArticle.class, WebCmsTypeSpecifier.class
			                                     )
			                                     .noDomainAllowedTypes( WebCmsTypeSpecifier.class )
			                                     .build();
		}

		@Bean
		CmsMultiDomainTestModule cmsMultiDomainTestModule() {
			return new CmsMultiDomainTestModule();
		}
	}
}
