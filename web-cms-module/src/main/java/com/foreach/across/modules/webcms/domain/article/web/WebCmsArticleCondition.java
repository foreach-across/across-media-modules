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

package com.foreach.across.modules.webcms.domain.article.web;

import com.foreach.across.modules.web.mvc.condition.AbstractCustomRequestCondition;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticle;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticleType;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.endpoint.web.WebCmsEndpointContextResolver;
import com.foreach.across.modules.webcms.domain.endpoint.web.context.ConfigurableWebCmsEndpointContext;
import com.foreach.across.modules.webcms.domain.publication.WebCmsPublication;
import com.foreach.across.modules.webcms.domain.publication.WebCmsPublicationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Collection;

import static com.foreach.across.modules.webcms.domain.endpoint.web.controllers.WebCmsAssetConditionUtils.combineArrays;
import static com.foreach.across.modules.webcms.domain.endpoint.web.controllers.WebCmsAssetConditionUtils.compareArrays;
import static org.apache.commons.lang3.ArrayUtils.contains;

/**
 * A condition for matching a {@link com.foreach.across.modules.webcms.domain.article.WebCmsArticle} being requested.
 *
 * @author Arne Vandamme
 * @see WebCmsArticleMapping
 * @since 0.0.2
 */
@RequiredArgsConstructor
@Slf4j
class WebCmsArticleCondition extends AbstractCustomRequestCondition<WebCmsArticleCondition>
{
	private final ConfigurableWebCmsEndpointContext context;
	private final WebCmsEndpointContextResolver resolver;
	String[] articleTypes = {};
	String[] publicationTypes = {};
	String[] publications = {};
	private String[] objectIds = {};

	/**
	 * Set the values for this condition based on the attributes of the annotated element.
	 *
	 * @param annotatedElement this condition is attached to
	 */
	@Override
	public void setAnnotatedElement( AnnotatedElement annotatedElement ) {
		WebCmsArticleMapping articleMapping = AnnotatedElementUtils.findMergedAnnotation( annotatedElement, WebCmsArticleMapping.class );

		articleTypes = articleMapping.articleType();
		publicationTypes = articleMapping.publicationType();
		publications = articleMapping.publication();
		// take objectIds into account for ordering - but leave actual matching check to the WebCmsAssetEndpointCondition
		objectIds = articleMapping.objectId();
	}

	@Override
	protected Collection<?> getContent() {
		return Arrays.asList( articleTypes, publicationTypes, publications );
	}

	@Override
	protected String getToStringInfix() {
		return " && ";
	}

	@Override
	public WebCmsArticleCondition combine( WebCmsArticleCondition other ) {
		WebCmsArticleCondition result = new WebCmsArticleCondition( this.context, this.resolver );
		result.articleTypes = combineArrays( articleTypes, other.articleTypes );
		result.publicationTypes = combineArrays( publicationTypes, other.publicationTypes );
		result.publications = combineArrays( publications, other.publications );
		result.objectIds = combineArrays( objectIds, other.objectIds );

		return result;
	}

	@Override
	public WebCmsArticleCondition getMatchingCondition( HttpServletRequest request ) {
		if ( !context.isResolved() ) {
			resolver.resolve( context, request );
		}

		WebCmsArticle article = retrieveArticle();

		if ( article != null ) {
			if ( !isValidArticleType( article ) || !isValidPublicationType( article ) || !isValidPublication( article ) ) {
				return null;
			}

			LOG.trace( "Matching WebCmsArticleCondition: {}", this );
			return this;
		}

		return null;
	}

	private boolean isValidPublication( WebCmsArticle article ) {
		if ( publications.length > 0 ) {
			WebCmsPublication publication = article.getPublication();
			return publication != null
					&& ( contains( publications, publication.getPublicationKey() ) || contains( publications, publication.getObjectId() ) );
		}
		return true;
	}

	private boolean isValidPublicationType( WebCmsArticle article ) {
		if ( publicationTypes.length > 0 ) {
			WebCmsPublication publication = article.getPublication();
			WebCmsPublicationType publicationType = publication != null ? publication.getPublicationType() : null;
			return publicationType != null
					&& ( contains( publicationTypes, publicationType.getTypeKey() ) || contains( publicationTypes, publicationType.getObjectId() ) );
		}
		return true;
	}

	private boolean isValidArticleType( WebCmsArticle article ) {
		if ( articleTypes.length > 0 ) {
			WebCmsArticleType articleType = article.getArticleType();
			return articleType != null
					&& ( contains( articleTypes, articleType.getTypeKey() ) || contains( articleTypes, articleType.getObjectId() ) );
		}
		return true;
	}

	private WebCmsArticle retrieveArticle() {
		if ( context.isOfType( WebCmsAssetEndpoint.class ) ) {
			WebCmsAssetEndpoint endpoint = context.getEndpoint( WebCmsAssetEndpoint.class );
			return endpoint.getAsset() instanceof WebCmsArticle ? (WebCmsArticle) endpoint.getAsset() : null;
		}
		return null;
	}

	@Override
	public int compareTo( WebCmsArticleCondition other, HttpServletRequest request ) {
		int val = compareArrays( objectIds, other.objectIds );

		if ( val == 0 ) {
			val = compareArrays( publications, other.publications );
		}

		if ( val == 0 ) {
			val = compareArrays( publicationTypes, other.publicationTypes );
		}

		if ( val == 0 ) {
			val = compareArrays( articleTypes, other.articleTypes );
		}

		return val;
	}

}
