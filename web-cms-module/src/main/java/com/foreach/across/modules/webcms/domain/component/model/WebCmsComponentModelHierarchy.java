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
import lombok.AllArgsConstructor;
import lombok.Getter;
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
 * Represents an ordered hierarchy of {@link WebCmsComponentModelSet}s that have a scope name.
 * Components can be looked up by name in the hierarchy in which case (depending on the parameter)
 * all registered sets will be queried in reverse order until a result is returned.
 * <p/>
 * The hierarchy will always contain an initial scope name {@link #GLOBAL}.
 * The global scope component set will fetch a component from the repository only when it is requested.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Component
@Exposed
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WebCmsComponentModelHierarchy
{
	public static final String REQUEST_ATTRIBUTE = "webCmsComponents";

	public static final String GLOBAL = "global";
	public static final String DEFAULT = "default";
	public static final String CONTAINER = "container";

	private final List<ComponentsWithScope> scopedComponentSets = new ArrayList<>();

	@Autowired
	void buildGlobalComponentModelSet( WebCmsComponentModelService webCmsComponentModelService ) {
		registerComponentsForScope(
				new WebCmsComponentModelSet(
						null,
						( owner, componentName ) -> webCmsComponentModelService.getComponentModelByName( componentName, owner )
				), GLOBAL
		);
	}

	@Autowired
	void registerAsRequestAttribute( HttpServletRequest request ) {
		request.setAttribute( REQUEST_ATTRIBUTE, this );
	}

	/**
	 * Add a set of components to the very end of the hierarchy.
	 * If a new scope name is specified, this scope will be registered as the first scope to look in.
	 * If an existing scope name is specified, the components will be replaced but the query order of that scope will not change.
	 *
	 * @param componentModelSet to add
	 * @param scopeName         for which to register components
	 */
	public void registerComponentsForScope( WebCmsComponentModelSet componentModelSet, String scopeName ) {
		Assert.notNull( componentModelSet, "A valid component model set is required." );
		Assert.notNull( scopeName, "A scope name is required to add a set to a hierarchy." );

		ComponentsWithScope current = getForScope( scopeName );
		if ( current != null ) {
			current.components = componentModelSet;
		}
		else {
			scopedComponentSets.add( new ComponentsWithScope( scopeName, componentModelSet ) );
		}
	}

	/**
	 * @return true if the component set for that scope has been removed
	 */
	public boolean removeComponents( String scopeName ) {
		return Optional.ofNullable( getForScope( scopeName ) )
		               .map( scopedComponentSets::remove )
		               .orElse( false );
	}

	/**
	 * @return true if component set has been removed
	 */
	public boolean removeComponents( WebCmsComponentModelSet componentModelSet ) {
		return scopedComponentSets.stream()
		                          .filter( componentsWithScope -> componentsWithScope.components.equals( componentModelSet ) )
		                          .findFirst()
		                          .map( scopedComponentSets::remove )
		                          .orElse( false );
	}

	/**
	 * @param scopeName name
	 * @return components registered to that scope
	 */
	public WebCmsComponentModelSet getComponentsForScope( String scopeName ) {
		Assert.notNull( scopeName );

		ComponentsWithScope entry = getForScope( scopeName );
		return entry != null ? entry.components : null;
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
		scopedComponentSets.sort( Comparator.comparingInt( entry -> ArrayUtils.indexOf( scopeNames, entry.scopeName ) ) );
	}

	/**
	 * @return all registered scope names in their order
	 */
	public Collection<String> getScopeNames() {
		return scopedComponentSets.stream()
		                          .map( ComponentsWithScope::getScopeName )
		                          .collect( Collectors.toList() );
	}

	/**
	 * Get the first scope that will be checked when requesting a component.
	 *
	 * @return scope name or {@code null} if the hierarchy is empty
	 */
	public String getDefaultScope() {
		return scopedComponentSets.isEmpty() ? null : scopedComponentSets.get( scopedComponentSets.size() - 1 ).scopeName;
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

		for ( int i = scopedComponentSets.size() - 1; i >= 0 && component == null; i-- ) {
			component = scopedComponentSets.get( i ).components.get( componentName );
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
		String actualScope = DEFAULT.equals( scopeName ) ? getDefaultScope() : scopeName;

		for ( int i = scopedComponentSets.size() - 1; i >= 0 && component == null; i-- ) {
			ComponentsWithScope componentsWithScope = scopedComponentSets.get( i );
			if ( scopeFound || actualScope.equals( componentsWithScope.getScopeName() ) ) {
				scopeFound = true;
				component = componentsWithScope.components.get( componentName );
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
		return getForScope( scopeName ) != null;
	}

	private ComponentsWithScope getForScope( String scopeName ) {
		for ( ComponentsWithScope entry : scopedComponentSets ) {
			if ( scopeName.equals( entry.scopeName ) ) {
				return entry;
			}
		}
		return null;
	}

	@AllArgsConstructor
	private static class ComponentsWithScope
	{
		@Getter
		private final String scopeName;
		private WebCmsComponentModelSet components;
	}
}
