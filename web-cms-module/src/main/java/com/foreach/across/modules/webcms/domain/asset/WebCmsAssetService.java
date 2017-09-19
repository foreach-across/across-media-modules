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

package com.foreach.across.modules.webcms.domain.asset;

import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpointService;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsUriComponentsService;

import java.util.Optional;

/**
 * Builds preview urls for {@link WebCmsAsset}s
 *
 * @author Steven Gentens
 * @see WebCmsUriComponentsService
 * @see WebCmsEndpointService
 * @since 0.0.3
 */
public interface WebCmsAssetService
{
	/**
	 * Builds a preview url for a particular {@link WebCmsAsset} on the current domain.
	 * The preview url is the primary url appended with a request parameter <strong>wcmPreview</strong>.
	 *
	 * @param asset to create the url for
	 * @return url if a primary url was available
	 */
	Optional<String> buildPreviewUrl( WebCmsAsset asset );

	/**
	 * Builds a preview url for a particular {@link WebCmsAsset} on the current domain.
	 * The preview url is the primary url appended with a request parameter <strong>wcmPreview</strong>.
	 *
	 * @param asset  to create the url for
	 * @param domain to create the url on
	 * @return url if a primary url was available
	 */
	Optional<String> buildPreviewUrlOnDomain( WebCmsAsset asset, WebCmsDomain domain );
}
