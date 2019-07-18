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

import com.foreach.across.modules.webcms.domain.domain.web.AbstractWebCmsDomainContextFilter;
import com.foreach.across.modules.webcms.domain.domain.web.WebCmsSiteConfigurationFilter;
import com.foreach.across.test.MockAcrossServletContext;
import com.foreach.across.test.MockFilterRegistration;
import it.AbstractMultiDomainCmsApplicationWithTestDataIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Arne Vandamme
 * @since 0.0.3
 */
public class ITWebCmsSiteConfigurationFilter extends AbstractMultiDomainCmsApplicationWithTestDataIT
{
	@Autowired
	private MockAcrossServletContext servletContext;

	@Test
	void defaultFilterShouldBeRegistered() {
		MockFilterRegistration filterRegistration = servletContext.getFilterRegistration( AbstractWebCmsDomainContextFilter.FILTER_NAME );
		assertNotNull( filterRegistration );
		assertTrue( filterRegistration.getFilter() instanceof WebCmsSiteConfigurationFilter );
	}

	@Test
	void noDomainIsDefault() {
		getAndExpect( "http://unknown.foreach.be/domain", "no-domain:it-IT" );
	}

	@Test
	void matchOnDomainWithoutPort() {
		getAndExpect( "http://foreach.be/domain", "be-foreach:de-DE" );
		getAndExpect( "http://nl.foreach.be/domain", "be-foreach:nl-BE" );
		getAndExpect( "http://fr.foreach.be/domain", "be-foreach:it-IT" );
	}

	@Test
	void portBasedDomainMatches() {
		getAndExpect( "http://nl.foreach.be:8080/domain", "de-foreach:nl-NL" );
		getAndExpect( "http://fr.foreach.be:8080/domain", "de-foreach:nl-NL" );
	}

	@Test
	void inactiveDomainIsIgnored() {
		getAndExpect( "http://foreach.be:8080/domain", "be-foreach:de-DE" );
	}
}
