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

package com.foreach.across.modules.webcms.domain.domain.config;

import com.foreach.across.modules.webcms.domain.article.WebCmsArticle;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticleType;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainBound;
import com.foreach.across.modules.webcms.domain.domain.web.WebCmsSiteConfigurationImpl;
import com.foreach.across.modules.webcms.domain.image.WebCmsImage;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenuItem;
import com.foreach.across.modules.webcms.domain.page.WebCmsPageType;
import com.foreach.across.modules.webcms.domain.redirect.WebCmsRemoteEndpoint;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifier;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Arne Vandamme
 * @since 0.0.3
 */
public class TestWebCmsMultiDomainConfiguration
{
	@Test
	public void defaultValues() {
		WebCmsMultiDomainConfiguration config = WebCmsMultiDomainConfiguration.builder().build();

		assertTrue( config.isDisabled() );
		assertNull( config.getDomainContextFilterClass() );
		assertNull( config.getDefaultDomainKey() );
		assertTrue( config.isNoDomainAllowed() );
		assertFalse( config.isDomainSelectablePerEntity() );

		assertEquals( config, WebCmsMultiDomainConfiguration.disabled().build() );

		assertFalse( config.isDomainBound( WebCmsAsset.class ) );
		assertFalse( config.isDomainBound( WebCmsTypeSpecifier.class ) );

		assertTrue( config.isNoDomainAllowed( WebCmsAsset.class ) );

		assertEquals( WebCmsSiteConfigurationImpl.class, config.getMetadataClass() );
	}

	@Test
	public void defaultManagementPerDomain() {
		WebCmsMultiDomainConfiguration config = WebCmsMultiDomainConfiguration.managementPerDomain().build();

		assertFalse( config.isDisabled() );
		assertTrue( config.isNoDomainAllowed() );
		assertFalse( config.isDomainSelectablePerEntity() );
		assertNotNull( config.getDomainContextFilterClass() );

		assertFalse( config.isDomainBound( String.class ) );
		assertTrue( config.isDomainBound( WebCmsDomainBound.class ) );
		assertTrue( config.isDomainBound( WebCmsAsset.class ) );
		assertTrue( config.isDomainBound( WebCmsRemoteEndpoint.class ) );
		assertTrue( config.isDomainBound( WebCmsUrl.class ) );
		assertTrue( config.isDomainBound( WebCmsMenuItem.class ) );
		assertFalse( config.isDomainBound( WebCmsTypeSpecifier.class ) );
		assertFalse( config.isDomainBound( WebCmsArticleType.class ) );

		assertFalse( config.isNoDomainAllowed( WebCmsArticle.class ) );
		assertTrue( config.isNoDomainAllowed( WebCmsComponent.class ) );
	}

	@Test
	public void defaultManagementPerEntity() {
		WebCmsMultiDomainConfiguration config = WebCmsMultiDomainConfiguration.managementPerEntity().build();

		assertFalse( config.isDisabled() );
		assertTrue( config.isNoDomainAllowed() );
		assertTrue( config.isDomainSelectablePerEntity() );

		assertFalse( config.isDomainBound( String.class ) );
		assertTrue( config.isDomainBound( WebCmsDomainBound.class ) );
		assertTrue( config.isDomainBound( WebCmsAsset.class ) );
		assertTrue( config.isDomainBound( WebCmsRemoteEndpoint.class ) );
		assertTrue( config.isDomainBound( WebCmsUrl.class ) );
		assertTrue( config.isDomainBound( WebCmsMenuItem.class ) );
		assertFalse( config.isDomainBound( WebCmsTypeSpecifier.class ) );
		assertFalse( config.isDomainBound( WebCmsArticleType.class ) );

		assertFalse( config.isNoDomainAllowed( WebCmsArticle.class ) );
		assertTrue( config.isNoDomainAllowed( WebCmsComponent.class ) );
	}

	@Test
	public void customizeDomainBoundTypes() {
		WebCmsMultiDomainConfiguration config = WebCmsMultiDomainConfiguration
				.managementPerDomain()
				.domainBoundTypes( WebCmsArticleType.class )
				.domainIgnoredTypes( WebCmsImage.class )
				.noDomainAllowedTypes( WebCmsArticle.class )
				.build();

		assertTrue( config.isDomainBound( WebCmsDomainBound.class ) );
		assertTrue( config.isDomainBound( WebCmsAsset.class ) );
		assertTrue( config.isDomainBound( WebCmsArticle.class ) );
		assertFalse( config.isDomainBound( WebCmsImage.class ) );
		assertFalse( config.isDomainBound( WebCmsTypeSpecifier.class ) );
		assertTrue( config.isDomainBound( WebCmsArticleType.class ) );
		assertFalse( config.isDomainBound( WebCmsPageType.class ) );
		assertTrue( config.isNoDomainAllowed( WebCmsArticle.class ) );
	}
}
