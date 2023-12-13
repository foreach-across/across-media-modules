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

package com.foreach.across.modules.webcms.domain.component;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
public class TestWebCmsContentMarker
{
	@Test
	public void markerKeyIsRequired() {
		assertThrows( IllegalArgumentException.class, () -> {
			new WebCmsContentMarker( null );
		} );
	}

	@Test
	public void markerKeyMustNotBeBlank() {
		assertThrows( IllegalArgumentException.class, () -> {
			new WebCmsContentMarker( "" );
		} );
	}

	@Test
	public void markerKeyMayNotContainWhiteSpace() {
		assertThrows( IllegalArgumentException.class, () -> {
			new WebCmsContentMarker( "test me" );
		} );
	}

	@Test
	public void defineMarker() {
		WebCmsContentMarker marker = new WebCmsContentMarker( "wcm:placeholder" );
		assertEquals( "@@wcm:placeholder@@", marker.toString() );
		assertFalse( marker.hasParameters() );
		assertEquals( "wcm:placeholder", marker.getKey() );
		assertNull( marker.getParameterString() );

		marker = new WebCmsContentMarker( "wcm:placeholder", "place holder name, other" );
		assertEquals( "@@wcm:placeholder(place holder name, other)@@", marker.toString() );
		assertTrue( marker.hasParameters() );
		assertEquals( "wcm:placeholder", marker.getKey() );
		assertEquals( "place holder name, other", marker.getParameterString() );
	}

	@Test
	public void parseMarker() {
		WebCmsContentMarker marker = WebCmsContentMarker.fromMarkerString( "@@wcm:placeholder@@" );
		assertFalse( marker.hasParameters() );
		assertEquals( "wcm:placeholder", marker.getKey() );
		assertNull( marker.getParameterString() );

		marker = WebCmsContentMarker.fromMarkerString( "@@wcm:component(my placeholder('ç, name test@localhost)@@" );
		assertTrue( marker.hasParameters() );
		assertEquals( "wcm:component", marker.getKey() );
		assertEquals( "my placeholder('ç, name test@localhost", marker.getParameterString() );
	}

	@Test
	public void boundariesAreOptionalWhenParsing() {
		WebCmsContentMarker marker = WebCmsContentMarker.fromMarkerString( "wcm:placeholder" );
		assertFalse( marker.hasParameters() );
		assertEquals( "wcm:placeholder", marker.getKey() );
		assertNull( marker.getParameterString() );

		marker = WebCmsContentMarker.fromMarkerString( "wcm:component(my placeholder('ç, name test@localhost)" );
		assertTrue( marker.hasParameters() );
		assertEquals( "wcm:component", marker.getKey() );
		assertEquals( "my placeholder('ç, name test@localhost", marker.getParameterString() );
	}
}
