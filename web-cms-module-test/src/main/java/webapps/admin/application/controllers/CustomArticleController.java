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

package webapps.admin.application.controllers;

import com.foreach.across.modules.web.template.ClearTemplate;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticle;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import com.foreach.across.modules.webcms.domain.asset.web.WebCmsAssetMapping;
import com.foreach.across.modules.webcms.domain.page.web.PageTemplateResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Controller
@RequiredArgsConstructor
@WebCmsAssetMapping(WebCmsArticle.class)
public class CustomArticleController
{
	private final PageTemplateResolver pageTemplateResolver;

	/**
	 * Dispatch to the template configured on the publication article template page.
	 */
	@ClearTemplate
	@GetMapping
	public String template( WebCmsAssetEndpoint<WebCmsArticle> endpoint, WebCmsUrl url, WebCmsArticle article, Model model ) {
		model.addAttribute( "asset", endpoint.getAsset() );
		model.addAttribute( "article", article );
		model.addAttribute( "page", article.getPublication().getArticleTemplatePage() );

		return pageTemplateResolver.resolvePageTemplate( article.getPublication().getArticleTemplatePage().getTemplate() );
	}
}
