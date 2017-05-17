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

import com.foreach.across.modules.webcms.domain.component.container.ContainerWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * General utility functions related to {@link WebCmsComponent}, {@link WebCmsComponentType} and {@link WebCmsComponentModel}.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
public final class WebCmsComponentUtils
{
	private final static String[] CONTAINER_BASE_TYPES = new String[] { ContainerWebCmsComponentModel.TYPE_DYNAMIC, ContainerWebCmsComponentModel.TYPE_FIXED };

	private WebCmsComponentUtils() {
	}

	/**
	 * Get the base type attribute ({@link WebCmsComponentModel#TYPE_ATTRIBUTE}) for a {@link WebCmsComponentType}.
	 *
	 * @param componentType to get the type attribute value from
	 * @return base type or the same as the component type name if none
	 */
	public static String getBaseType( WebCmsComponentType componentType ) {
		return StringUtils.defaultString( componentType.getAttribute( WebCmsComponentModel.TYPE_ATTRIBUTE ), componentType.getTypeKey() );
	}

	/**
	 * Check if a type has a {@link com.foreach.across.modules.webcms.domain.component.container.ContainerWebCmsComponentModel} as base type.
	 *
	 * @param componentType to check
	 * @return true if having a container base type
	 */
	public static boolean isContainerType( WebCmsComponentType componentType ) {
		return ArrayUtils.contains( CONTAINER_BASE_TYPES, getBaseType( componentType ) );
	}

}
