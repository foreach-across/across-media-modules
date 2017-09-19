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

import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * Represents a {@link WebCmsDomain} with its associated {@link WebCmsDomainAware}.
 * Note that both the actual domain and its metadata can be {@code null}.  A metadata instance is always
 * optional (but strongly advised), a {@code null} domain represents the special <strong>no-domain</strong> context,
 * in which case there can still be metadata available depending on your application setup.
 * <p/>
 * If you want to initialize a context for no-domain, you should use the {@link #noDomain(WebCmsDomainAware)} factory methods.
 *
 * @author Arne Vandamme
 * @since 0.0.3
 */
@Value
public class WebCmsDomainContext
{
	private final WebCmsDomain domain;
	private final Object metadata;

	public WebCmsDomainContext( WebCmsDomain domain, Object metadata ) {
		Assert.notNull( domain, "Unable to create a WebCmsDomainContext without a domain." );
		this.domain = domain;
		this.metadata = metadata;
	}

	private WebCmsDomainContext( Object metadata ) {
		this.domain = null;
		this.metadata = metadata;
	}

	/**
	 * @return context representing no-domain - without metadata
	 */
	public static WebCmsDomainContext noDomain() {
		return noDomain( null );
	}

	/**
	 * @param metadata to put on the context
	 * @return context representing no-domain holding the specified metadata
	 */
	public static WebCmsDomainContext noDomain( Object metadata ) {
		return new WebCmsDomainContext( metadata );
	}

	/**
	 * Retrieve the domain metadata and coerce it to a the expected type.
	 * A {@link ClassCastException} will be thrown if the metadata is not of the right type.
	 *
	 * @param expectedType the metadata should have
	 * @param <T>          metadata type
	 * @return metadata cast as type
	 */
	public <T> T getMetadata( Class<T> expectedType ) {
		return expectedType.cast( metadata );
	}

	/**
	 * Check if the metadata is of a particular type.
	 * If no metadata is set, this method will always return {@code false}.
	 *
	 * @param expectedType for the metadata
	 * @return true if metadata is of the actual type
	 */
	public boolean isMetadataOfType( Class<?> expectedType ) {
		return expectedType.isInstance( metadata );
	}

	/**
	 * @return true if metadata is configured
	 */
	public boolean hasMetadata() {
		return metadata != null;
	}

	/**
	 * @return true if a domain is present
	 */
	public boolean holdsDomain() {
		return domain != null;
	}

	/**
	 * Check if the context holds the domain with the specified domain key.
	 * If the domain key is null, this will check if no-domain is attached.
	 *
	 * @param domainKey to check for
	 * @return true if the attached domain has that key
	 */
	public boolean holdsDomain( String domainKey ) {
		if ( domainKey == null ) {
			return isNoDomain();
		}
		return domain != null && StringUtils.equals( domainKey, domain.getDomainKey() );
	}

	/**
	 * @return true if no domain is present (opposite of {@link #holdsDomain()})
	 */
	public boolean isNoDomain() {
		return !holdsDomain();
	}
}
