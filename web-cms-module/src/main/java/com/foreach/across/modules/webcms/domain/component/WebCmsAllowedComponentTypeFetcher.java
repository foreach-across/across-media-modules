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

package com.foreach.across.modules.webcms.domain.component;

import com.foreach.across.modules.webcms.domain.WebCmsObject;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;

import java.util.List;

/**
 * Interface that defines which {@link WebCmsComponentType}s can be used as an option for a specified {@link WebCmsObject}
 * @author Steven Gentens
 * @since 0.0.3
 */
public interface WebCmsAllowedComponentTypeFetcher
{
	boolean supports( WebCmsObject owner, WebCmsDomain domain );

	/**
	 * Returns the selectable {@link WebCmsComponentType}s for a specified {@link WebCmsObject} and {@link WebCmsDomain}
	 *
	 * @param owner  to retrieve the component types for
	 * @param domain in which should be searched
	 * @return the selectable component types
	 */
	List<WebCmsComponentType> fetchComponentTypes( WebCmsObject owner, WebCmsDomain domain );
}
