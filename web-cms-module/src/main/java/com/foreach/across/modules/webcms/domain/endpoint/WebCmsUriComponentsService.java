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

package com.foreach.across.modules.webcms.domain.endpoint;

import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.web.WebCmsDomainUrlConfigurer;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

/**
 * Utility service that builds an extendable {@link UriComponentsBuilder} for a given {@link WebCmsAsset}, {@link WebCmsEndpoint} or {@link WebCmsUrl}
 *
 * @author Steven Gentens
 * @since 0.0.3
 */
public interface WebCmsUriComponentsService
{
	/**
	 * Builds a {@link UriComponentsBuilder} for a particular {@link WebCmsAsset} on the current domain.
	 * The UriComponentsBuilder contains the path of the primary url of the asset if available.
	 * <p>
	 * If the current domain contains {@link com.foreach.across.modules.webcms.domain.domain.web.WebCmsDomainUrlConfigurer} metadata,
	 * the UriComponentsBuilder will contain the prefix url if the domain of the asset differs from the current domain or if
	 * {@link WebCmsDomainUrlConfigurer#isAlwaysPrefix()} is {@code true}.
	 *
	 * @param asset to build the UriComponentsBuilder for
	 * @return UriComponentsBuilder if the primary url was available.
	 */
	Optional<UriComponentsBuilder> buildUriComponents( WebCmsAsset asset );

	/**
	 * Builds a {@link UriComponentsBuilder} for a particular {@link WebCmsAsset} on the current domain.
	 * The UriComponentsBuilder contains the path of the primary url of the asset if available.
	 * <p>
	 * If the current domain contains {@link com.foreach.across.modules.webcms.domain.domain.web.WebCmsDomainUrlConfigurer} metadata,
	 * the UriComponentsBuilder will contain the prefix url if the domain of the asset differs from the current domain or if
	 * {@link WebCmsDomainUrlConfigurer#isAlwaysPrefix()} is {@code true}.
	 *
	 * @param asset  to build the UriComponentsBuilder for
	 * @param domain to build the UriComponentsBuilder for
	 * @return UriComponentsBuilder if the primary url was available.
	 */
	Optional<UriComponentsBuilder> buildUriComponents( WebCmsAsset asset, WebCmsDomain domain );

	/**
	 * Builds a {@link UriComponentsBuilder} for a particular {@link WebCmsEndpoint} on the current domain.
	 * The UriComponentsBuilder contains the path of the primary url of the asset if available.
	 * <p>
	 * If the current domain contains {@link com.foreach.across.modules.webcms.domain.domain.web.WebCmsDomainUrlConfigurer} metadata,
	 * the UriComponentsBuilder will contain the prefix url if the domain of the asset differs from the current domain or if
	 * {@link WebCmsDomainUrlConfigurer#isAlwaysPrefix()} is {@code true}.
	 *
	 * @param endpoint to build the UriComponentsBuilder for
	 * @return UriComponentsBuilder if the primary url was available.
	 */
	Optional<UriComponentsBuilder> buildUriComponents( WebCmsEndpoint endpoint );

	/**
	 * Builds a {@link UriComponentsBuilder} for a particular {@link WebCmsEndpoint} on the current domain.
	 * The UriComponentsBuilder contains the path of the primary url of the asset if available.
	 * <p>
	 * If the current domain contains {@link com.foreach.across.modules.webcms.domain.domain.web.WebCmsDomainUrlConfigurer} metadata,
	 * the UriComponentsBuilder will contain the prefix url if the domain of the asset differs from the current domain or if
	 * {@link WebCmsDomainUrlConfigurer#isAlwaysPrefix()} is {@code true}.
	 *
	 * @param endpoint to build the UriComponentsBuilder for
	 * @param domain   to build the UriComponentsBuilder for
	 * @return UriComponentsBuilder if the primary url was available.
	 */
	Optional<UriComponentsBuilder> buildUriComponents( WebCmsEndpoint endpoint, WebCmsDomain domain );

	/**
	 * Builds a {@link UriComponentsBuilder} for a particular {@link WebCmsUrl} on the current domain.
	 * The UriComponentsBuilder contains the path of the primary url of the asset if available.
	 * The domain of the url is specified by its endpoint.
	 * <p>
	 * If the current domain contains {@link com.foreach.across.modules.webcms.domain.domain.web.WebCmsDomainUrlConfigurer} metadata,
	 * the UriComponentsBuilder will contain the prefix url if the domain of the asset differs from the current domain or if
	 * {@link WebCmsDomainUrlConfigurer#isAlwaysPrefix()} is {@code true}.
	 *
	 * @param url to build the UriComponentsBuilder for
	 * @return UriComponentsBuilder if the primary url was available.
	 */
	Optional<UriComponentsBuilder> buildUriComponents( WebCmsUrl url );

	/**
	 * Builds a {@link UriComponentsBuilder} for a particular {@link WebCmsUrl} on the current domain.
	 * The UriComponentsBuilder contains the path of the primary url of the asset if available.
	 * The domain of the url is specified by its endpoint.
	 * <p>
	 * If the current domain contains {@link com.foreach.across.modules.webcms.domain.domain.web.WebCmsDomainUrlConfigurer} metadata,
	 * the UriComponentsBuilder will contain the prefix url if the domain of the asset differs from the current domain or if
	 * {@link WebCmsDomainUrlConfigurer#isAlwaysPrefix()} is {@code true}.
	 *
	 * @param url    to build the UriComponentsBuilder for
	 * @param domain to build the UriComponentsBuilder for
	 * @return UriComponentsBuilder if the primary url was available.
	 */
	Optional<UriComponentsBuilder> buildUriComponents( WebCmsUrl url, WebCmsDomain domain );
}
