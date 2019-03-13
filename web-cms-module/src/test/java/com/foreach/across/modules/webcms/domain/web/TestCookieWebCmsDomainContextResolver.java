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

package com.foreach.across.modules.webcms.domain.web;

import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainContext;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainService;
import com.foreach.across.modules.webcms.domain.domain.web.CookieWebCmsDomainContextResolver;
import com.foreach.across.modules.webcms.domain.domain.web.WebCmsDomainChangedEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.Cookie;

import static org.mockito.Mockito.*;

/**
 * @author Marc Vanbrabant
 * @since 0.0.7
 */
@RunWith(MockitoJUnitRunner.class)
public class TestCookieWebCmsDomainContextResolver
{
	@Mock
	private WebCmsDomainService webCmsDomainService;
	@Mock
	private ApplicationEventPublisher eventPublisher;

	private CookieWebCmsDomainContextResolver resolver;
	private MockHttpServletResponse response;
	private MockHttpServletRequest request;

	@Before
	public void setup() {
		resolver = spy( new CookieWebCmsDomainContextResolver( webCmsDomainService, eventPublisher ) );
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		request.setCookies( new Cookie( CookieWebCmsDomainContextResolver.DEFAULT_COOKIE_NAME, "wcm:domain:across.net" ) );
	}

	@Test
	public void switchingDomainToNullDomainShouldRemoveCookie() {
		resolver.setDomainContext( request, response, null );
		verify( resolver ).removeCookie( response );
		verifyNoMoreInteractions( eventPublisher );
	}

	@Test
	public void switchingDomainToNoDomainShouldSetCookieAndSendEvent() {
		WebCmsDomainContext context = WebCmsDomainContext.noDomain();
		resolver.setDomainContext( request, response, context );
		verify( resolver ).addCookie( response, "no-domain" );
		verify( eventPublisher ).publishEvent( new WebCmsDomainChangedEvent( "wcm:domain:across.net", "no-domain" ) );
	}

	@Test
	public void switchingDomainToOtherDomainShouldSetCookieAndSendEvent() {
		WebCmsDomainContext context = new WebCmsDomainContext( WebCmsDomain.builder().objectId( "wcm:domain:across.dev" ).build(), null );
		resolver.setDomainContext( request, response, context );
		verify( resolver ).addCookie( response, "wcm:domain:across.dev" );
		verify( eventPublisher ).publishEvent( new WebCmsDomainChangedEvent( "wcm:domain:across.net", "wcm:domain:across.dev" ) );
	}
}
