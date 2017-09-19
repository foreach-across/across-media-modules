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

import com.foreach.across.modules.entity.query.*;
import com.foreach.across.modules.spring.security.actions.AllowableAction;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.foreach.across.modules.entity.query.EntityQueryOps.*;

/**
 * Implements support for {@code selectedDomain()}, {@code visibleDomains()} and {@code accessibleDomains()} in EQL statements.
 * Uses the {@link WebCmsMultiDomainAdminUiService} for execution of the specific methods and will convert the result to valid EQL.
 *
 * @author Arne Vandamme
 * @see WebCmsMultiDomainAdminUiService
 * @since 0.0.3
 */
@ConditionalOnAdminUI
@Component
@Lazy
@RequiredArgsConstructor
public final class EntityQueryWebCmsDomainFunctions implements EntityQueryFunctionHandler
{
	/**
	 * Always returns the current domain, bound to the {@link WebCmsDomainContextHolder}.
	 * This method will return a single domain only or none.
	 * <p/>
	 * <code>domain = currentDomain()</code> will be translated to either <code>domain IS EMPTY</code>
	 * or <code>domain = DOMAIN_ID</code>
	 */
	public static final String SELECTED_DOMAIN = "selectedDomain";

	/**
	 * Returns the domains from which entities are currently selectable.
	 * Usually this is the current domain, however if the current multi-domain configuration allows
	 * no-domain, these will be included as well.
	 * <p/>
	 * In short, the query <code>domain = selectableDomains()</code> might be translated to
	 * <code>domain = currentDomain()</code> or <code>domain = currentDomain() or domain IS EMPTY</code>
	 * depending on the multi-domain configuration.
	 */
	public static final String VISIBLE_DOMAINS = "visibleDomains";

	/**
	 * Returns all domains that the current principal can see.  Optionally takes one or more string parameter that are
	 * {@link com.foreach.across.modules.spring.security.actions.AllowableAction} that the principal should
	 * have for that domain.  Note that in case of multiple parameters, it is an OR construct: as soon as the user has
	 * any of those actions allowed the domain will be included.
	 * <p/>
	 * If no-domain is allowed, will check if the user has the permissions to manage no-domain.
	 */
	public static final String ACCESSIBLE_DOMAINS = "accessibleDomains";

	private final static EntityQueryConditionTranslator DOMAIN_CONDITION_TRANSLATOR = condition -> {
		boolean translatable = EntityQueryOps.EQ.equals( condition.getOperand() ) || IN.equals( condition.getOperand() );
		boolean negation = EntityQueryOps.NEQ.equals( condition.getOperand() ) || NOT_IN.equals( condition.getOperand() );

		if ( translatable || negation ) {
			List<Object> domains = Stream.of( condition.getArguments() )
			                             .flatMap( argument -> argument instanceof Object[] ? Stream.of( (Object[]) argument ) : Stream.of( argument ) )
			                             .collect( Collectors.toList() );
			boolean nullIncluded = domains.remove( null );

			EntityQueryCondition domainCondition = null;
			EntityQueryCondition nullCondition = null;

			if ( domains.size() > 0 ) {
				if ( domains.size() > 1 ) {
					domainCondition = new EntityQueryCondition( condition.getProperty(), negation ? NOT_IN : IN, domains.toArray() );
				}
				else {
					domainCondition = new EntityQueryCondition( condition.getProperty(), negation ? NEQ : EQ, domains.toArray() );
				}
			}

			if ( nullIncluded ) {
				nullCondition = new EntityQueryCondition( condition.getProperty(), negation ? IS_NOT_NULL : IS_NULL );
			}

			if ( nullCondition != null && domainCondition != null ) {
				return negation ? EntityQuery.and( domainCondition, nullCondition ) : EntityQuery.or( domainCondition, nullCondition );
			}
			else if ( nullCondition != null ) {
				return nullCondition;
			}

			return domainCondition;
		}

		return condition;
	};

	private final WebCmsMultiDomainAdminUiService multiDomainAdminUiService;

	/**
	 * Create a condition translator that supports the domain-related functions and will expand single condition
	 * (eg. <code>domain in accessibleDomains()</code>) to multiple conditions (eg. <code>(domain in (1,2,3) or domain IS EMPTY)</code>.
	 *
	 * @return condition translator
	 */
	public static EntityQueryConditionTranslator conditionTranslator() {
		return DOMAIN_CONDITION_TRANSLATOR;
	}

	@Override
	public boolean accepts( String functionName, TypeDescriptor desiredType ) {
		return SELECTED_DOMAIN.equals( functionName ) || VISIBLE_DOMAINS.equals( functionName ) || ACCESSIBLE_DOMAINS.equals( functionName );
	}

	@Override
	public Object apply( String functionName, EQType[] arguments, TypeDescriptor desiredType, EQTypeConverter argumentConverter ) {
		return retrieveDomains( functionName, arguments, argumentConverter )
				.stream()
				.map( domain -> convertDomainToType( domain, desiredType, argumentConverter ) )
				.collect( Collectors.toList() )
				.toArray();
	}

	private Object convertDomainToType( WebCmsDomain domain, TypeDescriptor desiredType, EQTypeConverter argumentConverter ) {
		Class<?> targetType = desiredType.getObjectType();

		if ( domain != null ) {
			if ( WebCmsDomain.class.equals( targetType ) ) {
				return domain;
			}
			if ( String.class.equals( targetType ) ) {
				return domain.getObjectId();
			}
			if ( Long.class.equals( targetType ) || long.class.equals( targetType ) ) {
				return domain.getId();
			}

			return argumentConverter.convert( desiredType, domain );
		}

		return null;
	}

	private Collection<WebCmsDomain> retrieveDomains( String functionName, EQType[] arguments, EQTypeConverter argumentConverter ) {
		switch ( functionName ) {
			case VISIBLE_DOMAINS:
				return multiDomainAdminUiService.getVisibleDomains();
			case ACCESSIBLE_DOMAINS:
				AllowableAction[] actions = new AllowableAction[] { AllowableAction.READ };
				if ( arguments.length > 0 ) {
					actions = Stream.of( arguments )
					                .map( a -> new AllowableAction( (String) argumentConverter.convert( TypeDescriptor.valueOf( String.class ), a ) ) )
					                .collect( Collectors.toList() )
					                .toArray( new AllowableAction[arguments.length] );
				}
				return multiDomainAdminUiService.getAccessibleDomains( actions );
			case SELECTED_DOMAIN:
				return Collections.singleton( multiDomainAdminUiService.getSelectedDomain() );
		}

		return Collections.emptyList();
	}
}
