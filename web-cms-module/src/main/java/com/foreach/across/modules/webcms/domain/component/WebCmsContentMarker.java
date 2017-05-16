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

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * Represents a single content marker.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
public final class WebCmsContentMarker
{
	@Getter
	private final String key, parameterString;

	public WebCmsContentMarker( String key ) {
		this( key, null );
	}

	public WebCmsContentMarker( String key, String parameterString ) {
		Assert.isTrue( StringUtils.isNotBlank( key ), "Marker key must not be blank" );
		Assert.isTrue( !StringUtils.containsWhitespace( key ), "Marker key should not contain whitespace" );
		this.key = key;
		this.parameterString = parameterString;
	}

	public boolean hasParameters() {
		return parameterString != null;
	}

	@Override
	public String toString() {
		return "@@" + key + ( hasParameters() ? "(" + parameterString + ")" : "" ) + "@@";
	}

	public static WebCmsContentMarker fromMarkerString( String marker ) {
		if ( marker.startsWith( "@@" ) ) {
			return parseWithoutBoundaries( marker.substring( 2, marker.length() - 2 ) );
		}

		return parseWithoutBoundaries( marker );
	}

	private static WebCmsContentMarker parseWithoutBoundaries( String marker ) {
		if ( marker.endsWith( ")" )) {
			int startOfParameters = marker.indexOf( '(' );
			return new WebCmsContentMarker( marker.substring( 0, startOfParameters ), marker.substring( startOfParameters + 1, marker.lastIndexOf( ')' ) ) );
		}
		return new WebCmsContentMarker( marker );
	}
}
