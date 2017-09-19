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

package com.foreach.across.modules.webcms.domain.domain.config;

import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainAware;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainBound;
import com.foreach.across.modules.webcms.domain.domain.web.AbstractWebCmsDomainContextFilter;
import com.foreach.across.modules.webcms.domain.domain.web.WebCmsSiteConfigurationFilter;
import com.foreach.across.modules.webcms.domain.domain.web.WebCmsSiteConfigurationImpl;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenuItem;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifier;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import lombok.*;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuration bean for easy set-up of multi-domain configurations.
 * An application has single multi-domain configuration.
 * <p/>
 * The {@link WebCmsMultiDomainConfiguration} strongly determines the behaviour of the
 * WebCmsModule regarding its objects.  It will automatically configure the administration UI
 * as well as apply the multi-domain rules during saving of objects (including during imports).
 * <p/>
 * A configuration can only be created using the {@link WebCmsMultiDomainConfigurationBuilder}
 * and not be modified afterwards.  See the factory methods for default configurations:
 * <ul>
 * <li>{@link #disabled()}</li>
 * <li>{@link #managementPerDomain()}</li>
 * <li>{@link #managementPerEntity()}</li>
 * </ul>
 *
 * @author Arne Vandamme
 * @since 0.0.3
 */
@SuppressWarnings("WeakerAccess")
@EqualsAndHashCode
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WebCmsMultiDomainConfiguration
{
	@Getter
	private final Set<Class<?>> domainBoundTypes;

	@Getter
	private final Set<Class<?>> domainIgnoredTypes;

	/**
	 * Set of domain bound types where a configured domain is optional.
	 * By default a domain bound type requires a domain.
	 */
	@Getter
	private final Set<Class<?>> noDomainAllowedTypes;

	/**
	 * If specified, indicates the default domain that should be selected.
	 * By default this means that the {@link com.foreach.across.modules.webcms.domain.domain.web.WebCmsSiteConfigurationFilter} will select
	 * this domain if no other has matched.
	 */
	private String defaultDomainKey;

	/**
	 * Is a no-domain configuration level possible?
	 * If set to {@code false}, a domain-bound entity will always require a specific domain to be set.
	 * <p/>
	 * When set to {@code false}, calls to {@link #isNoDomainAllowed(Class)} will always return {@code false}
	 * for domain-bound entities.
	 */
	@Builder.Default
	private boolean noDomainAllowed = true;

	/**
	 * If disabled, no multi-domain configuration is active.
	 * If a default domain is specified, it will still be applied.
	 */
	@Builder.Default
	private boolean disabled = true;

	/**
	 * Can the domain be selected per entity or not.  If {@code false} (the default),
	 * a domain will be selected on a top-level and the management UI will scope itself
	 * only to objects of that domain.
	 * <p/>
	 * Only relevant if the administration UI is active.
	 */
	@Builder.Default
	private boolean domainSelectablePerEntity = false;

	/**
	 * The actual metadata implementation that should be used for the domains.
	 * If specified, metadata instances will be created as prototype beans and attached to the domain context
	 * for their corresponding domain.  Metadata must implement the {@link WebCmsDomainAware} interface.
	 * <p/>
	 * If {@code null}, no metadata will be loaded for domains.
	 */
	@Builder.Default
	private Class<? extends WebCmsDomainAware> metadataClass = WebCmsSiteConfigurationImpl.class;

	/**
	 * Name of the bean that should be retrieved if the no-domain context is loaded.
	 * Because no-domain does not correspond with a {@link com.foreach.across.modules.webcms.domain.domain.WebCmsDomain} instance,
	 * metadata for no-domain will only be loaded if a bean name is specified.
	 * <p/>
	 * The actual bean returned can be a singleton and is not required to implement {@link WebCmsDomainAware}.
	 * Even if it would, {@link WebCmsDomainAware#setWebCmsDomain(WebCmsDomain)} would never be called.
	 */
	private String noDomainMetadataBeanName;

	/**
	 * Implementation of a {@link AbstractWebCmsDomainContextFilter} that is responsible for determining the
	 * {@link com.foreach.across.modules.webcms.domain.domain.WebCmsDomainContext} for incoming web requests.
	 * If a class is set, the bean will be retrieved and configured as a servlet filter for all requests.
	 */
	@Builder.Default
	private Class<? extends AbstractWebCmsDomainContextFilter> domainContextFilterClass = WebCmsSiteConfigurationFilter.class;

	/**
	 * @return a configuration for domain-less management (default)
	 */
	public static WebCmsMultiDomainConfigurationBuilder disabled() {
		return builder();
	}

	/**
	 * Create a multi-domain configuration where any {@link WebCmsDomainBound} object is domain bound
	 * by default, with the exception of any {@link WebCmsTypeSpecifier}.
	 * <p/>
	 * Domain is determined on a global level, entities are managed within the context of a single domain.
	 *
	 * @return multi-domain configuration
	 */
	public static WebCmsMultiDomainConfigurationBuilder managementPerDomain() {
		return builder().disabled( false )
		                .domainSelectablePerEntity( false )
		                .domainBoundTypes( WebCmsDomainBound.class, WebCmsUrl.class, WebCmsMenuItem.class )
		                .domainIgnoredTypes( WebCmsTypeSpecifier.class )
		                .noDomainAllowedTypes( WebCmsComponent.class );
	}

	/**
	 * Create a multi-domain configuration where any {@link WebCmsDomainBound} object is domain bound
	 * by default, with the exception of any {@link WebCmsTypeSpecifier}.
	 * <p/>
	 * Domain is determined on the individual entity level.
	 *
	 * @return multi-domain configuration
	 */
	public static WebCmsMultiDomainConfigurationBuilder managementPerEntity() {
		return builder().disabled( false )
		                .domainSelectablePerEntity( true )
		                .domainBoundTypes( WebCmsDomainBound.class, WebCmsUrl.class, WebCmsMenuItem.class )
		                .domainIgnoredTypes( WebCmsTypeSpecifier.class )
		                .noDomainAllowedTypes( WebCmsComponent.class );
	}

	/**
	 * @return the domain context filter class that should be instantiated
	 */
	public Class<? extends AbstractWebCmsDomainContextFilter> getDomainContextFilterClass() {
		return isDisabled() ? null : domainContextFilterClass;
	}

	/**
	 * Check if a type is domain bound.  This requires multi-domain to be activated for that type or one of its parent types,
	 * and the class not to have been added to the domain ignored list using {@link #domainIgnoredTypes}.
	 * <p/>
	 * If multi-domain configuration is disabled, this will return {@code false} for any type.
	 *
	 * @param entityType to check if domain bound
	 * @return true if domain bound
	 */
	public boolean isDomainBound( Class<?> entityType ) {
		Assert.notNull( entityType );

		if ( isDisabled() ) {
			return false;
		}
		// first check for exact type match
		for ( Class<?> ignored : domainIgnoredTypes ) {
			if ( ignored.equals( entityType ) ) {
				return false;
			}
		}
		for ( Class<?> bound : domainBoundTypes ) {
			if ( bound.equals( entityType ) ) {
				return true;
			}
		}
		// undecided - check for interfaces and parent classes
		for ( Class<?> ignored : domainIgnoredTypes ) {
			if ( ignored.isAssignableFrom( entityType ) ) {
				return false;
			}
		}
		for ( Class<?> bound : domainBoundTypes ) {
			if ( bound.isAssignableFrom( entityType ) ) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Check if a domain bound type has the actual domain demarcated as optional.
	 * This allows a "no-domain" to be attached to the type.
	 * <p/>
	 * This call will always return {@code true} if multi-domain configuration is disabled,
	 * or if a non-domain bound object is checked.
	 *
	 * @param entityType to check for no-domain allowed
	 * @return true is domain bound and no domain is allowed
	 */
	public boolean isNoDomainAllowed( Class<?> entityType ) {
		Assert.notNull( entityType );

		if ( isDisabled() || !isDomainBound( entityType ) ) {
			return true;
		}

		if ( isNoDomainAllowed() ) {
			for ( Class<?> noDomainAllowed : noDomainAllowedTypes ) {
				if ( noDomainAllowed.isAssignableFrom( entityType ) ) {
					return true;
				}
			}
		}

		return false;
	}

	public static class WebCmsMultiDomainConfigurationBuilder
	{
		public WebCmsMultiDomainConfigurationBuilder() {
			domainBoundTypes = new HashSet<>();
			domainIgnoredTypes = new HashSet<>();
			noDomainAllowedTypes = new HashSet<>();
		}

		public WebCmsMultiDomainConfigurationBuilder domainBoundTypes( Class<?>... entityTypes ) {
			return domainBoundTypes( Arrays.asList( entityTypes ) );
		}

		public WebCmsMultiDomainConfigurationBuilder domainBoundTypes( Collection<Class<?>> entityTypes ) {
			domainBoundTypes.addAll( entityTypes );
			domainIgnoredTypes.removeAll( entityTypes );
			return this;
		}

		public WebCmsMultiDomainConfigurationBuilder clearDomainBoundTypes() {
			domainBoundTypes.clear();
			return this;
		}

		public WebCmsMultiDomainConfigurationBuilder domainIgnoredTypes( Class<?>... entityTypes ) {
			return domainIgnoredTypes( Arrays.asList( entityTypes ) );
		}

		public WebCmsMultiDomainConfigurationBuilder domainIgnoredTypes( Collection<Class<?>> entityTypes ) {
			domainIgnoredTypes.addAll( entityTypes );
			domainBoundTypes.removeAll( entityTypes );
			return this;
		}

		public WebCmsMultiDomainConfigurationBuilder clearDomainIgnoredTypes() {
			domainIgnoredTypes.clear();
			return this;
		}

		public WebCmsMultiDomainConfigurationBuilder noDomainAllowedTypes( Class<?>... entityTypes ) {
			return noDomainAllowedTypes( Arrays.asList( entityTypes ) );
		}

		public WebCmsMultiDomainConfigurationBuilder noDomainAllowedTypes( Collection<Class<?>> entityTypes ) {
			noDomainAllowedTypes.addAll( entityTypes );
			return this;
		}

		public WebCmsMultiDomainConfigurationBuilder clearNoDomainAllowedTypes() {
			noDomainAllowedTypes.clear();
			return this;
		}
	}
}
