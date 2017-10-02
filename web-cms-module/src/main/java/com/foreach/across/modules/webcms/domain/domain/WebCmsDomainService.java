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
 * Core API for retrieving domain data, with caching support.  If you want to use domain-aware methods in a multi-domain context,
 * you should use the {@link WebCmsMultiDomainService} instead.  The latter extends this interface.
 *
 * @author Arne Vandamme
 * @see WebCmsMultiDomainService
 * @since 0.0.3
 */
public interface WebCmsDomainService
{
	/**
	 * Get the domain with that object id.
	 *
	 * @param objectId of the domain
	 * @return domain or {@code null} if not found
	 */
	WebCmsDomain getDomain( String objectId );

	/**
	 * Get the domain with the specified domain key.
	 *
	 * @param domainKey of the domain
	 * @return domain or {@code null} if not found
	 */
	WebCmsDomain getDomainByKey( String domainKey );

	/**
	 * Retrieve the metadata for a specific domain.
	 * Coerce it to the given type. A {@link ClassCastException} will be thrown if the metadata is not of the right type.
	 *
	 * @param domain       or {@code null} for the no-domain
	 * @param metadataType type the metadata should have
	 * @return metadata or null if none available
	 */
	<U> U getMetadataForDomain( WebCmsDomain domain, Class<U> metadataType );
}
