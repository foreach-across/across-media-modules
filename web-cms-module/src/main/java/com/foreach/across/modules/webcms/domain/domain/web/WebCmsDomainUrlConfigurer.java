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

/**
 * API for domain metadata that describes how asset urls should be built.
 * If the domain metadata implements this interface, any generated url using the
 * {@link com.foreach.across.modules.webcms.domain.endpoint.WebCmsUriComponentsService} will be prefixed
 * using the configured {@link #getUrlPrefix()}.
 * <p/>
 * Unless {@link #isAlwaysPrefix()} is {@code true}, prefixing will only be done if the current domain is
 * different than the domain the endpoint belongs to.  This favours creating relative links on the domain itself.
 *
 * @author Steven Gentens
 * @since 0.0.3
 * @see com.foreach.across.modules.webcms.domain.endpoint.WebCmsUriComponentsService
 */
public interface WebCmsDomainUrlConfigurer
{
	/**
	 * @return the URI prefix to identify the domain
	 */
	String getUrlPrefix();

	/**
	 * @return whether or not the url should always be prefixed by the {@link WebCmsDomainUrlConfigurer#getUrlPrefix()}
	 */
	boolean isAlwaysPrefix();
}
