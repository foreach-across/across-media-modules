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

package it.multidomainreference;

import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainRepository;
import com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService;
import com.foreach.across.modules.webcms.domain.domain.web.WebCmsSiteConfiguration;
import it.AbstractMultiDomainCmsApplicationWithTestDataIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Arne Vandamme
 * @since 0.0.3
 */
public class ITMultiDomainDomainReferenceData extends AbstractMultiDomainCmsApplicationWithTestDataIT
{
	@Autowired
	private WebCmsDomainRepository domainRepository;

	@Autowired
	private WebCmsMultiDomainService multiDomainService;

	@Test
	void domainMetadataIsSiteConfigurationClass() {
		WebCmsDomain foreach = domainRepository.findOneByDomainKey( "nl-foreach" ).orElse( null );
		assertNotNull( foreach );

		WebCmsSiteConfiguration site = multiDomainService.getMetadataForDomain( foreach, WebCmsSiteConfiguration.class );
		assertNotNull( site );
		assertEquals( "nl-foreach", site.getDomainKey() );
		assertEquals( foreach, site.getDomain() );
	}

	@Test
	void attributesAreImportedAndConvertedToStrongTypes() {
		WebCmsDomain foreach = domainRepository.findOneByDomainKey( "be-foreach" ).orElse( null );
		assertNotNull( foreach );

		WebCmsSiteConfiguration site = multiDomainService.getMetadataForDomain( foreach, WebCmsSiteConfiguration.class );
		assertNotNull( site );

		assertEquals( "some text", site.getAttribute( "customString" ) );
		assertEquals( "123", site.getAttribute( "customNumber" ) );
		assertEquals( Integer.valueOf( 123 ), site.getAttribute( "customNumber", Integer.class ) );

		assertEquals( "be-foreach", site.getDomainKey() );
		assertEquals( foreach.getName(), site.getName() );
		assertEquals( 1, site.getSortIndex() );
		assertEquals( "foreach.be", site.getCookieDomain() );
		assertEquals( Locale.forLanguageTag( "de-DE" ), site.getDefaultLocale() );
		assertEquals(
				Arrays.asList( "foreach.be", "nl.foreach.be", "fr.foreach.be" ),
				site.getHostNames()
		);

		Map<String, Locale> expected = new HashMap<>();
		expected.put( "nl.foreach.be", Locale.forLanguageTag( "nl-BE" ) );
		expected.put( "fr.foreach.be", null );
		assertEquals( expected, site.getLocales() );
	}
}
