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

import com.foreach.across.modules.webcms.data.WebCmsDataEntry;
import com.foreach.across.modules.webcms.domain.asset.AbstractWebCmsAssetImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
public final class WebCmsArticleImporter extends AbstractWebCmsAssetImporter<WebCmsArticle>
{
	private WebCmsArticleRepository articleRepository;

	public WebCmsArticleImporter() {
		super( "article", WebCmsArticle.class );
	}

	@Override
	protected WebCmsArticle createDto( WebCmsArticle itemToUpdate ) {
		return itemToUpdate != null ? itemToUpdate.toDto() : new WebCmsArticle();
	}

	@Override
	protected WebCmsArticle prepareForSaving( WebCmsArticle itemToBeSaved, WebCmsDataEntry data ) {
		return itemToBeSaved;
	}

	@Override
	protected WebCmsArticle getExistingByEntryKey( String entryKey ) {
		return articleRepository.findOneByAssetId( entryKey );
	}

	@Autowired
	void setArticleRepository( WebCmsArticleRepository articleRepository ) {
		this.articleRepository = articleRepository;
	}
}
