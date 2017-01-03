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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Configuration
public class WebCmsWebConfiguration
{
	/**
	 * Ensure that the resource handlers are executed before any controllers.
	 */
	// todo remove with AX-154
	@Autowired
	public void updateResourceHandlerMappingOrder( SimpleUrlHandlerMapping resourceHandlerMapping ) {
		if ( resourceHandlerMapping.getOrder() == Ordered.LOWEST_PRECEDENCE ) {
			resourceHandlerMapping.setOrder( 0 );
		}
	}
}
