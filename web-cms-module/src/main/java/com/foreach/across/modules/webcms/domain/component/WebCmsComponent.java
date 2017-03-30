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

/**
 * Represents a (visual) component of an asset.
 *
 * @author Arne Vandamme
 * @see com.foreach.across.modules.webcms.domain.asset.WebCmsAsset
 * @since 0.0.1
 */
public class WebCmsComponent
{
	public static final String COLLECTION_ID = "wcm:component";

	/**
	 * Globally unique id for this component.
	 */
	private String assetId;

	/**
	 * Unique asset id of the asset that owns this component.
	 * There is no actual referential integrity here, custom asset implementations must make sure they perform the required cleanup.
	 */
	private String ownerId;

	/**
	 * Optional descriptive title of the component.
	 */
	private String title;

	/**
	 * Raw body of the component. How the body can be managed is determined by the component type.
	 */
	private String body;

	/**
	 * Raw metadata of the component.  How the metadata can be managed is determined by the component type.
	 */
	private String metadata;
}
