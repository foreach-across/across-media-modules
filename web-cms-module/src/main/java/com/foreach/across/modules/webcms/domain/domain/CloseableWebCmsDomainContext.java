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

/**
 * Implementation that will set the current {@link WebCmsDomainContext} based on the given
 * {@link WebCmsDomain} instance upon creation, and will reset to the previous
 * WebCmsDomainContext when {@link #close()} is called.
 * <p/>
 * The instance is expected to be disposed after closing.
 *
 * @author Steven Gentens
 * @since 0.0.3
 */
public class CloseableWebCmsDomainContext implements AutoCloseable
{
	private WebCmsDomainContext previousDomainContext;
	private boolean closed;

	public CloseableWebCmsDomainContext( WebCmsDomainContext newDomainContext ) {
		previousDomainContext = WebCmsDomainContextHolder.getWebCmsDomainContext();
		WebCmsDomainContextHolder.setWebCmsDomainContext( newDomainContext );
	}

	public CloseableWebCmsDomainContext() {
		previousDomainContext = WebCmsDomainContextHolder.getWebCmsDomainContext();
	}

	@Override
	public void close() {
		if ( !closed ) {
			closed = true;
			WebCmsDomainContextHolder.setWebCmsDomainContext( previousDomainContext );
			previousDomainContext = null;
		}
	}
}
