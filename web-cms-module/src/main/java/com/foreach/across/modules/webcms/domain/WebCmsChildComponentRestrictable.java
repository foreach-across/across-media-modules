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

package com.foreach.across.modules.webcms.domain;

/**
 * Indicates whether the implementing class should restrict the selectable {@link com.foreach.across.modules.webcms.domain.component.WebCmsComponentType}s
 *
 * @author Steven Gentens
 * @since 0.0.3
 */
public interface WebCmsChildComponentRestrictable
{
	/**
	 * If set to {@code true}, this object will only allow its linked {@link com.foreach.across.modules.webcms.domain.component.WebCmsComponentType}s as an option
	 */
	String CHILD_COMPONENT_RESTRICTED = "childComponentsRestricted";

	/**
	 * @return whether the object is child component restricted.
	 */
	boolean isChildComponentRestricted();
}
