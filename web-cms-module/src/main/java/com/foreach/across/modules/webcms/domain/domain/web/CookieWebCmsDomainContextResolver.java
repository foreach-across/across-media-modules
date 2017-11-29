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
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.CookieGenerator;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Resolver that reads the {@link com.foreach.across.modules.webcms.domain.domain.WebCmsDomainContext} from a cookie,
 * and writes it to a cookie as well.  Mainly for use in admin web as usually a domain will be resolved in another manner
 * (eg. through DNS) for the front-end.
 *
 * @author Arne Vandamme
 * @see WebCmsDomainContextResolver
 * @since 0.0.3
 */
public class CookieWebCmsDomainContextResolver extends CookieGenerator implements WebCmsDomainContextResolver
{
	public static final String DEFAULT_COOKIE_NAME = CookieWebCmsDomainContextResolver.class.getName() + ".DOMAIN";
	private static final String NO_DOMAIN_VALUE = "no-domain";

	private final WebCmsDomainService domainService;

	public CookieWebCmsDomainContextResolver( WebCmsDomainService domainService ) {
		this.domainService = domainService;
		setCookieName( DEFAULT_COOKIE_NAME );
	}

	@Override
	public WebCmsDomainContext resolveDomainContext( HttpServletRequest request ) {
		String fixedDomainKey = request.getParameter( "wcmSelectDomain" );
		String objectId = retrieveDomainObjectIdFromCookie( request );

		if ( NO_DOMAIN_VALUE.equals( fixedDomainKey ) || ( fixedDomainKey == null && NO_DOMAIN_VALUE.equals( objectId ) ) ) {
			return WebCmsDomainContext.noDomain( domainService.getMetadataForDomain( WebCmsDomain.NONE, Object.class ) );
		}

		WebCmsDomain domain = retrieveDomain( fixedDomainKey, objectId );

		if ( domain != null ) {
			return new WebCmsDomainContext( domain, domainService.getMetadataForDomain( domain, Object.class ) );
		}

		return null;
	}

	private WebCmsDomain retrieveDomain( String fixedDomainKey, String objectIdFromCookie ) {
		WebCmsDomain domain = null;

		if ( fixedDomainKey != null ) {
			domain = domainService.getDomainByKey( fixedDomainKey );
		}

		if ( domain == null && objectIdFromCookie != null ) {
			domain = domainService.getDomain( objectIdFromCookie );
		}

		return domain;
	}

	@Override
	public void setDomainContext( HttpServletRequest request, HttpServletResponse response, WebCmsDomainContext domainContext ) {
		String currentValue = retrieveDomainObjectIdFromCookie( request );

		if ( domainContext != null ) {
			String value = domainContext.holdsDomain() ? domainContext.getDomain().getObjectId() : NO_DOMAIN_VALUE;
			if ( !StringUtils.equals( currentValue, value ) ) {
				addCookie( response, value );
			}
		}
		else if ( currentValue != null ) {
			removeCookie( response );
		}
	}

	private String retrieveDomainObjectIdFromCookie( HttpServletRequest request ) {
		Cookie cookie = WebUtils.getCookie( request, getCookieName() );
		return cookie != null ? cookie.getValue() : null;
	}
}
