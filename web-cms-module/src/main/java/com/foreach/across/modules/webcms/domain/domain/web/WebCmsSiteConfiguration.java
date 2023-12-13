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
import org.springframework.core.convert.TypeDescriptor;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This is the default domain metadata interface for a {@link com.foreach.across.modules.webcms.domain.domain.WebCmsDomain}
 * representing a website.  This is a common case where the WebCmsModule is used for multi-site management.
 *
 * @author Arne Vandamme
 * @see com.foreach.across.modules.webcms.domain.domain.config.WebCmsMultiDomainConfiguration
 * @see WebCmsSiteConfigurationImpl
 * @since 0.0.3
 */
public interface WebCmsSiteConfiguration extends WebCmsDomainUrlConfigurer
{
	/**
	 * @return the domain this configuration is attached to
	 */
	WebCmsDomain getDomain();

	/**
	 * @return unique key of the domain
	 */
	String getDomainKey();

	/**
	 * @return name of the domain
	 */
	String getName();

	/**
	 * List of the hostnames this domain matches.
	 * A hostname can contain wildcards (*) and optionally a port.
	 * If no port is specified, it will match regardless of port.
	 *
	 * @return map of hostnames this domain matches
	 */
	List<String> getHostNames();

	/**
	 * @return domain cookie should be attached to
	 */
	String getCookieDomain();

	/**
	 * @return optional default locale (if none is specified on the dns)
	 */
	Locale getDefaultLocale();

	/**
	 * @return map containing default locale per dns matcher
	 */
	Map<String, Locale> getLocales();

	/**
	 * Get the configured locale for a particular configured host name.
	 * Can return {@code null} if no locale is configured and no default locale is set.
	 *
	 * @param hostName to get the locale for
	 * @return locale or null
	 */
	Locale getLocaleForHostName( String hostName );

	/**
	 * @return sort index of this domain
	 */
	int getSortIndex();

	/**
	 * Get the value of the attribute identified by {@code attributeName}.
	 * Return {@code null} if the attribute doesn't exist.
	 *
	 * @param attributeName the unique attribute key
	 * @return the current value of the attribute, if any
	 */
	String getAttribute( String attributeName );

	/**
	 * Get the value of the attribute identified by {@code attributeName}.
	 * The value will be converted to the {@code attributeType} specified.
	 * Return {@code null} if the attribute doesn't exist.
	 *
	 * @param attributeName the unique attribute key
	 * @param attributeType type the attribute should have
	 * @return the current value of the attribute, if any
	 */
	<Y> Y getAttribute( String attributeName, Class<Y> attributeType );

	/**
	 * Get the value of the attribute identified by {@code attributeName}.
	 * The value will be converted to the {@code attributeType} specified.
	 * Return {@code null} if the attribute doesn't exist.
	 *
	 * @param attributeName the unique attribute key
	 * @param attributeType type the attribute should have
	 * @param defaultValue  if attribute not present
	 * @return the current value of the attribute, if any
	 */
	<Y> Y getAttribute( String attributeName, Class<Y> attributeType, Y defaultValue );

	/**
	 * Get the value of the attribute identified by {@code attributeName}.
	 * The value will be converted to the {@code attributeType} specified.
	 * Return {@code null} if the attribute doesn't exist.
	 * <p/>
	 * This variant uses the {@link TypeDescriptor} to define the attribute type,
	 * allowing for the use of parametrized types.
	 *
	 * @param attributeName the unique attribute key
	 * @param attributeType type the attribute should have
	 * @param defaultValue  if attribute not present
	 * @return the current value of the attribute, if any
	 */
	<Y> Y getAttribute( String attributeName, TypeDescriptor attributeType, Y defaultValue );

	/**
	 * @param attributeName to check for
	 * @return true if the domain has that attribute
	 */
	boolean hasAttribute( String attributeName );
}
