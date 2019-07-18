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

import it.AbstractMultiDomainCmsApplicationWithTestDataIT;
import org.junit.jupiter.api.Test;

/**
 * @author Arne Vandamme
 * @since 0.0.3
 */
class ITMultiDomainControllers extends AbstractMultiDomainCmsApplicationWithTestDataIT
{
	@Test
	void pageFromRightDomainIsSelected() {
		Html doc = html( "http://foreach.be/cafe" );
		doc.assertElementHasText( "Foreach Cafe (BE)", "title" );

		doc = html( "http://de.foreach.be:8080/cafe" );
		doc.assertElementHasText( "Foreach Cafe (DE)", "title" );
	}

	@Test
	void domainBoundCustomControllers() {
		getAndExpect("http://foreach.be/multi-domain/be-only", "specific" );
		getAndExpect("http://de.foreach.be:8080/multi-domain/be-only", "fallback" );

		getAndExpect("http://foreach.be/multi-domain/test", "test on be" );
		getAndExpect("http://de.foreach.be:8080/multi-domain/test", "test on de" );
	}

	@Test
	void domainBoundPageMappings() {
		getAndExpect("http://foreach.be/domain-mapped-page", "mapped page on be: Domain mapped page BE" );
		getAndExpect("http://de.foreach.be:8080/domain-mapped-page", "mapped page on de: Domain mapped page DE" );
	}
}
