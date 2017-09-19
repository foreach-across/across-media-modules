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
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainContext;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.i18n.AbstractLocaleContextResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * Simple {@link org.springframework.web.servlet.LocaleContextResolver} that checks if a domain locale
 * attribute has been set and uses that one if so.  Uses accept header locale as fallback (unless a default
 * has been configured on the bean itself).
 * <p/>
 * The domain locale attribute is usually set by a {@link AbstractWebCmsDomainContextFilter}.
 * <p/>
 * You can use this implementation as fallback for another (eg cookie) locale resolver.
 *
 * @author Arne Vandamme
 * @see AbstractWebCmsDomainContextFilter
 * @since 0.0.3
 */
@Exposed
@Component
public class WebCmsDomainLocaleContextResolver extends AbstractLocaleContextResolver
{
	public static final String LOCALE_ATTRIBUTE = WebCmsDomainContext.class.getName() + ".LOCALE";

	@Override
	public LocaleContext resolveLocaleContext( HttpServletRequest request ) {
		Locale locale = (Locale) request.getAttribute( LOCALE_ATTRIBUTE );
		return locale != null ? new SimpleLocaleContext( locale ) : new SimpleLocaleContext( determineDefaultLocale( request ) );
	}

	@Override
	public void setLocaleContext( HttpServletRequest request, HttpServletResponse response, LocaleContext localeContext ) {
	}

	/**
	 * Determine the default locale for the given request,
	 * Called if no Locale session attribute has been found.
	 * <p>The default implementation returns the specified default locale,
	 * if any, else falls back to the request's accept-header locale.
	 *
	 * @param request the request to resolve the locale for
	 * @return the default locale (never {@code null})
	 * @see #setDefaultLocale
	 * @see javax.servlet.http.HttpServletRequest#getLocale()
	 */
	protected Locale determineDefaultLocale( HttpServletRequest request ) {
		Locale defaultLocale = getDefaultLocale();
		if ( defaultLocale == null ) {
			defaultLocale = request.getLocale();
		}
		return defaultLocale;
	}
}
