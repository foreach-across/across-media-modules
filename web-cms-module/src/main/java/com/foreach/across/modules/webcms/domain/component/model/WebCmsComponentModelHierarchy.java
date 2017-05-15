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

import com.foreach.across.core.annotations.Exposed;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents an ordered hierarchy of {@link OrderedWebComponentModelSet}s that have a scope name.
 * Components can be looked up by name in the hierarchy in which case (depending on the parameter)
 * all registered sets will be queried in reverse order until a result is returned.
 * <p/>
 * The hierarchy will always contain an initial scope name {@link #GLOBAL}.
 * The global scope component set is special in that it will fetch a component from the repository only when it is requested.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
@Exposed
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WebCmsComponentModelHierarchy
{
	public static final String REQUEST_ATTRIBUTE = "webCmsComponents";

	public static final String GLOBAL = "global";

	private List<OrderedWebComponentModelSet> componentModelSets = new ArrayList<>();

	@Autowired
	void buildGlobalComponentModelSet( WebCmsComponentModelService webCmsComponentModelService ) {
		addComponents( new OrderedWebComponentModelSet(
				null, GLOBAL,
				( owner, componentName ) -> webCmsComponentModelService.getComponentModelByName( componentName, owner )
		) );
	}

	@Autowired
	void registerAsRequestAttribute( HttpServletRequest request ) {
		request.setAttribute( REQUEST_ATTRIBUTE, this );
	}

	/**
	 * Add a set of components to the very end of the hierarchy.
	 * The new set will be the very first one that will be looked in.
	 *
	 * @param componentModelSet to add
	 */
	public void addComponents( OrderedWebComponentModelSet componentModelSet ) {
		Assert.notNull( componentModelSet.getScopeName(), "A scope name is required to add a set to a hierarchy." );
		Assert.isNull(
				getComponentsForScope( componentModelSet.getScopeName() ),
				"Scope names must be unique in a hierarchy - another set already has this scope."
		);
		componentModelSets.add( componentModelSet );
	}

	/**
	 * Add a set of components before an already registered scope.  The existing components will be used before the new set.
	 * If the scope is not yet present, an exception will be thrown.
	 *
	 * @param componentModelSet to add
	 * @param scopeNameAfter    name of the component scope that comes after
	 */
	public void addComponentsBefore( OrderedWebComponentModelSet componentModelSet, String scopeNameAfter ) {
		OrderedWebComponentModelSet componentsForScope = getComponentsForScope( scopeNameAfter );
		Assert.notNull( componentModelSet, "No components registered for scope " + scopeNameAfter );

		componentModelSets.add( componentModelSets.indexOf( componentsForScope ), componentModelSet );
	}

	/**
	 * @return true if the component set for that scope has been removed
	 */
	public boolean removeComponents( String scopeName ) {
		return Optional.ofNullable( getComponentsForScope( scopeName ) )
		               .map( this::removeComponents )
		               .orElse( false );
	}

	/**
	 * @return true if component set has been removed
	 */
	public boolean removeComponents( OrderedWebComponentModelSet componentModelSet ) {
		return componentModelSets.remove( componentModelSet );
	}

	/**
	 * @param scopeName name
	 * @return components registered to that scope
	 */
	public OrderedWebComponentModelSet getComponentsForScope( String scopeName ) {
		Assert.notNull( scopeName );

		for ( OrderedWebComponentModelSet modelSet : componentModelSets ) {
			if ( scopeName.equals( modelSet.getScopeName() ) ) {
				return modelSet;
			}
		}

		return null;
	}

	/**
	 * Re-order all scopes.  All registered scope names must be mentioned or an exception will be thrown.
	 * Any unknown scope name will be ignored.
	 * <p/>
	 * Scope names are defined left-to-right but scopes will be traversed right-to-left when looking for components.
	 *
	 * @param scopeNames in order
	 */
	public void setScopeOrder( String... scopeNames ) {
		componentModelSets.sort( Comparator.comparingInt( modelSet -> ArrayUtils.indexOf( scopeNames, modelSet.getScopeName() ) ) );
	}

	/**
	 * @return all registered scope names in their order
	 */
	public Collection<String> getScopeNames() {
		return componentModelSets.stream()
		                         .map( OrderedWebComponentModelSet::getScopeName )
		                         .collect( Collectors.toList() );
	}

	/**
	 * Get the first scope that will be checked when requesting a component.
	 *
	 * @return scope name or {@code null} if the hierarchy is empty
	 */
	public String getDefaultScope() {
		return componentModelSets.isEmpty() ? null : componentModelSets.get( componentModelSets.size() - 1 ).getScopeName();
	}

	/**
	 * Get the component by name by traversing the entire hierarchy bottom up.
	 *
	 * @param componentName name of the component
	 * @return component or null if not found
	 */
	public WebCmsComponentModel get( String componentName ) {
		return get( componentName, true );
	}

	/**
	 * Get the component by name and optionally traverse the hierarchy.
	 *
	 * @param componentName      name of the component
	 * @param searchParentScopes true if parent scopes should be searched as well
	 * @return component or null if not found
	 */
	public WebCmsComponentModel get( String componentName, boolean searchParentScopes ) {
		WebCmsComponentModel component = null;

		for ( int i = componentModelSets.size() - 1; i >= 0 && component == null; i-- ) {
			component = componentModelSets.get( i ).get( componentName );
			if ( !searchParentScopes ) {
				break;
			}
		}

		return component;
	}

	/**
	 * Get the component by name by traversing the hierarchy bottom up, starting with the specific scope.
	 * Any scope below this one will be ignored.
	 *
	 * @param componentName name of the component
	 * @param scopeName     name of the scope to start with
	 * @return component or null if not found
	 */
	public WebCmsComponentModel getFromScope( String componentName, String scopeName ) {
		return getFromScope( componentName, scopeName, true );
	}

	/**
	 * Get the component by name from a specific scope.  Optionally traverse parent scopes looking for the component.
	 *
	 * @param componentName      name of the component
	 * @param scopeName          name of the scope to start with
	 * @param searchParentScopes true if parent scopes should be searched as well
	 * @return component or null if not found
	 */
	public WebCmsComponentModel getFromScope( String componentName, String scopeName, boolean searchParentScopes ) {
		Assert.notNull( scopeName );
		WebCmsComponentModel component = null;

		boolean scopeFound = false;

		for ( int i = componentModelSets.size() - 1; i >= 0 && component == null; i-- ) {
			OrderedWebComponentModelSet modelSet = componentModelSets.get( i );
			if ( scopeFound || scopeName.equals( modelSet.getScopeName() ) ) {
				scopeFound = true;
				component = modelSet.get( componentName );
				if ( !searchParentScopes ) {
					break;
				}
			}
		}

		return component;
	}

	/**
	 * @return true if components scope is registered
	 */
	public boolean containsScope( String scopeName ) {
		return getComponentsForScope( scopeName ) != null;
	}
}
