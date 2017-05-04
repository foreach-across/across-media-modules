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

/**
 * Responsible for creating the default web components for a {@link WebCmsArticle};
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
public interface WebCmsArticleComponentsStrategy
{
	/**
	 * Create the default components for a particular article.
	 * How the default components are determined is implementation dependant.
	 *
	 * @param article to add the components to
	 */
	void createDefaultComponents( WebCmsArticle article );
}
