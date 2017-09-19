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

package com.foreach.across.modules.webcms.domain.domain.support;

import com.foreach.across.modules.entity.actions.EntityConfigurationAllowableActionsBuilder;
import com.foreach.across.modules.entity.registry.EntityConfiguration;
import com.foreach.across.modules.spring.security.actions.AllowableAction;
import com.foreach.across.modules.spring.security.actions.AllowableActions;
import com.foreach.across.modules.webcms.WebCmsEntityAttributes;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainBound;
import com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.Collections;
import java.util.Iterator;

/**
 * Wraps around a target {@link EntityConfigurationAllowableActionsBuilder} and checks that domain bound objects
 * can only be managed if a specific domain is selected.  Domain bound entities can only
 * be managed if a specific domain is selected, unless they also allow no-domain.
 * <p/>
 * A specific domain bound object can only be managed on the actual domain it belongs to.
 * <p/>
 * You can overrule the default behaviour by setting a boolean {@link WebCmsEntityAttributes#ALLOW_PER_DOMAIN} on the
 * {@link EntityConfiguration}.  If the value is {@code true}, a non-domain-bound entity will still be allowed on specific domains.
 * If the value is {@code false}, a domain-bound entity will not be allowed on specific domains.
 *
 * @author Arne Vandamme
 * @since 0.0.3
 */
@RequiredArgsConstructor
public class CurrentDomainAwareAllowableActionsBuilder implements EntityConfigurationAllowableActionsBuilder
{
	static final AllowableActions NOTHING_ALLOWED = new AllowableActions()
	{
		@Override
		public boolean contains( AllowableAction action ) {
			return false;
		}

		@Override
		public Iterator<AllowableAction> iterator() {
			return Collections.emptyIterator();
		}
	};

	private final WebCmsMultiDomainService multiDomainService;
	private final EntityConfigurationAllowableActionsBuilder targetBuilder;

	@Override
	public AllowableActions getAllowableActions( EntityConfiguration<?> entityConfiguration ) {
		WebCmsDomain currentDomain = multiDomainService.getCurrentDomain();
		Boolean allowedPerDomain = entityConfiguration.getAttribute( WebCmsEntityAttributes.ALLOW_PER_DOMAIN, Boolean.class );

		if ( multiDomainService.isDomainBound( entityConfiguration.getEntityType() ) ) {
			if ( ( WebCmsDomain.isNoDomain( currentDomain ) && !multiDomainService.isNoDomainAllowed( entityConfiguration.getEntityType() ) )
					|| Boolean.FALSE.equals( allowedPerDomain ) ) {
				return NOTHING_ALLOWED;
			}
		}
		else if ( !WebCmsDomain.isNoDomain( currentDomain ) && !Boolean.TRUE.equals( allowedPerDomain ) ) {
			return NOTHING_ALLOWED;
		}

		return targetBuilder.getAllowableActions( entityConfiguration );
	}

	@Override
	public <V> AllowableActions getAllowableActions( EntityConfiguration<V> entityConfiguration, V entity ) {
		WebCmsDomain currentDomain = multiDomainService.getCurrentDomain();
		WebCmsDomain entityDomain = entityDomain( entityConfiguration, entity );
		Boolean allowedPerDomain = entityConfiguration.getAttribute( WebCmsEntityAttributes.ALLOW_PER_DOMAIN, Boolean.class );

		if ( WebCmsDomain.isNoDomain( entityDomain ) ) {
			if ( currentDomain != WebCmsDomain.NONE && !Boolean.TRUE.equals( allowedPerDomain ) ) {
				return NOTHING_ALLOWED;
			}
		}
		else if ( !entityDomain.equals( currentDomain ) ) {
			return NOTHING_ALLOWED;
		}

		return targetBuilder.getAllowableActions( entityConfiguration, entity );
	}

	private WebCmsDomain entityDomain( EntityConfiguration entityConfiguration, Object entity ) {
		String domainProperty = entityConfiguration.getAttribute( WebCmsEntityAttributes.DOMAIN_PROPERTY, String.class );

		if ( domainProperty != null ) {
			BeanWrapper beanWrapper = new BeanWrapperImpl( entity );
			Object domain = beanWrapper.getPropertyValue( domainProperty );

			if ( domain == null || domain instanceof WebCmsDomain ) {
				return (WebCmsDomain) domain;
			}
		}

		return entity instanceof WebCmsDomainBound ? ( (WebCmsDomainBound) entity ).getDomain() : WebCmsDomain.NONE;
	}
}
