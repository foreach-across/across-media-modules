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

package com.foreach.across.modules.webcms.domain.endpoint.config;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.Collection;
import java.util.Collections;

/**
 * Configuration properties for included and excluded path patterns that should
 * be mapped for {@link com.foreach.across.modules.webcms.domain.url.WebCmsUrl} instances.
 * <p/>
 * Only non-excluded patterns wil be allowed, and if included patterns are specified, they
 * must match the specifically included as well.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Component
@Getter
@Setter
@ConfigurationProperties("webCmsModule.urls")
public class WebCmsEndpointMappingConfiguration
{
	private final AntPathMatcher antPathMatcher = new AntPathMatcher();

	/**
	 * Paths to be included for WebCmsUrl mapping.
	 * If empty, all non-excluded paths will be considered.
	 */
	@NonNull
	private Collection<String> includedPathPatterns = Collections.emptyList();

	/**
	 * Paths to always be excluded from WebCmsUrl mapping.
	 * If empty, only included paths will be considered.
	 */
	@NonNull
	private Collection<String> excludedPathPatterns = Collections.emptyList();

	/**
	 * Should the request be mapped to a {@link com.foreach.across.modules.webcms.domain.url.WebCmsUrl}?
	 * Checks if the requested lookup path matches the patterns configured.
	 *
	 * @param path to check
	 * @return true if should be considered for url mapping
	 */
	public boolean shouldMapToWebCmsUrl( String path ) {
		if ( path != null ) {
			for ( String excluded : excludedPathPatterns ) {
				if ( antPathMatcher.match( excluded, path ) ) {
					return false;
				}
			}

			if ( includedPathPatterns.isEmpty() ) {
				return true;
			}
			else {
				for ( String included : includedPathPatterns ) {
					if ( antPathMatcher.match( included, path ) ) {
						return true;
					}
				}
			}
		}

		return false;
	}
}
