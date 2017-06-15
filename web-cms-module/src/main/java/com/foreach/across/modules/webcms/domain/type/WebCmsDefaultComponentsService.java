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

package com.foreach.across.modules.webcms.domain.type;

import com.foreach.across.modules.webcms.domain.WebCmsObject;

import java.util.Map;

/**
 * Core API for creating default data as defined in the type of the object. (@see {@link DefaultComponentsService})
 *
 * @author Raf Ceuls
 * @since 0.0.2
 */
public interface WebCmsDefaultComponentsService
{
	/**
	 * Creates the default components for an object.
	 *
	 * @param asset The asset that will receive the generated data.
	 * @param typeSpecifier The type of the asset.
	 * @param markerValues Any marker values, specified as a map of the format ('@@marker@@', 'replace value')
	 */
	void createDefaultComponents( WebCmsObject asset, WebCmsTypeSpecifier<?> typeSpecifier, Map<String, String> markerValues );
}
