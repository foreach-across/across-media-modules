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
 * Interface for attaching a {@link WebCmsDomain} to an instance. Used to implement custom domain metadata.
 * <p/>
 * Metadata is usually stored in the {@link WebCmsDomain#getAttributes()} but can come from anywhere.
 * Use a custom metadata implementation to wrap your application specific domain attributes.
 * You can specify the actual metadata implementation to use on the
 * {@link com.foreach.across.modules.webcms.domain.domain.config.WebCmsMultiDomainConfiguration}.
 * Metadata instances get created as beans and the domain they belong to gets set using {@link #setWebCmsDomain(WebCmsDomain)}.
 *
 * @author Arne Vandamme
 * @see com.foreach.across.modules.webcms.domain.domain.config.WebCmsMultiDomainConfiguration
 * @since 0.0.3
 */
public interface WebCmsDomainAware
{
	/**
	 * Set the domain this metadata belongs to.
	 *
	 * @param domain instance
	 */
	void setWebCmsDomain( WebCmsDomain domain );
}
