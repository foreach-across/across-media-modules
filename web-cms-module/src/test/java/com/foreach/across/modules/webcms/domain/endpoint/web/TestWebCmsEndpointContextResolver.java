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

package com.foreach.across.modules.webcms.domain.endpoint.web;

import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpointService;
import com.foreach.across.modules.webcms.domain.endpoint.config.WebCmsEndpointMappingConfiguration;
import com.foreach.across.modules.webcms.domain.endpoint.web.context.DefaultWebCmsEndpointContext;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static junit.framework.TestCase.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestWebCmsEndpointContextResolver
{
	@Mock
	private WebCmsEndpointMappingConfiguration mappingConfiguration;

	@Mock
	private WebCmsEndpointService endpointService;

	@InjectMocks
	private DefaultWebCmsEndpointContextResolver resolver;

	private WebCmsUrl url;
	private WebCmsAssetEndpoint endpoint;

	@BeforeEach
	public void setUp() throws Exception {
		endpoint = WebCmsAssetEndpoint.builder().build();
		url = WebCmsUrl.builder().endpoint( endpoint ).build();

		when( mappingConfiguration.shouldMapToWebCmsUrl( anyString() ) ).thenReturn( true );
	}

	@Test
	public void endpointShouldNotBeMappedIfRequestDoesNotMatch() {
		when( mappingConfiguration.shouldMapToWebCmsUrl( anyString() ) ).thenReturn( false );

		DefaultWebCmsEndpointContext context = new DefaultWebCmsEndpointContext();
		HttpServletRequest request = new MockHttpServletRequest();

		resolver.resolve( context, request );

		assertTrue( context.isResolved() );
		verifyNoMoreInteractions( endpointService );
	}

	@Test
	public void afterResolvingContextShouldBeSetWithValidData() throws Exception {
		when( endpointService.getUrlForPath( anyString() ) ).thenReturn( Optional.of( url ) );

		DefaultWebCmsEndpointContext context = new DefaultWebCmsEndpointContext();
		HttpServletRequest request = new MockHttpServletRequest();

		resolver.resolve( context, request );

		assertTrue( context.isResolved() );
		assertThat( url, is( equalTo( context.getUrl() ) ) );
		assertThat( endpoint, is( equalTo( context.getEndpoint() ) ) );
	}

	@Test
	public void afterResolvingContextShouldBeSetWithEmptyData() throws Exception {
		when( endpointService.getUrlForPath( anyString() ) )
				.thenReturn( Optional.empty() );

		DefaultWebCmsEndpointContext context = new DefaultWebCmsEndpointContext();
		HttpServletRequest request = new MockHttpServletRequest();

		resolver.resolve( context, request );

		assertTrue( context.isResolved() );
		assertNull( context.getUrl() );
		assertNull( context.getEndpoint() );
	}

}
