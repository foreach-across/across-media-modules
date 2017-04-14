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

package com.foreach.across.modules.webcms.web.asset.processors;

import com.foreach.across.modules.webcms.domain.article.WebCmsArticle;
import com.foreach.across.modules.webcms.domain.image.WebCmsImage;
import com.foreach.across.modules.webcms.domain.image.WebCmsImageRepository;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Component;

/**
 * @author: Sander Van Loock
 * @since: 0.0.1
 */
@Component
public class WebCmsArticleImageFormViewProcessor extends ImageFormViewProcessor<WebCmsArticle>
{
	private final WebCmsImageRepository imageRepository;

	public WebCmsArticleImageFormViewProcessor( BeanFactory beanFactory, WebCmsImageRepository imageRepository ) {
		super( beanFactory );
		this.imageRepository = imageRepository;
	}

	@Override
	protected void processImageHolder( WebCmsArticle article, String externalId ) {
		WebCmsImage image = WebCmsImage.builder()
		                               .name( article.getTitle() + "-" + externalId )
		                               .externalId( externalId )
		                               .build();
		imageRepository.save( image );
		article.setImage( image );
	}
}
