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

import com.foreach.across.modules.webcms.domain.domain.config.WebCmsMultiDomainConfiguration;

/**
 * Central API for working with a multi-domain context.
 *
 * @author Arne Vandamme
 * @see WebCmsMultiDomainConfiguration
 * @since 0.0.3
 */
public interface WebCmsMultiDomainService
{
	/**
	 * Always returns the current domain, bound to the {@link WebCmsDomainContextHolder}.
	 * This method will return a single domain only or none.
	 *
	 * @return domain or null
	 */
	WebCmsDomain getCurrentDomain();

	/**
	 * Return the metadata for the current domain.
	 * Can be {@code null} if not metadata is available.
	 * A {@link ClassCastException} will be thrown if the metadata is not of the right type.
	 *
	 * @param metadataType the metadata should have
	 * @param <U>          metadata type reference
	 * @return metadata or null if none available
	 */
	<U> U getCurrentDomainMetadata( Class<U> metadataType );

	/**
	 * Returns the current domain for a particular entity.  This is basically the same as
	 * {@link #getCurrentDomain()} if the call of {@link #isDomainBound(Object)} returns {@code true}.
	 * <p/>
	 * If the entity type is not domain-bound, this will always return {@code null}.
	 *
	 * @param entity for which to retrieve the current domain
	 * @return domain or null
	 */
	WebCmsDomain getCurrentDomainForEntity( Object entity );

	/**
	 * Returns the current domain for a particular entity type.  This is basically the same as
	 * {@link #getCurrentDomain()} if the call of {@link #isDomainBound(Class)} returns {@code true}.
	 * <p/>
	 * If the entity type is not domain-bound, this will always return {@code null}.
	 *
	 * @param entityType for which to retrieve the current domain
	 * @return domain or null
	 */
	WebCmsDomain getCurrentDomainForType( Class<?> entityType );

	/**
	 * Retrieve the metadata for a specific domain.
	 * Coerce it to the given type. A {@link ClassCastException} will be thrown if the metadata is not of the right type.
	 *
	 * @param domain       or {@code null} for the no-domain
	 * @param metadataType type the metadata should have
	 * @return metadata or null if none available
	 */
	<U> U getMetadataForDomain( WebCmsDomain domain, Class<U> metadataType );

	/**
	 * Check if a particular entity is domain-bound according to the current {@link WebCmsMultiDomainConfiguration}.
	 *
	 * @param entity to check
	 * @return true if domain-bound
	 */
	boolean isDomainBound( Object entity );

	/**
	 * Check if a particular entity type is domain-bound according to the current {@link WebCmsMultiDomainConfiguration}.
	 *
	 * @param entityType to check
	 * @return true if domain-bound
	 */
	boolean isDomainBound( Class<?> entityType );

	/**
	 * Check if a particular entity does not require a specific domain.
	 * This will be true for either non-domain bound entities, of for domain-bound
	 * entities that also allow no-domain.
	 *
	 * @param entity to check
	 * @return true if no-domain is allowed
	 */
	boolean isNoDomainAllowed( Object entity );

	/**
	 * Check if a particular entity type does not require a specific domain.
	 * This will be true for either non-domain bound entities, of for domain-bound
	 * entities that also allow no-domain.
	 *
	 * @param entityType to check
	 * @return true if no-domain is allowed
	 */
	boolean isNoDomainAllowed( Class<?> entityType );

	/**
	 * Create a new {@link WebCmsDomainContext} for the given domain, and attach it to the {@link WebCmsDomainContextHolder}.
	 * This will load the metadata, create the context and effectively change the value of {@link #getCurrentDomain()}.
	 * <p/>
	 * This method returns an {@link AutoCloseable} referring the previous domain context.
	 * When calling the {@link CloseableWebCmsDomainContext#close()}, the previous domain context will be reset.
	 *
	 * @param domain to set
	 * @return closeable
	 */
	CloseableWebCmsDomainContext attachDomainContext( WebCmsDomain domain );

	boolean isCurrentDomain( WebCmsDomain domain );
}
