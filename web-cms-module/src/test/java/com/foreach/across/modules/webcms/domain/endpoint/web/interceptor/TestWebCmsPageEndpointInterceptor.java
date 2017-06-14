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

package com.foreach.across.modules.webcms.domain.endpoint.web.interceptor;

import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.redirect.WebCmsRemoteEndpoint;
import com.foreach.across.modules.webcms.domain.endpoint.web.context.WebCmsEndpointContext;
import com.foreach.across.modules.webcms.domain.page.web.PageTemplateResolver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

/**
 * @author Sander Van Loock
 * @since 0.0.1
 */
@RunWith(MockitoJUnitRunner.class)
public class TestWebCmsPageEndpointInterceptor
{
	@Mock
	private WebCmsEndpointContext context;

	@Mock
	private PageTemplateResolver templateResolver;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private Object handler;

	@Mock
	private ModelAndView mav;

	private WebCmsPageEndpointHandlerInterceptor interceptor;
	private String template = "test";
	private String resolvedTemplate = "th/site/test";

	@Before
	public void setUp() throws Exception {
		interceptor = new WebCmsPageEndpointHandlerInterceptor( context, templateResolver );

		when( context.getEndpoint() ).thenReturn( WebCmsAssetEndpoint.builder()
		                                                             .asset( WebCmsPage.builder()
		                                                                               .template( template )
		                                                                               .build() )
		                                                             .build() );
		when( templateResolver.resolvePageTemplate( template ) ).thenReturn( resolvedTemplate );
	}

	@Test
	public void onlyHandleWhenReferenceView() throws Exception {
		when( mav.getViewName() ).thenReturn( "template" );
		when( mav.isReference() ).thenReturn( false );
		interceptor.postHandle( request, response, handler, mav );

		verify( mav, times( 1 ) ).isReference();
		verifyZeroInteractions( request, response, handler );

	}

	@Test
	public void viewNameShouldBeSet() throws Exception {
		when( mav.getViewName() ).thenReturn( "template" );
		when( mav.isReference() ).thenReturn( true );

		interceptor.postHandle( request, response, handler, mav );

		verify( mav, times( 1 ) ).isReference();
		verify( mav, times( 3 ) ).getViewName();
		verify( mav, times( 1 ) ).setViewName( anyString() );
		verifyZeroInteractions( request, response, handler );
	}

	@Test
	public void nothingHappensWhenWrongEndpointOnContext() throws Exception {
		when( context.getEndpoint( any() ) ).thenReturn( WebCmsRemoteEndpoint.builder()
		                                                                     .targetUrl( "www.google.be" )
		                                                                     .build() );

		interceptor.postHandle( request, response, handler, mav );

		verify( mav, never() ).setViewName( anyString() );
		verifyZeroInteractions( request, response, handler );
	}

}