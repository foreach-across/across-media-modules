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

package com.foreach.across.modules.webcms.domain.domain;

import com.foreach.across.modules.entity.registry.EntityConfiguration;
import com.foreach.across.modules.entity.registry.EntityRegistry;
import com.foreach.across.modules.spring.security.actions.AllowableAction;
import com.foreach.across.modules.spring.security.actions.AllowableActions;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.domain.config.WebCmsMultiDomainConfiguration;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Helper service that retrieves the domains for the current user,
 * taking into account the currently bound context.
 *
 * @author Arne Vandamme
 * @since 0.0.3
 */
@ConditionalOnAdminUI
@Service
@RequiredArgsConstructor
public class WebCmsMultiDomainAdminUiService
{
	private final EntityRegistry entityRegistry;
	private final WebCmsMultiDomainConfiguration multiDomainConfiguration;
	private final WebCmsDomainRepository domainRepository;

	/**
	 * Always returns the current domain, bound to the {@link WebCmsDomainContextHolder}.
	 * This method will return a single domain only or none.
	 *
	 * @return domain or null
	 */
	public WebCmsDomain getSelectedDomain() {
		WebCmsDomainContext domainContext = WebCmsDomainContextHolder.getWebCmsDomainContext();
		return domainContext != null ? domainContext.getDomain() : null;
	}

	/**
	 * Returns the domains from which entities are currently selectable (visible domains).
	 * Usually this is the current domain, however if the current multi-domain configuration allows
	 * no-domain this collection will also contain {@code null}.
	 *
	 * @return collection of domains - possibly including a {@code null} value
	 */
	public Collection<WebCmsDomain> getVisibleDomains() {
		List<WebCmsDomain> domains = new ArrayList<>( 2 );
		WebCmsDomain selectedDomain = getSelectedDomain();

		if ( selectedDomain != null ) {
			domains.add( selectedDomain );
		}

		if ( multiDomainConfiguration.isNoDomainAllowed() ) {
			domains.add( null );
		}

		return domains;
	}

	/**
	 * Returns all domains that the current principal can select.  Optionally takes one or more
	 * {@link com.foreach.across.modules.spring.security.actions.AllowableAction} parameters that the principal should
	 * have for that domain.  Note that in case of multiple parameters, it is an OR construct: as soon as the user has
	 * any of those actions allowed the domain will be included.
	 * <p/>
	 * If no {@link AllowableAction} is specified, it will default to {@link AllowableAction#READ}.
	 * <p/>
	 * If no-domain is allowed, will check if the user has the permissions to manage the {@link WebCmsDomain} type itself.
	 */
	public Collection<WebCmsDomain> getAccessibleDomains( AllowableAction... actions ) {
		EntityConfiguration<WebCmsDomain> configuration = entityRegistry.getEntityConfiguration( WebCmsDomain.class );
		AllowableAction[] actionsToCheck = actions.length > 0 ? actions : new AllowableAction[] { AllowableAction.READ };
		List<WebCmsDomain> domains = new ArrayList<>();

		if ( multiDomainConfiguration.isNoDomainAllowed() && containsAny( configuration.getAllowableActions(), actionsToCheck ) ) {
			domains.add( null );
		}

		domainRepository.findAll()
		                .stream()
		                .filter( d -> containsAny( configuration.getAllowableActions( d ), actionsToCheck ) )
		                .forEach( domains::add );

		return domains;
	}

	private boolean containsAny( AllowableActions actions, AllowableAction... action ) {
		for ( AllowableAction a : action ) {
			if ( actions.contains( a ) ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Create a domain binding predicate for a QueryDSL path of a {@link WebCmsDomain} property.
	 * This will check the multi-domain configuration and create a domain predicate if necessary.
	 * This method can return a {@code null} value but should be safe to use with QueryDSL expression utils.
	 *
	 * @param domain expression (property linked to the {@link WebCmsDomain})
	 * @return predicate or {@code null}
	 */
	public Predicate visibleDomainsPredicate( QWebCmsDomain domain ) {
		Predicate predicate = null;

		if ( !multiDomainConfiguration.isDisabled() ) {
			WebCmsDomain currentDomain = getSelectedDomain();

			if ( !WebCmsDomain.isNoDomain( currentDomain ) ) {
				predicate = domain.eq( currentDomain );
			}

			if ( multiDomainConfiguration.isNoDomainAllowed() ) {
				predicate = ExpressionUtils.or( predicate, domain.isNull() );
			}
		}

		return predicate;
	}

	/**
	 * Automatically append the domain binding predicate to an existing QueryDSL predicate.
	 *
	 * @param predicate to append the domain predicate to
	 * @param domain    representing the domain property for the expression object
	 * @return original or appended predicate
	 */
	public Predicate applyVisibleDomainsPredicate( Predicate predicate, QWebCmsDomain domain ) {
		return ExpressionUtils.and( predicate, visibleDomainsPredicate( domain ) );
	}
}
