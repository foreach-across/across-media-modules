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
import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents an ordered hierarchy of {@link WebCmsComponentModelSet}s that have a scope name.
 * Components can be looked up by name in the hierarchy in which case (depending on the parameter)
 * all registered sets will be queried in reverse order until a result is returned.
 * <p/>
 * The hierarchy will always contain an initial scope name {@link #GLOBAL} and {@link #DOMAIN}.
 * Both scopes will fetch a component from the repository only when it is requested.
 * If no current domain is active, the {@link #DOMAIN} scope will be an alias to {@link #GLOBAL}.
 * Otherwise the {@link #DOMAIN} scope will contain all components shared across that domain.
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
	public static final String DOMAIN = "domain";
	public static final String ASSET = "asset";

	public static final String CONTAINER = "container";

	private final List<ComponentsWithScope> scopedComponentSets = new ArrayList<>();

	@Autowired
	void buildGlobalComponentModelSet( WebCmsComponentModelService webCmsComponentModelService, WebCmsMultiDomainService multiDomainService ) {
		registerComponentsForScope(
				new WebCmsComponentModelSet(
						null,
						WebCmsDomain.NONE,
						( owner, componentName ) -> webCmsComponentModelService.getComponentModelByNameAndDomain( componentName, owner, WebCmsDomain.NONE )
				), GLOBAL
		);

		WebCmsDomain currentDomain = multiDomainService.getCurrentDomainForType( WebCmsComponent.class );

		if ( currentDomain != WebCmsDomain.NONE ) {
			registerComponentsForScope(
					new WebCmsComponentModelSet(
							null,
							currentDomain,
							( owner, componentName ) -> webCmsComponentModelService.getComponentModelByNameAndDomain( componentName, owner, currentDomain )
					),
					DOMAIN
			);
		}
		else {
			registerAliasForScope( DOMAIN, GLOBAL );
		}
	}

	@Autowired
	void registerAsRequestAttribute( HttpServletRequest request ) {
		request.setAttribute( REQUEST_ATTRIBUTE, this );
	}

	/**
	 * Add a set of components to the very end of the hierarchy.
	 * If a new scope name is specified, this scope will be registered as the first scope to look in.
	 * If an existing scope name is specified, the components will be replaced but the query order of that scope will not change.
	 * <p/>
	 * If an alias is being replaced by a separate set, it will be inserted right before the scope it was
	 * originally an alias for (so it will be queried earlier).
	 *
	 * @param componentModelSet to add
	 * @param scopeName         for which to register components
	 */
	public void registerComponentsForScope( WebCmsComponentModelSet componentModelSet, String scopeName ) {
		Assert.notNull( componentModelSet, "A valid component model set is required." );
		Assert.notNull( scopeName, "A scope name is required to add a set to a hierarchy." );

		ComponentsWithScope current = getForScope( scopeName );
		if ( current != null ) {
			if ( scopeName.equals( current.getScopeName() ) ) {
				current.components = componentModelSet;
			}
			else {
				current.removeAlias( scopeName );
				scopedComponentSets.add( scopedComponentSets.indexOf( current ) + 1, new ComponentsWithScope( scopeName, componentModelSet ) );
			}
		}
		else {
			scopedComponentSets.add( new ComponentsWithScope( scopeName, componentModelSet ) );
		}
	}

	/**
	 * Removes the components for that scope.  If the scope is an alias for another scope,
	 * only the alias will be removed but the actual components will still be available under
	 * the original scope name.
	 * <p/>
	 * If the scope name is not an alias, the entire scope - including itse aliases - will be removed.
	 *
	 * @return true if the component set for that scope has been removed
	 */
	public boolean removeComponents( String scopeName ) {
		return Optional.ofNullable( getForScope( scopeName ) )
		               .map( scope -> {
			               if ( scopeName.equals( scope.scopeName ) ) {
				               return scopedComponentSets.remove( scope );
			               }
			               else {
				               return scope.removeAlias( scopeName );
			               }
		               } )
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
		Assert.notNull( scopeName, "scope name is required" );

		ComponentsWithScope entry = getForScope( scopeName );
		return entry != null ? entry.components : null;
	}

	/**
	 * Re-order all scopes.  All registered scope names must be mentioned or an exception will be thrown.
	 * Any unknown scope name will be ignored.
	 * <p/>
	 * Scope names are defined left-to-right but scopes will be traversed right-to-left when looking for components.
	 * <p/>
	 * If the names contains one or more aliases, the earliest occurrence of the entire set will be used.
	 *
	 * @param scopeNames in order
	 */
	public void setScopeOrder( String... scopeNames ) {
		List<String> nonAliases = Stream.of( scopeNames )
		                                .map( this::getForScope )
		                                .map( ComponentsWithScope::getScopeName )
		                                .collect( Collectors.toList() );

		scopedComponentSets.sort( Comparator.comparingInt( entry -> nonAliases.indexOf( entry.scopeName ) ) );
	}

	/**
	 * @return all registered scope names in their order - this will not show aliases
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
	 * Get the component by name from the specific scope.
	 * Will not search any other (parent) scopes.
	 *
	 * @param componentName name of the component
	 * @param scopeName     name of the scope to start with
	 * @return component or null if not found
	 */
	public WebCmsComponentModel getFromScope( String componentName, String scopeName ) {
		return getFromScope( componentName, scopeName, false );
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
		Assert.notNull( scopeName, "scope name is required" );
		WebCmsComponentModel component = null;

		boolean scopeFound = false;
		String actualScope = DEFAULT.equals( scopeName ) ? getDefaultScope() : scopeName;

		for ( int i = scopedComponentSets.size() - 1; i >= 0 && component == null; i-- ) {
			ComponentsWithScope componentsWithScope = scopedComponentSets.get( i );
			if ( scopeFound || componentsWithScope.isScope( actualScope ) ) {
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
	 * Add an alias for another scope. The target scope must exist.
	 * Any other set already registered under that alias will be removed.
	 *
	 * @param alias           to add
	 * @param targetScopeName the alias is for
	 */
	public void registerAliasForScope( String alias, String targetScopeName ) {
		ComponentsWithScope target = getForScope( targetScopeName );
		Assert.notNull( target, "No components registered for target scope: '" + targetScopeName + "'" );

		removeComponents( alias );
		target.addAlias( alias );
	}

	/**
	 * @return true if components scope is registered
	 */
	public boolean containsScope( String scopeName ) {
		return getForScope( scopeName ) != null;
	}

	private ComponentsWithScope getForScope( String scopeName ) {
		for ( ComponentsWithScope entry : scopedComponentSets ) {
			if ( entry.isScope( scopeName ) ) {
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
		private final Collection<String> aliases = new ArrayList<>( 2 );
		private WebCmsComponentModelSet components;

		boolean isScope( String requested ) {
			return scopeName.equals( requested ) || aliases.contains( requested );
		}

		boolean removeAlias( String alias ) {
			return aliases.remove( alias );
		}

		void addAlias( String alias ) {
			aliases.add( alias );
		}
	}
}
