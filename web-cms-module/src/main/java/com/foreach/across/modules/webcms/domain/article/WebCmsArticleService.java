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

package com.foreach.across.modules.webcms.domain.article;

import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;

/**
 * Service for retrieving {@link WebCmsArticle} and {@link WebCmsArticleType} instances.
 * Inspects the multi-domain configuration to fallback to shared entities if necessary.
 *
 * @author Arne Vandamme
 * @since 0.0.3
 */
public interface WebCmsArticleService
{
	/**
	 * Get the {@link WebCmsArticleType} with the specified object id.
	 *
	 * @param objectId the article type should have
	 * @return page type or {@code null}
	 */
	WebCmsArticleType getArticleType( String objectId );

	/**
	 * Get the {@link WebCmsArticleType} with the specified type key, attached to the current domain.
	 * Will take the multi-domain configuration into account when looking for the article type:
	 * will also inspect no-domain if allowed for {@link WebCmsArticleType}.
	 *
	 * @param typeKey the page type should have
	 * @return article type or {@code null} if not found
	 */
	WebCmsArticleType getArticleTypeByKey( String typeKey );

	/**
	 * Get the {@link WebCmsArticleType} with the specified type key, attached to the specified domain.
	 * Will take the multi-domain configuration into account when looking for the article type:
	 * will also inspect no-domain if allowed for {@link WebCmsArticleType}.
	 *
	 * @param typeKey the article type should have
	 * @param domain  the article type is requested for
	 * @return article type or {@code null} if not found
	 */
	WebCmsArticleType getArticleTypeByKey( String typeKey, WebCmsDomain domain );
}
