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

package com.foreach.across.modules.webcms.domain.domain.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Arne Vandamme
 * @since 0.0.3
 */
@ExtendWith(MockitoExtension.class)
public class TestWebCmsSiteConfigurationFilter
{
	@InjectMocks
	private WebCmsSiteConfigurationFilter filter;

	@Test
	public void hostPatternMatching() {
		assertTrue( hostNameMatches( "localhost", "localhost" ) );
		assertTrue( hostNameMatches( "localhost:8080", "localhost" ) );
		assertTrue( hostNameMatches( "localhost:443", "localhost" ) );
		assertFalse( hostNameMatches( "localhost", "localhost:8080" ) );
		assertTrue( hostNameMatches( "localhost:8080", "localhost:8080" ) );
		assertTrue( hostNameMatches( "foreach.be", "foreach.be" ) );
		assertFalse( hostNameMatches( "foreach_be", "foreach.be" ) );
		assertTrue( hostNameMatches( "nl.foreach.be", "*.foreach.be" ) );
		assertFalse( hostNameMatches( "sub.nl.foreach.be", "*.foreach.be" ) );
		assertTrue( hostNameMatches( "sub.nl.foreach.be", "*.*.foreach.be" ) );
		assertTrue( hostNameMatches( "sub.nl.foreach.be", "**.foreach.be" ) );
		assertTrue( hostNameMatches( "sub.nl.foreach.be", "sub.*.foreach.be" ) );
		assertTrue( hostNameMatches( "sub.nl.foreach.be", "sub.**.be" ) );
		assertTrue( hostNameMatches( "nl.foreach.be:8080", "*.foreach.be" ) );
		assertTrue( hostNameMatches( "nl.foreach.be:8080", "*.foreach.*:8080" ) );
		assertFalse( hostNameMatches( "nl.foreach.be:8080", "*.foreach.*:443" ) );
	}

	private boolean hostNameMatches( String hostName, String pattern ) {
		Pattern p = filter.compileHostPattern( pattern );
		assertNotNull( p );
		return p.matcher( hostName ).matches();
	}
}
