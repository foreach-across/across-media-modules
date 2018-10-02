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

package com.foreach.across.modules.webcms.domain.page.web;

import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for {@link WebCmsPage} templating.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Data
@ConfigurationProperties(prefix = "webCmsModule.pages")
public class PageTemplateProperties
{
	static final String DEFAULT_TEMPLATE = "th/webCmsModule/default-page-template";

	/**
	 * The default template to be used if there is no template specified on the {@link WebCmsPage}.
	 */
	private String defaultTemplate = DEFAULT_TEMPLATE;

	/**
	 * Prefix to apply if a relative template.
	 * This usually corresponds to the base location where templates are present.
	 */
	private String templatePrefix;

	/**
	 * If the template starts with any of these prefixes, it will be considered absolute and will
	 * not be prefixed.
	 */
	@Setter(AccessLevel.NONE)
	private String[] templatePrefixToIgnore = new String[] { "th/" };

	/**
	 * Suffix to be removed if present.
	 */
	private String templateSuffixToRemove = ".html";

	public void setTemplatePrefixToIgnore( String[] templatePrefixToIgnore ) {
		this.templatePrefixToIgnore = templatePrefixToIgnore.clone();
	}
}
