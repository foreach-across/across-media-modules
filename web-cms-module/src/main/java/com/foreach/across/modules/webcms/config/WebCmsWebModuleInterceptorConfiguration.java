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

package com.foreach.across.modules.webcms.config;

import com.foreach.across.modules.webcms.domain.endpoint.web.context.WebCmsEndpointContext;
import com.foreach.across.modules.webcms.domain.endpoint.web.interceptor.WebCmsEndpointHandlerInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author Sander Van Loock
 * @since 0.0.1
 */
@Configuration
@RequiredArgsConstructor
public class WebCmsWebModuleInterceptorConfiguration extends WebMvcConfigurerAdapter
{
	private final WebCmsEndpointContext context;

	@Override
	public void addInterceptors( InterceptorRegistry registry ) {
		registry.addInterceptor( webCmsEndpointInterceptor() );
	}

	@Bean
	public WebCmsEndpointHandlerInterceptor webCmsEndpointInterceptor() {
		return new WebCmsEndpointHandlerInterceptor( context );
	}
}
