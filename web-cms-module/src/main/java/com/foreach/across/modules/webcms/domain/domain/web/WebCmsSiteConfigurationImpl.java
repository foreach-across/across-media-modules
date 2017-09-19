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

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.webcms.data.WebCmsDataConversionService;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainAware;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.*;

/**
 * Default implementation of {@link WebCmsSiteConfiguration}.
 * This class is deliberately left open for extension and (partial) modification.
 *
 * @author Arne Vandamme
 * @since 0.0.3
 */
@Exposed
@Component
@Scope("prototype")
@RequiredArgsConstructor
public class WebCmsSiteConfigurationImpl implements WebCmsSiteConfiguration, WebCmsDomainAware
{
	private static final TypeDescriptor HOST_NAMES_TYPE = TypeDescriptor.collection( List.class, TypeDescriptor.valueOf( String.class ) );
	private static final TypeDescriptor LOCALES_TYPE
			= TypeDescriptor.map( HashMap.class, TypeDescriptor.valueOf( String.class ), TypeDescriptor.valueOf( Locale.class ) );

	private static final String HOST_NAMES = "hostNames";
	private static final String COOKIE_DOMAIN = "cookieDomain";
	private static final String DEFAULT_LOCALE = "defaultLocale";
	private static final String SORT_INDEX = "sortIndex";
	private static final String LOCALES = "locales";
	private static final String URL_PREFIX = "urlPrefix";
	private static final String ALWAYS_PREFIX = "alwaysPrefix";

	private final WebCmsDataConversionService dataConversionService;

	private WebCmsDomain domain;

	private List<String> hostNames;
	private Map<String, Locale> locales;
	private String cookieDomain;
	private Locale defaultLocale;
	private int sortIndex;
	private String urlPrefix;
	private boolean alwaysPrefix;

	@Override
	public void setWebCmsDomain( WebCmsDomain domain ) {
		Assert.notNull( domain, "A non-null domain is required" );
		this.domain = domain;

		this.cookieDomain = getAttribute( COOKIE_DOMAIN, String.class );
		this.sortIndex = getAttribute( SORT_INDEX, int.class, 0 );
		this.defaultLocale = getAttribute( DEFAULT_LOCALE, Locale.class );
		this.hostNames = getAttribute( HOST_NAMES, HOST_NAMES_TYPE, Collections.emptyList() );
		this.locales = getAttribute( LOCALES, LOCALES_TYPE, Collections.emptyMap() );
		this.urlPrefix = getAttribute( URL_PREFIX );
		this.alwaysPrefix = getAttribute( ALWAYS_PREFIX, boolean.class, false );
	}

	@Override
	public WebCmsDomain getDomain() {
		return domain;
	}

	@Override
	public String getDomainKey() {
		return domain.getDomainKey();
	}

	@Override
	public String getName() {
		return domain.getName();
	}

	public List<String> getHostNames() {
		return hostNames;
	}

	@Override
	public String getCookieDomain() {
		return cookieDomain;
	}

	@Override
	public Locale getDefaultLocale() {
		return defaultLocale;
	}

	@Override
	public Map<String, Locale> getLocales() {
		return locales;
	}

	@Override
	public Locale getLocaleForHostName( String hostName ) {
		return locales.containsKey( hostName ) ? locales.get( hostName ) : getDefaultLocale();
	}

	@Override
	public int getSortIndex() {
		return sortIndex;
	}

	@Override
	public final String getAttribute( String attributeName ) {
		return domain.getAttribute( attributeName );
	}

	@Override
	public final <Y> Y getAttribute( String attributeName, Class<Y> attributeType ) {
		return getAttribute( attributeName, attributeType, null );
	}

	@Override
	public final <Y> Y getAttribute( String attributeName, TypeDescriptor attributeType, Y defaultValue ) {
		String attributeValue = domain.getAttribute( attributeName );
		return attributeValue != null ? readAttribute( attributeValue, attributeType ) : defaultValue;
	}

	@Override
	public final <Y> Y getAttribute( String attributeName, Class<Y> attributeType, Y defaultValue ) {
		String attributeValue = domain.getAttribute( attributeName );
		return attributeValue != null ? readAttribute( attributeValue, TypeDescriptor.valueOf( attributeType ) ) : defaultValue;
	}

	@SuppressWarnings("unchecked")
	private <Y> Y readAttribute( String attributeValue, TypeDescriptor attributeType ) {
		return (Y) dataConversionService.convert( attributeValue, attributeType );
	}

	@Override
	public final boolean hasAttribute( String attributeName ) {
		return domain.hasAttribute( attributeName );
	}

	@Override
	public String getUrlPrefix() {
		return urlPrefix;
	}

	@Override
	public boolean isAlwaysPrefix() {
		return alwaysPrefix;
	}
}
