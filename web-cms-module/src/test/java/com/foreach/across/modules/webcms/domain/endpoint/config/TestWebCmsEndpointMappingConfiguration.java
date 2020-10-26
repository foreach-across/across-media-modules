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

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
public class TestWebCmsEndpointMappingConfiguration
{
	private WebCmsEndpointMappingConfiguration configuration = new WebCmsEndpointMappingConfiguration();

	@Test
	public void byDefaultAllRequestsAreAllowed() {
		assertTrue( configuration.shouldMapToWebCmsUrl( "/my/path" ) );
	}

	@Test
	public void ifIncludedPatternsRequestMustMatch() {
		configuration.setIncludedPathPatterns( Arrays.asList( "/my/*", "/**/path" ) );

		assertTrue( configuration.shouldMapToWebCmsUrl( "/my/test" ) );
		assertTrue( configuration.shouldMapToWebCmsUrl( "/my/other/path" ) );
		assertFalse( configuration.shouldMapToWebCmsUrl( "/not/test" ) );
	}

	@Test
	public void excludeTakesPrecedenceOverIncluded() {
		configuration.setExcludedPathPatterns( Collections.singletonList( "/my/path" ) );

		assertFalse( configuration.shouldMapToWebCmsUrl( "/my/path" ) );
		assertTrue( configuration.shouldMapToWebCmsUrl( "/my/other/path" ) );
		assertTrue( configuration.shouldMapToWebCmsUrl( "/not/test" ) );

		configuration.setIncludedPathPatterns( Arrays.asList( "/my/*", "/**/path" ) );
		assertFalse( configuration.shouldMapToWebCmsUrl( "/my/path" ) );
		assertTrue( configuration.shouldMapToWebCmsUrl( "/my/other/path" ) );
		assertFalse( configuration.shouldMapToWebCmsUrl( "/not/test" ) );
	}
}
