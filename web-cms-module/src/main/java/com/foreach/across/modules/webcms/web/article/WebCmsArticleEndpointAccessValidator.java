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

package com.foreach.across.modules.webcms.web.article;

import com.foreach.across.modules.webcms.domain.article.WebCmsArticle;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint;
import com.foreach.across.modules.webcms.web.asset.WebCmsAssetEndpointAccessValidator;
import org.springframework.stereotype.Component;

/**
 * Both article asset itself but also the publication should be published.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
public class WebCmsArticleEndpointAccessValidator extends WebCmsAssetEndpointAccessValidator<WebCmsArticle>
{
	@Override
	public boolean appliesFor( WebCmsEndpoint endpoint ) {
		return endpoint instanceof WebCmsAssetEndpoint && ( (WebCmsAssetEndpoint) endpoint ).getAsset() instanceof WebCmsArticle;
	}

	@Override
	public boolean validateAccess( WebCmsAssetEndpoint<WebCmsArticle> endpoint ) {
		return super.validateAccess( endpoint ) && validateAccess( endpoint.getAsset().getPublication() );
	}
}
