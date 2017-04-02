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

package com.foreach.across.modules.webcms.web.asset;

import com.foreach.across.modules.web.mvc.condition.CustomRequestMapping;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.web.endpoint.controllers.WebCmsEndpointMapping;
import org.springframework.http.HttpStatus;

import java.lang.annotation.*;

/**
 * Mapping for a {@link WebCmsAsset} endpoint.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@WebCmsEndpointMapping(value = WebCmsAssetEndpoint.class)
@CustomRequestMapping(WebCmsAssetCondition.class)
public @interface WebCmsAssetMapping
{
	/**
	 * Asset type.
	 */
	Class<?> value();

	HttpStatus[] status() default {};/* default HttpStatus.OK*/

	HttpStatus.Series[] series() default HttpStatus.Series.SUCCESSFUL;
}
