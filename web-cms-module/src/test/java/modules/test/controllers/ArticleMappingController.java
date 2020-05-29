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

package modules.test.controllers;

import com.foreach.across.modules.webcms.domain.article.WebCmsArticle;
import com.foreach.across.modules.webcms.domain.article.web.WebCmsArticleMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Defines specific article mappings for any publication type facts.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Controller
@ResponseBody
@WebCmsArticleMapping(publicationType = { "fun-facts", "boring-facts" })
public class ArticleMappingController
{
	@WebCmsArticleMapping
	public String publicationTypeMapping( WebCmsArticle article ) {
		return "publicationTypeMapping: " + article.getTitle();
	}

	@WebCmsArticleMapping(articleType = "big-article")
	public String bigArticleTypeMapping( WebCmsArticle article ) {
		return "bigArticleTypeMapping: " + article.getTitle();
	}

	@WebCmsArticleMapping(publication = "movies")
	public String publicationMapping( WebCmsArticle article ) {
		return "publicationMapping: " + article.getTitle();
	}

	@WebCmsArticleMapping(publication = "movies", articleType = "small-article")
	public String articleTypeOnPublicationMapping( WebCmsArticle article ) {
		return "articleTypeOnPublicationMapping: " + article.getTitle();
	}

	@WebCmsArticleMapping(objectId = "wcm:asset:article:facts-five")
	public String articleObjectIdMapping( WebCmsArticle article ) {
		return "articleObjectIdMapping: " + article.getTitle();
	}
}
