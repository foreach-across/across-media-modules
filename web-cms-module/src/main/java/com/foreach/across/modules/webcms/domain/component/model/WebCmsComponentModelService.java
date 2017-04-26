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

package com.foreach.across.modules.webcms.domain.component.model;

import com.foreach.across.modules.webcms.domain.WebCmsObject;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentType;

/**
 * Core API for interacting with renderable/editable {@link WebCmsComponentModel}s.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
public interface WebCmsComponentModelService
{
	/**
	 * Create a new component model for a specific component type.
	 *
	 * @param componentType to create model for
	 * @return model
	 */
	WebCmsComponentModel createComponentModel( WebCmsComponentType componentType );

	/**
	 * Get a single {@link WebCmsComponentModel} by name.
	 *
	 * @param componentName name of the component
	 * @param owner         of the component
	 * @return component or null if not found
	 */
	WebCmsComponentModel getComponentModel( String componentName, WebCmsObject owner );

	/**
	 * Get all {@link WebCmsComponentModel}s owned by a the {@link WebCmsObject}.
	 * The returned set will have all components according to their index order.
	 *
	 * @param object owner
	 * @return set of components
	 */
	OrderedWebComponentModelSet getComponentModelsForOwner( WebCmsObject object );

	/**
	 * Build the {@link WebCmsComponentModel} for a particular {@link WebCmsComponent} entity.
	 *
	 * @param component to build the model for
	 * @return model
	 */
	WebCmsComponentModel buildModelForComponent( WebCmsComponent component );

	/**
	 * Save a {@link WebCmsComponentModel} to the repository.
	 * The return value is the main {@link WebCmsComponent} that this model represents.
	 *
	 * @param componentModel to save
	 * @return component
	 */
	WebCmsComponent save( WebCmsComponentModel componentModel );
}
