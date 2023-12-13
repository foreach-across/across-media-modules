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

package com.foreach.across.modules.webcms.domain.publication;

import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;

/**
 * Service for retrieving {@link WebCmsPublication} instances.
 * Inspects the multi-domain configuration to fallback to shared publications if necessary.
 *
 * @author Arne Vandamme
 * @since 0.0.3
 */
public interface WebCmsPublicationService
{
	/**
	 * Finds the publication represented by the publication key.
	 * <p/>
	 * Will inspect the multi-domain configuration and use the current domain as well as fallback
	 * to no-domain if allowed for {@link WebCmsPublication}.
	 *
	 * @param publicationKey of the publication
	 * @return publication or {@code null} if not found
	 */
	WebCmsPublication getPublicationByKey( String publicationKey );

	/**
	 * Finds the publication represented by the publication key and attached to that domain.
	 * <p/>
	 * Will inspect the multi-domain configuration and use the specified domain as well as fallback
	 * to no-domain if allowed for {@link WebCmsPublication}.
	 *
	 * @param publicationKey of the publication
	 * @return publication or {@code null} if not found
	 */
	WebCmsPublication getPublicationByKey( String publicationKey, WebCmsDomain domain );
}
