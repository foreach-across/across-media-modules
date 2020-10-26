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

package com.foreach.across.modules.webcms.domain.url;

import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.endpoint.web.context.ConfigurableWebCmsEndpointContext;
import com.foreach.across.modules.webcms.domain.endpoint.web.context.DefaultWebCmsEndpointContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

/**
 * @author Sander Van Loock
 * @since 0.0.1
 */
@ExtendWith(MockitoExtension.class)

public class TestWebCmsUrlHandlerMethodArgumentResolver
{
	@Mock
	private MethodParameter methodThatShouldResolve;

	@Mock
	private MethodParameter methodThatShouldNotResolve;

	private ConfigurableWebCmsEndpointContext context;
	private WebCmsUrlHandlerMethodArgumentResolver resolver;
	private WebCmsUrl url;

	@BeforeEach
	public void setUp() throws Exception {
		context = new DefaultWebCmsEndpointContext();
		url = WebCmsUrl.builder().build();
		context.setUrl( url );
		context.setEndpoint( WebCmsAssetEndpoint.builder().build() );
		context.setResolved( true );
		resolver = new WebCmsUrlHandlerMethodArgumentResolver( context );

	}

	@Test
	public void supportsParameter() {
		doReturn( WebCmsUrl.class ).when( methodThatShouldResolve ).getParameterType();
		doReturn( String.class ).when( methodThatShouldNotResolve ).getParameterType();
		assertTrue( resolver.supportsParameter( methodThatShouldResolve ) );
		assertFalse( resolver.supportsParameter( methodThatShouldNotResolve ) );
	}

	@Test
	public void resolve() throws Exception {
		Object actual = resolver.resolveArgument( methodThatShouldResolve, new ModelAndViewContainer(), null, null );

		assertNotNull( actual );
		assertTrue( WebCmsUrl.class.isAssignableFrom( actual.getClass() ) );
		assertEquals( url, actual );

	}

	@Test
	public void notResolvedContext() throws Exception {
		context.setResolved( false );

		Object actual = resolver.resolveArgument( methodThatShouldResolve, new ModelAndViewContainer(), null, null );

		assertNull( actual );
	}
}
