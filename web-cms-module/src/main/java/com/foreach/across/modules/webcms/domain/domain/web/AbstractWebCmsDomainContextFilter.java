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

import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainContext;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainContextHolder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Filter that attempts to set the current {@link com.foreach.across.modules.webcms.domain.domain.WebCmsDomainContext}
 * based on the hostname of the current request.  Builds a list of {@link DomainContextLookup} records and will use the
 * first one that matches for the current hostname. If no domain can be matched on hostname, a default domain will be assigned instead.
 * <p/>
 * A {@link DomainContextLookup} record can optionally specify a {@link Locale} that should be set for the selected hostname match.
 * <p/>
 * For performance reasons, this base class caches its lookup records.  It is strongly advised that domain metadata
 * can be cached eternally as long as the domain itself is not modified.
 * <p/>
 * Extend this class to create your own domain selection logic.
 *
 * @author Arne Vandamme
 * @see WebCmsSiteConfigurationFilter
 * @since 0.0.3
 */
public abstract class AbstractWebCmsDomainContextFilter extends OncePerRequestFilter
{
	public static final String FILTER_NAME = "domainContextFilter";

	@Override
	protected final void doFilterInternal( HttpServletRequest request,
	                                       HttpServletResponse response,
	                                       FilterChain filterChain ) throws ServletException, IOException {
		boolean localeSet = false;

		try {
			UriComponents uriComponents = UriComponentsBuilder.fromHttpRequest( new ServletServerHttpRequest( request ) ).build();

			DomainContextLookup matchingLookup = retrieveMatchingLookup( uriComponents );

			if ( matchingLookup != null ) {
				WebCmsDomainContextHolder.setWebCmsDomainContext( matchingLookup.getDomainContext() );

				if ( matchingLookup.getLocale() != null ) {
					LocaleContextHolder.setLocale( matchingLookup.getLocale() );
					request.setAttribute( WebCmsDomainLocaleContextResolver.LOCALE_ATTRIBUTE, matchingLookup.getLocale() );
					localeSet = true;
				}
			}

			filterChain.doFilter( request, response );
		}
		finally {
			WebCmsDomainContextHolder.clearWebCmsDomainContext();
			if ( localeSet ) {
				LocaleContextHolder.resetLocaleContext();
			}
		}
	}

	private DomainContextLookup retrieveMatchingLookup( UriComponents uriComponents ) {
		String hostNameAndPort = uriComponents.getHost() + ( uriComponents.getPort() > 0 ? ":" + uriComponents.getPort() : "" );

		List<DomainContextLookup> lookups = retrieveLookups();

		return lookups.stream()
		              .filter( lookup -> lookup.matches( hostNameAndPort ) )
		              .findFirst()
		              .orElseGet( () -> buildDefaultDomainLookup( lookups ) );
	}

	/**
	 * Gets the list of lookups and from cache if possible, will also cache them if they have been newly created.
	 *
	 * @return list of lookups
	 */
	private List<DomainContextLookup> retrieveLookups() {
		return buildDomainSpecificLookups();
	}

	/**
	 * Builds the list of lookups that should be used.
	 * Fetches all domains, checks if they are active and if their metadata is {@link WebCmsSiteConfiguration}.
	 * If so, create a lookup for every hostname in the configuration.
	 * <p/>
	 * The list should not include the default domain lookup.
	 * <p/>
	 * <strong>NOTE</strong>: Only called if lookup cache is being refreshed.
	 *
	 * @return list of lookups
	 */
	protected abstract List<DomainContextLookup> buildDomainSpecificLookups();

	/**
	 * Builds the {@link DomainContextLookup} for the default domain.
	 * This lookup will be used in case no lookup matched based on the hostname.
	 * <p/>
	 * Any configured {@link DomainContextLookup#hostPattern} will be ignored.
	 * <p/>
	 * <strong>NOTE</strong>: Only called if lookup cache is being refreshed.
	 *
	 * @param domainSpecificLookups list of lookups for actual domains
	 * @return lookup
	 */
	protected abstract DomainContextLookup buildDefaultDomainLookup( List<DomainContextLookup> domainSpecificLookups );

	/**
	 * Convert a configured hostname to a lookup pattern.
	 * <ul>
	 * <li><strong>**</strong> will match multiple domain segments</li>
	 * <li><strong>*</strong> will match a single domain segment</li>
	 * <li>if no port is specified all domains will match regardless of port</li>
	 * </ul>
	 *
	 * @param hostName pattern
	 * @return regex pattern
	 */
	@SuppressWarnings("WeakerAccess")
	protected Pattern compileHostPattern( String hostName ) {
		String patternString = StringUtils.defaultString( hostName )
		                                  .replace( ".", "\\." )
		                                  .replace( "**", ".+" )
		                                  .replace( "*", "[^\\.]+" );

		if ( !hostName.contains( ":" ) ) {
			patternString += "(:\\d+)?";
		}

		return Pattern.compile( "^" + patternString + "$", Pattern.CASE_INSENSITIVE );
	}

	/**
	 * Helper for a single hostname lookup that maps to a {@link WebCmsDomainContext}
	 * and its corresponding default {@link Locale}.
	 */
	@AllArgsConstructor
	public static class DomainContextLookup
	{
		private Pattern hostPattern;

		@Getter
		private Locale locale;

		@NonNull
		@Getter
		private WebCmsDomainContext domainContext;

		/**
		 * Checks if the hostname matches with the configured host pattern.
		 * Will throw a {@link NullPointerException} if no host pattern configured.
		 *
		 * @param hostName to check
		 * @return true if hostname matches
		 */
		public boolean matches( String hostName ) {
			return hostPattern.matcher( hostName ).matches();
		}
	}
}
