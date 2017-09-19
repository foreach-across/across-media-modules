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

package com.foreach.across.modules.webcms.domain.page.services;

import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.infrastructure.ModificationReport;
import com.foreach.across.modules.webcms.infrastructure.ModificationType;

import java.util.Map;
import java.util.Optional;

/**
 * API for managing {@link WebCmsPage} instances.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
public interface WebCmsPageService
{
	/**
	 * Find a single page by its unique canonical path.
	 * This method will use {@link #findByCanonicalPathAndDomain} to retrieve the page.
	 * The domain used in looking up the page will be retrieved from the {@link com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService}
	 * @param canonicalPath unique path
	 * @return page if found
	 */
	Optional<WebCmsPage> findByCanonicalPath( String canonicalPath );

	/**
	 * Find a single page by its canonicalPath within the specified domain.
	 *
	 * @param canonicalPath the unique path within the domain
	 * @param domain        the domain
	 * @return page if found
	 */
	Optional<WebCmsPage> findByCanonicalPathAndDomain( String canonicalPath, WebCmsDomain domain );

	/**
	 * Prepares a {@link WebCmsPage} for saving.  This will check the different settings on the page,
	 * and will generate the fields that need generating.  The instance passed as a parameter will be updated
	 * with the new values.  If the new values are different, a {@link ModificationReport} will be returned
	 * for the action taken.
	 * <p/>
	 * Note: this method does not perform actual validation on the properties.
	 *
	 * @param page to update
	 * @return map of possible modifications that have been performed
	 */
	Map<ModificationType, ModificationReport<PrepareModificationType, Object>> prepareForSaving( WebCmsPage page );
}
