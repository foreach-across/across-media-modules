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

import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.config.support.PrefixingHandlerMappingConfigurerAdapter;
import com.foreach.across.modules.web.mvc.InterceptorRegistry;
import com.foreach.across.modules.webcms.web.endpoint.context.WebCmsEndpointContext;
import com.foreach.across.modules.webcms.web.endpoint.interceptor.WebCmsEndpointInterceptor;
import com.foreach.across.modules.webcms.web.endpoint.interceptor.WebCmsPageEndpointInterceptor;
import com.foreach.across.modules.webcms.web.page.template.PageTemplateResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: Sander Van Loock
 * @since: 0.0.1
 */
@Configuration
@RequiredArgsConstructor
public class WebCmsWebModuleInterceptorConfiguration extends PrefixingHandlerMappingConfigurerAdapter
{
	private final WebCmsEndpointContext context;
	private final PageTemplateResolver templateResolver;

	@Override
	public void addInterceptors( InterceptorRegistry interceptorRegistry ) {
		interceptorRegistry.addInterceptor( webCmsEndpointInterceptor() );
		interceptorRegistry.addInterceptor( webCmsPageEndpointInterceptor() );
	}

	@Bean
	public WebCmsPageEndpointInterceptor webCmsPageEndpointInterceptor() {
		return new WebCmsPageEndpointInterceptor( context, templateResolver );
	}

	@Bean
	public WebCmsEndpointInterceptor webCmsEndpointInterceptor() {
		return new WebCmsEndpointInterceptor( context );
	}

	@Override
	public boolean supports( String mapperName ) {
		return AcrossWebModule.NAME.equals( mapperName );
	}
}
