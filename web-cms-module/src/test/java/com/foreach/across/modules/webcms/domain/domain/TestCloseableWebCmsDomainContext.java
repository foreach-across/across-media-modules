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

package com.foreach.across.modules.webcms.domain.domain;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * @author Arne Vandamme
 * @since 0.0.3
 */
public class TestCloseableWebCmsDomainContext
{
	private WebCmsDomain one = WebCmsDomain.builder().id( 123L ).build();

	@Before
	public void before() {
		WebCmsDomainContextHolder.clearWebCmsDomainContext();
	}

	@After
	public void after() {
		WebCmsDomainContextHolder.clearWebCmsDomainContext();
	}

	@Test
	public void creatingTheCloseableSetsTheDomainContext() {
		WebCmsDomainContext domainContext = new WebCmsDomainContext( one, null );

		assertNull( WebCmsDomainContextHolder.getWebCmsDomainContext() );
		CloseableWebCmsDomainContext closeableWebCmsDomainContext = new CloseableWebCmsDomainContext( domainContext );
		assertSame( domainContext, WebCmsDomainContextHolder.getWebCmsDomainContext() );

		closeableWebCmsDomainContext.close();
		assertNull( WebCmsDomainContextHolder.getWebCmsDomainContext() );
	}

	@Test
	public void creatingACloseableWithNullContextIsAllowed() {
		WebCmsDomainContext domainContext = new WebCmsDomainContext( one, null );
		WebCmsDomainContextHolder.setWebCmsDomainContext( domainContext );

		try (CloseableWebCmsDomainContext ignore = new CloseableWebCmsDomainContext( null )) {
			assertNull( WebCmsDomainContextHolder.getWebCmsDomainContext() );
		}

		assertSame( domainContext, WebCmsDomainContextHolder.getWebCmsDomainContext() );
	}
}
