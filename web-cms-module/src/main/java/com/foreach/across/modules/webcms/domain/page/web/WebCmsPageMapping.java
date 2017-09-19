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
import com.foreach.across.modules.webcms.domain.asset.web.WebCmsAssetMapping;
import com.foreach.across.modules.webcms.domain.domain.web.WebCmsDomainMapping;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.WebCmsPageType;
import org.springframework.core.annotation.AliasFor;
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
@WebCmsAssetMapping(value = WebCmsPage.class) //I think this should be here, however the comparison doesn't work when it is.
@CustomRequestMapping(WebCmsPageCondition.class)
public @interface WebCmsPageMapping
{
	/**
	 * Mapping will only apply if the {@link com.foreach.across.modules.webcms.domain.url.WebCmsUrl} that it
	 * matched on has a status in this series configured.
	 */
	HttpStatus.Series[] series() default HttpStatus.Series.SUCCESSFUL;

	/**
	 * Mapping will only apply if the {@link com.foreach.across.modules.webcms.domain.url.WebCmsUrl} that it
	 * matched on has this status configured.
	 */
	HttpStatus[] status() default {};

	/**
	 * Page types this mapping applies for.
	 * An page type can be specified either by its {@link WebCmsPageType#getObjectId()} or {@link WebCmsPageType#getTypeKey()}.
	 */
	String[] pageType() default {};

	/**
	 * Canonical paths this mapping applies for.
	 */
	String[] canonicalPath() default {};

	/**
	 * Globally unique asset object ids.  Mapping will only apply for this list of assets.
	 */
	String[] objectId() default {};

	/**
	 * Mapping will only apply if the current {@link com.foreach.across.modules.webcms.domain.domain.WebCmsDomainContext}
	 * if for any of the domains specified. If the array of domains is empty, mapping will always apply.
	 * <p/>
	 * A domain is specified by its domain key, {@code null} represent the explicit no-domain.
	 */
	@AliasFor(annotation = WebCmsDomainMapping.class, attribute = "value")
	String[] domain() default {};
}
