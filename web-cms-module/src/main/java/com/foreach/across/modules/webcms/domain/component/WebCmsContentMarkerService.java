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

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * API for checking and parsing markers from a text.
 * A marker is any fragment of the form @@fragment@@, all text between the @@ is considered the marker that should be replaced.
 * <p/>
 * Using markers can be very convenient but take into account that performance wise the use of custom templates of custom
 * rendering for component types is most likely better.
 *
 * @author Arne Vandamme
 * @see com.foreach.across.modules.webcms.web.thymeleaf.WebCmsComponentContentMarkerRenderer
 * @since 0.0.2
 */
@Service
public class WebCmsContentMarkerService
{
	public static Pattern MARKER_PATTERN = Pattern.compile( "@@[^\\s]+?(\\(.*?\\))?@@" );

	/**
	 * @param marker string
	 * @return marker instance
	 */
	public WebCmsContentMarker parseMarker( String marker ) {
		return WebCmsContentMarker.fromMarkerString( marker );
	}

	/**
	 * @param text to check for markers
	 * @return true if the text contains any markers
	 */
	public boolean containsMarkers( String text ) {
		return !retrieveMarkers( text ).isEmpty();
	}

	/**
	 * Retrieve the unique markers in a text.
	 *
	 * @param text to parse the markers from
	 * @return collection of markers found - can be empty
	 */
	public Collection<String> retrieveMarkers( String text ) {
		if ( !StringUtils.isBlank( text ) ) {
			List<String> markers = new ArrayList<>();
			Matcher matcher = MARKER_PATTERN.matcher( text );

			while ( matcher.find() ) {
				markers.add( matcher.group() );
			}

			return markers;
		}

		return Collections.emptyList();
	}

	/**
	 * Replace all markers in the text specified.  Retrieves the value from the map passed as parameter.
	 *
	 * @param text         to replace the markers in
	 * @param markerValues map of value for marker
	 * @return text with all markers replaced
	 */
	public String replaceMarkers( String text, final Map<String, String> markerValues ) {
		return replaceMarkers( text, marker -> markerValues.get( marker.toString() ) );
	}

	/**
	 * Simply strip all markers from the text.
	 *
	 * @param text to remove all markers from
	 * @return text with markers removed
	 */
	public String stripMarkers( String text ) {
		return replaceMarkers( text, marker -> "" );
	}

	/**
	 * Replace all markers in the text specified.  Uses the value function callback to determine the marker value.
	 * Null values will not be replaced!
	 *
	 * @param text                to replace the markers in
	 * @param markerValueFunction to determine a marker value
	 * @return text with all markers replaced
	 */
	public String replaceMarkers( String text, Function<WebCmsContentMarker, String> markerValueFunction ) {
		Assert.notNull( markerValueFunction );

		if ( !StringUtils.isEmpty( text ) ) {
			StringBuilder stringBuilder = new StringBuilder( text.length() );

			Matcher matcher = MARKER_PATTERN.matcher( text );

			int start = 0;

			while ( matcher.find() ) {
				stringBuilder.append( text.substring( start, matcher.start() ) );
				stringBuilder.append( StringUtils.defaultString( markerValueFunction.apply( parseMarker( matcher.group() ) ), matcher.group() ) );
				start = matcher.end();
			}

			stringBuilder.append( text.substring( start ) );

			return stringBuilder.toString();
		}

		return text;
	}
}
