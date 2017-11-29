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

package com.foreach.across.modules.webcms.domain.domain.web;

import com.foreach.across.modules.web.mvc.condition.CustomRequestMapping;

import java.lang.annotation.*;

/**
 * Mapping to filter on the current {@link com.foreach.across.modules.webcms.domain.domain.WebCmsDomainContext}.
 * If configured without any domains, this mapping will always apply.  Usually used in combination with another
 * {@link org.springframework.web.bind.annotation.RequestMapping}.  All other WebCmsModule mapping annotations
 * implicitly add this mapping annotation as well.
 * <p/>
 * Mapping with a domain value will take precedence over a mapping without a domain value.
 * <p/>
 * <strong>NOTE:</strong> the default no-domain must be configured with an explicit {@code null} value as well.
 *
 * @author Arne Vandamme
 * @see com.foreach.across.modules.webcms.domain.endpoint.web.controllers.WebCmsEndpointMapping
 * @since 0.0.3
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@CustomRequestMapping(WebCmsDomainCondition.class)
public @interface WebCmsDomainMapping
{
	/**
	 * Mapping will only apply if the current {@link com.foreach.across.modules.webcms.domain.domain.WebCmsDomainContext}
	 * if for any of the domains specified. If the array of domains is empty, mapping will always apply.
	 * <p/>
	 * A domain is specified by its domain key, {@code null} represent the explicit no-domain.
	 */
	String[] value() default {};
}
