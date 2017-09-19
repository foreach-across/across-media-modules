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

import com.foreach.across.modules.webcms.data.WebCmsDataAction;
import com.foreach.across.modules.webcms.data.WebCmsDataEntry;
import com.foreach.across.modules.webcms.domain.asset.AbstractWebCmsAssetImporter;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

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
	protected WebCmsArticle createDto( WebCmsDataEntry data, WebCmsArticle itemToUpdate, WebCmsDataAction action, Map<String, Object> dataValues ) {
		if ( action == WebCmsDataAction.REPLACE ) {
			return WebCmsArticle.builder()
			                    .id( itemToUpdate.getId() ).createdBy( itemToUpdate.getCreatedBy() ).createdDate( itemToUpdate.getCreatedDate() )
			                    .build();
		}

		return itemToUpdate != null ? itemToUpdate.toDto() : new WebCmsArticle();
	}

	@Override
	protected WebCmsArticle getExistingEntity( String entryKey, WebCmsDataEntry data, WebCmsDomain domain ) {
		if ( StringUtils.isEmpty( entryKey ) ) {
			return null;
		}
		return articleRepository.findOneByObjectId( entryKey );
	}

	@Autowired
	void setArticleRepository( WebCmsArticleRepository articleRepository ) {
		this.articleRepository = articleRepository;
	}
}
