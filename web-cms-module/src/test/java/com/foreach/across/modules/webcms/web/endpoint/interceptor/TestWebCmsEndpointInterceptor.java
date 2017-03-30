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

package com.foreach.across.modules.webcms.web.endpoint.interceptor;

import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import com.foreach.across.modules.webcms.web.endpoint.context.WebCmsEndpointContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;

/**
 * @author: Sander Van Loock
 * @since: 0.0.1
 */
@RunWith(MockitoJUnitRunner.class)
public class TestWebCmsEndpointInterceptor
{
	@Mock
	private WebCmsEndpointContext context;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private Object handler;

	@Mock
	private ModelAndView mav;

	@InjectMocks
	private WebCmsEndpointInterceptor interceptor;

	@Test
	public void postHandle() throws Exception {
		HttpStatus expectedStatus = HttpStatus.ALREADY_REPORTED;
		int expectedStatusCode = expectedStatus.value();
		when( context.getUrl() ).thenReturn( WebCmsUrl.builder().httpStatus( expectedStatus ).build() );
		interceptor = new WebCmsEndpointInterceptor( context );

		interceptor.postHandle( request, response, handler, mav );

		verify( response, times( 1 ) ).setStatus( expectedStatusCode );
		verifyNoMoreInteractions( request, handler, mav );
	}

}