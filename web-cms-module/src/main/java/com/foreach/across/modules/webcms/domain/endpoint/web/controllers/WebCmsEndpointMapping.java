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

package com.foreach.across.modules.webcms.domain.endpoint.web.controllers;

import com.foreach.across.modules.web.mvc.condition.CustomRequestMapping;
import com.foreach.across.modules.webcms.domain.domain.web.WebCmsDomainMapping;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint;
import org.springframework.core.annotation.AliasFor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.*;

/**
 * @author Sander Van Loock
 * @since 0.0.1
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@WebCmsDomainMapping
@CustomRequestMapping(WebCmsEndpointCondition.class)
@RequestMapping("**")
public @interface WebCmsEndpointMapping
{
	Class<? extends WebCmsEndpoint> value() default WebCmsEndpoint.class;

	HttpStatus[] status() default {};

	HttpStatus.Series[] series() default {};

	/**
	 * Mapping will only apply if the current {@link com.foreach.across.modules.webcms.domain.domain.WebCmsDomainContext}
	 * if for any of the domains specified. If the array of domains is empty, mapping will always apply.
	 * <p/>
	 * A domain is specified by its domain key, {@code null} represent the explicit no-domain.
	 */
	@AliasFor(annotation = WebCmsDomainMapping.class, attribute = "value")
	String[] domain() default {};
}
