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

package com.foreach.across.modules.webcms.domain.page.web;

import com.foreach.across.modules.web.mvc.condition.CustomRequestMapping;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import org.springframework.http.HttpStatus;

import java.lang.annotation.*;

/**
 * Mapping for a {@link WebCmsPage} endpoint.
 *
 * @author Raf Ceuls
 * @since 0.0.2
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
//@WebCmsAssetMapping(value = WebCmsPage.class) I think this should be here, however the comparison doesn't work when it is.
@CustomRequestMapping(WebCmsPageCondition.class)
public @interface WebCmsPageMapping
{
	HttpStatus[] status() default {};/* default HttpStatus.OK*/

	HttpStatus.Series[] series() default HttpStatus.Series.SUCCESSFUL;

	String[] pageType() default {};

	String[] canonicalPath() default {};
}
