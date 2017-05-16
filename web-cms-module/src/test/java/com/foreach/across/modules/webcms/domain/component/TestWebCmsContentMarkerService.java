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

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
public class TestWebCmsContentMarkerService
{
	private final WebCmsContentMarkerService markerService = new WebCmsContentMarkerService();

	@Test
	public void emptyNullOrBlankTextContainsNoMarkers() {
		assertFalse( markerService.containsMarkers( null ) );
		assertFalse( markerService.containsMarkers( "" ) );
		assertFalse( markerService.containsMarkers( "  \t" ) );
	}

	@Test
	public void textWithoutMarkers() {
		assertFalse( markerService.containsMarkers( "@@text" ) );
		assertFalse( markerService.containsMarkers( "@@text @@" ) );
		assertFalse( markerService.containsMarkers( "some @@ text @@" ) );
		assertFalse( markerService.containsMarkers( "some @@text( without" ) );
		assertFalse( markerService.containsMarkers( "some @@text (without valid markers)@@" ) );
	}

	@Test
	public void singleMarker() {
		assertEquals( Collections.singletonList( "@@my-marker@@" ), markerService.retrieveMarkers( "@@my-marker@@" ) );
		assertEquals( Collections.singletonList( "@@my-marker(with content)@@" ), markerService.retrieveMarkers( "@@my-marker(with content)@@" ) );
		assertEquals( Collections.singletonList( "@@my-marker(with,content)@@" ), markerService.retrieveMarkers( "some @@my-marker(with,content)@@ text" ) );
	}

	@Test
	public void multipleMarkers() {
		assertEquals(
				Arrays.asList( "@@my-marker@@", "@@other(with parameters)@@" ),
				markerService.retrieveMarkers( "t @@my-marker@@ text @@other(with parameters)@@ more text" )
		);
		assertEquals(
				Arrays.asList( "@@my-marker@@", "@@other(with parameters)@@" ),
				markerService.retrieveMarkers( "@@my-marker@@@@other(with parameters)@@" )
		);
	}

	@Test
	public void replaceMarkersWithCallback() {
		assertEquals(
				"Replaced one and replaced two and one again and @@unknown@@!",
				markerService.replaceMarkers(
						"Replaced @@one@@ and replaced @@one(return two)@@ and @@one@@ again and @@unknown@@!",
						marker -> {
							if ( marker.hasParameters() && marker.getParameterString().equals( "return two" ) ) {
								return "two";
							}
							if ( marker.getKey().equals( "one" ) ) {
								return "one";
							}
							return null;
						}
				)
		);
	}

	@Test
	public void replaceMarkersWithMapValues() {
		Map<String, String> values = new HashMap<>();
		values.put( "@@one@@", "one" );
		values.put( "@@one(return two)@@", "two" );

		assertEquals(
				"Replaced one and replaced two and one again and @@unknown@@!",
				markerService.replaceMarkers( "Replaced @@one@@ and replaced @@one(return two)@@ and @@one@@ again and @@unknown@@!", values )
		);
	}

	@Test
	public void stripMarkers() {
		assertEquals(
				"Replaced  and replaced  and  again and !",
				markerService.stripMarkers("Replaced @@one@@ and replaced @@one(return two)@@ and @@one@@ again and @@unknown@@!")
		);
	}
}
