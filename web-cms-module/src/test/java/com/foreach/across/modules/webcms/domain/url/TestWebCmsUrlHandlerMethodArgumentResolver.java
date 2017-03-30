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

import com.foreach.across.modules.webcms.domain.page.WebCmsPageEndpoint;
import com.foreach.across.modules.webcms.web.endpoint.context.ConfigurableWebCmsEndpointContext;
import com.foreach.across.modules.webcms.web.endpoint.context.DefaultWebCmsEndpointContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.MethodParameter;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;

/**
 * @author: Sander Van Loock
 * @since: 0.0.1
 */
@RunWith(MockitoJUnitRunner.class)

public class TestWebCmsUrlHandlerMethodArgumentResolver
{
	@Mock
	private MethodParameter methodThatShouldResolve;

	@Mock
	private MethodParameter methodThatShouldNotResolve;

	private ConfigurableWebCmsEndpointContext context;
	private WebCmsUrlHandlerMethodArgumentResolver resolver;
	private WebCmsUrl url;

	@Before
	public void setUp() throws Exception {
		context = new DefaultWebCmsEndpointContext();
		url = WebCmsUrl.builder().build();
		context.setUrl( url );
		context.setEndpoint( WebCmsPageEndpoint.builder().build() );
		context.setResolved( true );
		resolver = new WebCmsUrlHandlerMethodArgumentResolver( context );

		doReturn( WebCmsUrl.class ).when( methodThatShouldResolve ).getParameterType();
		doReturn( String.class ).when( methodThatShouldNotResolve ).getParameterType();
	}

	@Test
	public void supportsParameter() throws Exception {
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