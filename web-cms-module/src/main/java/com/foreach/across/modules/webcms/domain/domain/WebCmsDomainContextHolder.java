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

import org.springframework.core.NamedInheritableThreadLocal;

/**
 * Holder class that associates a {@link WebCmsDomainContext} with the current thread.
 *
 * @author Arne Vandamme
 * @since 0.0.3
 */
public abstract class WebCmsDomainContextHolder
{
	private static final ThreadLocal<WebCmsDomainContext> HOLDER =
			new NamedInheritableThreadLocal<>( "WebCmsDomainContext" );

	private WebCmsDomainContextHolder() {
	}

	/**
	 * @return the context attached to the current thread
	 */
	public static WebCmsDomainContext getWebCmsDomainContext() {
		return HOLDER.get();
	}

	/**
	 * Associate the given context with the current thread.  Will replace any previously configured context.
	 * If the context parameter is {@code null}, the attached context will be removed.
	 *
	 * @param domainContext instance
	 */
	public static void setWebCmsDomainContext( WebCmsDomainContext domainContext ) {
		if ( domainContext != null ) {
			HOLDER.set( domainContext );
		}
		else {
			clearWebCmsDomainContext();
		}
	}

	/**
	 * Removes the (optional) context attached to the current thread.
	 *
	 * @return the context that has been removed
	 */
	public static void clearWebCmsDomainContext() {
		HOLDER.remove();
	}
}