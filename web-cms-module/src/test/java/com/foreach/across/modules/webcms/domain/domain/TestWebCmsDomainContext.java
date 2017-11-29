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

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author Arne Vandamme
 * @since 0.0.3
 */
public class TestWebCmsDomainContext
{
	private WebCmsDomain domain = WebCmsDomain.builder().domainKey( "my-domain" ).build();

	@Test(expected = IllegalArgumentException.class)
	public void unableToCreateContextWithoutDomain() {
		new WebCmsDomainContext( null, null );
	}

	@Test
	public void noDomainContextWithoutMetadata() {
		WebCmsDomainContext ctx = WebCmsDomainContext.noDomain();
		assertTrue( ctx.isNoDomain() );
		assertFalse( ctx.holdsDomain() );
		assertNull( ctx.getDomain() );
		assertNull( ctx.getMetadata() );
		assertFalse( ctx.hasMetadata() );
		assertFalse( ctx.isMetadataOfType( WebCmsDomainAware.class ) );
		assertNull( ctx.getMetadata( CustomDomainAware.class ) );
		assertFalse( ctx.holdsDomain( "my-domain" ) );
		assertTrue( ctx.holdsDomain( null ) );
	}

	@Test
	public void noDomainContextWithMetadata() {
		WebCmsDomainAware metadata = mock( WebCmsDomainAware.class );
		WebCmsDomainContext ctx = WebCmsDomainContext.noDomain( metadata );
		assertTrue( ctx.isNoDomain() );
		assertFalse( ctx.holdsDomain() );
		assertNull( ctx.getDomain() );
		assertSame( metadata, ctx.getMetadata() );
		assertTrue( ctx.hasMetadata() );
		assertTrue( ctx.isMetadataOfType( WebCmsDomainAware.class ) );
		assertFalse( ctx.isMetadataOfType( CustomDomainAware.class ) );
		assertSame( metadata, ctx.getMetadata( WebCmsDomainAware.class ) );
		assertFalse( ctx.holdsDomain( "my-domain" ) );
		assertTrue( ctx.holdsDomain( null ) );
	}

	@Test
	public void domainContextWithoutMetadata() {
		WebCmsDomainContext ctx = new WebCmsDomainContext( domain, null );
		assertFalse( ctx.isNoDomain() );
		assertTrue( ctx.holdsDomain() );
		assertSame( domain, ctx.getDomain() );
		assertNull( ctx.getMetadata() );
		assertFalse( ctx.hasMetadata() );
		assertFalse( ctx.isMetadataOfType( WebCmsDomainAware.class ) );
		assertNull( ctx.getMetadata( CustomDomainAware.class ) );
		assertFalse( ctx.holdsDomain( "my-other-domain" ) );
		assertTrue( ctx.holdsDomain( "my-domain" ) );
		assertFalse( ctx.holdsDomain( null ) );
	}

	@Test
	public void domainContextWithMetadata() {
		WebCmsDomainAware metadata = mock( CustomDomainAware.class );
		WebCmsDomainContext ctx = new WebCmsDomainContext( domain, metadata );
		assertFalse( ctx.isNoDomain() );
		assertTrue( ctx.holdsDomain() );
		assertSame( domain, ctx.getDomain() );
		assertSame( metadata, ctx.getMetadata() );
		assertTrue( ctx.hasMetadata() );
		assertTrue( ctx.isMetadataOfType( CustomDomainAware.class ) );
		assertSame( metadata, ctx.getMetadata( CustomDomainAware.class ) );
		assertFalse( ctx.holdsDomain( "my-other-domain" ) );
		assertTrue( ctx.holdsDomain( "my-domain" ) );
		assertFalse( ctx.holdsDomain( null ) );
	}

	interface CustomDomainAware extends WebCmsDomainAware
	{
	}
}
