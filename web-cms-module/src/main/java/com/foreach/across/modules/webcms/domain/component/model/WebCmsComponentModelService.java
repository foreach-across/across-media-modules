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
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;

import java.util.Collection;

/**
 * Core API for interacting with renderable/editable {@link WebCmsComponentModel}s.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
public interface WebCmsComponentModelService
{
	/**
	 * Get the component type represented by the type key.
	 * Will use the current domain to look for component types.
	 *
	 * @param componentTypeKey type key
	 * @return type or {@code null} if not found
	 */
	WebCmsComponentType getComponentType( String componentTypeKey );

	/**
	 * Get the component type represented by the type key on the specified domain.
	 *
	 * @param componentTypeKey type key
	 * @param domain           to scan
	 * @return type of {@code null} if not found
	 */
	WebCmsComponentType getComponentType( String componentTypeKey, WebCmsDomain domain );

	/**
	 * Create a new component model for a specific component type.
	 *
	 * @param componentTypeKey to create model for
	 * @param expectedType     type to coerce to
	 * @return model
	 */
	<U extends WebCmsComponentModel> U createComponentModel( String componentTypeKey, Class<U> expectedType );

	/**
	 * Create a new component model for a specific component type on the specified domain.
	 * Component will be added to that domain and component type will be looked for in the domain.
	 *
	 * @param componentTypeKey to create model for
	 * @param expectedType     type to coerce to
	 * @return model
	 */
	<U extends WebCmsComponentModel> U createComponentModel( String componentTypeKey, WebCmsDomain domain, Class<U> expectedType );

	/**
	 * Create a new component model for a specific component type, on the current domain.
	 *
	 * @param componentType to create model for
	 * @param expectedType  type to coerce to
	 * @return model
	 */
	<U extends WebCmsComponentModel> U createComponentModel( WebCmsComponentType componentType, Class<U> expectedType );

	/**
	 * Get the component model for a specific {@link WebCmsComponent} identified by its object id.
	 *
	 * @param objectId of the component
	 * @return component model or {@code null} if not a valid component
	 */
	WebCmsComponentModel getComponentModel( String objectId );

	/**
	 * Get the component model for a specific {@link WebCmsComponent} identified by its object id.
	 *
	 * @param objectId     of the component
	 * @param expectedType type to coerce to
	 * @return component model or {@code null} if not a valid component
	 */
	<U extends WebCmsComponentModel> U getComponentModel( String objectId, Class<U> expectedType );

	/**
	 * Get a single {@link WebCmsComponentModel} for a specific owner by name.
	 * Calls {@link #getComponentModelByNameAndDomain(String, WebCmsObject, WebCmsDomain)} with the
	 * domain of the owner object, if it is an instance of {@link WebCmsComponentType}, otherwise with the
	 * domain provided by {@link com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService#getCurrentDomainForType(Class<WebCmsComponent>)}.
	 *
	 * @param componentName name of the component
	 * @param owner         of the component
	 * @return component or null if not found
	 */
	WebCmsComponentModel getComponentModelByName( String componentName, WebCmsObject owner );

	/**
	 * Get a single {@link WebCmsComponentModel} for a specific owner by name and domain.
	 *
	 * @param componentName name of the component
	 * @param owner         of the component
	 * @param domain        domain of the component
	 * @return component or null if not found
	 */
	WebCmsComponentModel getComponentModelByNameAndDomain( String componentName, WebCmsObject owner, WebCmsDomain domain );

	/**
	 * Get a single {@link WebCmsComponentModel} by name.
	 * Will use the context-bound domain to lookup the component.
	 *
	 * @param componentName name of the component
	 * @param owner         of the component
	 * @param expectedType  type to coerce to
	 * @return component or null if not found
	 */
	<U extends WebCmsComponentModel> U getComponentModelByName( String componentName, WebCmsObject owner, Class<U> expectedType );

	/**
	 * Get a single {@link WebCmsComponentModel} for a specific owner by name and domain.
	 *
	 * @param componentName name of the component
	 * @param owner         of the component
	 * @param domain        domain of the component
	 * @param expectedType  type to coerce to
	 * @return component or null if not found
	 */
	<U extends WebCmsComponentModel> U getComponentModelByNameAndDomain( String componentName, WebCmsObject owner, WebCmsDomain domain, Class<U> expectedType );

	/**
	 * Return all components owned by the {@link WebCmsObject} on the current domain, according to their sort order.
	 *
	 * @param object owner
	 * @return ordered collection of components
	 */
	Collection<WebCmsComponentModel> getComponentModelsForOwner( WebCmsObject object );

	/**
	 * Return all components owned by the {@link WebCmsObject} on the specific domain, according to their sort order.
	 *
	 * @param object owner
	 * @param domain the components should be attached to
	 * @return ordered collection of components
	 */
	Collection<WebCmsComponentModel> getComponentModelsForOwner( WebCmsObject object, WebCmsDomain domain );

	/**
	 * Build a {@link WebCmsComponentModelSet} for all components owned by the {@link WebCmsObject},
	 * and attached to the current domain.
	 * <p>
	 * The returned set only contains components with a configured name.
	 *
	 * @param object owner
	 * @param eager  {@code true} if component models should all be fetched eagerly,
	 *               if {@code false} the set will only fetch the component when it is being requested
	 * @return set of components
	 */
	WebCmsComponentModelSet buildComponentModelSetForOwner( WebCmsObject object, boolean eager );

	/**
	 * Build a {@link WebCmsComponentModelSet} for all components owned by the {@link WebCmsObject},
	 * and attached to the specified domain.
	 * <p>
	 * The returned set only contains components with a configured name.
	 *
	 * @param object owner
	 * @param domain the components should be attached to
	 * @param eager  {@code true} if component models should all be fetched eagerly,
	 *               if {@code false} the set will only fetch the component when it is being requested
	 * @return set of components
	 */
	WebCmsComponentModelSet buildComponentModelSetForOwner( WebCmsObject object, WebCmsDomain domain, boolean eager );

	/**
	 * Build the {@link WebCmsComponentModel} for a particular {@link WebCmsComponent} entity.
	 *
	 * @param component to build the model for
	 * @return model
	 */
	WebCmsComponentModel buildModelForComponent( WebCmsComponent component );

	/**
	 * Build the {@link WebCmsComponentModel} for a particular {@link WebCmsComponent} entity.
	 *
	 * @param component    to build the model for
	 * @param expectedType type to coerce to
	 * @return model
	 */
	<U extends WebCmsComponentModel> U buildModelForComponent( WebCmsComponent component, Class<U> expectedType );

	/**
	 * Save a {@link WebCmsComponent} to the repository. If the component is a new component, this will build the
	 * component model before saving, ensuring support for component templates.
	 *
	 * @param component to save
	 * @return component
	 */
	WebCmsComponent save( WebCmsComponent component );

	/**
	 * Save a {@link WebCmsComponentModel} to the repository.
	 * The return value is the main {@link WebCmsComponent} that this model represents.
	 *
	 * @param componentModel to save
	 * @return component
	 */
	WebCmsComponent save( WebCmsComponentModel componentModel );
}
