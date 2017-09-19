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

package com.foreach.across.modules.webcms.domain.domain.web;

import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainContext;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainRepository;
import com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService;
import com.foreach.across.modules.webcms.domain.domain.config.WebCmsMultiDomainConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implementation of {@link AbstractWebCmsDomainContextFilter} that checks {@link WebCmsSiteConfiguration} metadata.
 * Every active domain that has {@link WebCmsSiteConfiguration} metadata will be taken into account.
 * The domains will be ordered, and their {@link WebCmsSiteConfiguration#getHostNames()} inspected in order.
 * The first domain to match will be set as the current domain context.
 * <p/>
 * This class can be extended for further customization.
 *
 * @author Arne Vandamme
 * @since 0.0.3
 */
@Lazy
@Component
public class WebCmsSiteConfigurationFilter extends AbstractWebCmsDomainContextFilter
{
	private WebCmsDomainRepository domainRepository;
	private WebCmsMultiDomainService multiDomainService;
	private WebCmsMultiDomainConfiguration multiDomainConfiguration;

	@Override
	protected List<DomainContextLookup> buildDomainSpecificLookups() {
		return domainRepository.findAll()
		                       .stream()
		                       .filter( WebCmsDomain::isActive )
		                       .map( domain -> multiDomainService.getMetadataForDomain( domain, Object.class ) )
		                       .filter( metadata -> metadata instanceof WebCmsSiteConfiguration )
		                       .map( WebCmsSiteConfiguration.class::cast )
		                       .sorted( Comparator.comparing( WebCmsSiteConfiguration::getSortIndex ) )
		                       .flatMap(
				                       site -> site.getHostNames()
				                                   .stream()
				                                   .map( hostName -> createDomainContextLookup( hostName, site.getDomain(), site ) )
		                       )
		                       .collect( Collectors.toList() );
	}

	/**
	 * Uses the {@link WebCmsMultiDomainConfiguration#getDefaultDomainKey()} to determine the default domain.
	 * If specified, that domain and its metadata will be used for the domain context.
	 * The active status of the domain is not taken into account.
	 * <p/>
	 * If no domain key is specified, then no-domain is used as default domain.
	 *
	 * @param domainSpecificLookups list of lookups for actual domains
	 * @return lookup record
	 */
	@Override
	protected DomainContextLookup buildDefaultDomainLookup( List<DomainContextLookup> domainSpecificLookups ) {
		String defaultDomainKey = multiDomainConfiguration.getDefaultDomainKey();
		WebCmsDomain domain = defaultDomainKey != null ? domainRepository.findOneByDomainKey( defaultDomainKey ) : WebCmsDomain.NONE;
		Object metadata = multiDomainService.getMetadataForDomain( domain, Object.class );

		return createDomainContextLookup( null, domain, metadata );
	}

	/**
	 * Create a single {@link DomainContextLookup} for a host name and domain.
	 * Note that either parameter can be {@code null}.
	 * If host name is {@code null} it usually implies the default (fallback) domain lookup record.
	 * <p/>
	 * If the metadata is of type {@link WebCmsSiteConfiguration}, the {@link Locale} to use will be resolved from it.
	 *
	 * @param hostName to match (can be {@code null})
	 * @param domain   to select (can be {@code null})
	 * @param metadata of the domain (can be {@code null}
	 * @return lookup
	 */
	@SuppressWarnings("WeakerAccess")
	protected DomainContextLookup createDomainContextLookup( String hostName, WebCmsDomain domain, Object metadata ) {
		Pattern hostPattern = null;
		Locale localeToUse = null;

		if ( hostName != null ) {
			hostPattern = compileHostPattern( hostName );
		}

		if ( metadata instanceof WebCmsSiteConfiguration ) {
			WebCmsSiteConfiguration siteConfiguration = (WebCmsSiteConfiguration) metadata;
			localeToUse = hostName != null ? siteConfiguration.getLocaleForHostName( hostName ) : siteConfiguration.getDefaultLocale();
		}

		WebCmsDomainContext domainContext = domain != null ? new WebCmsDomainContext( domain, metadata ) : WebCmsDomainContext.noDomain( metadata );
		return new DomainContextLookup( hostPattern, localeToUse, domainContext );
	}

	@Autowired
	void setDomainRepository( WebCmsDomainRepository domainRepository ) {
		this.domainRepository = domainRepository;
	}

	@Autowired
	void setMultiDomainService( WebCmsMultiDomainService multiDomainService ) {
		this.multiDomainService = multiDomainService;
	}

	@Autowired
	void setMultiDomainConfiguration( WebCmsMultiDomainConfiguration multiDomainConfiguration ) {
		this.multiDomainConfiguration = multiDomainConfiguration;
	}
}
