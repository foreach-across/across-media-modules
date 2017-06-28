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

import com.foreach.across.modules.web.mvc.condition.AbstractCustomRequestCondition;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.endpoint.web.WebCmsEndpointContextResolver;
import com.foreach.across.modules.webcms.domain.endpoint.web.context.ConfigurableWebCmsEndpointContext;
import com.foreach.across.modules.webcms.domain.endpoint.web.controllers.InvalidWebCmsEndpointConditionCombination;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A condition to use for retrieving the correct {@link com.foreach.across.modules.webcms.domain.page.WebCmsPage}.  This condition takes all properties of
 * {@link WebCmsPageMapping} into account.
 *
 * @author Raf Ceuls
 * @since 0.0.2
 */
@RequiredArgsConstructor
@Slf4j
final class WebCmsPageCondition extends AbstractCustomRequestCondition<WebCmsPageCondition>
{
	private final ConfigurableWebCmsEndpointContext context;
	private final WebCmsEndpointContextResolver resolver;

	private String[] canonicalPath;
	private String[] pageType;

	/**
	 * Set the values for this condition based on the attributes of the annotated element.
	 *
	 * @param annotatedElement this condition is attached to
	 */
	@Override
	public void setAnnotatedElement( AnnotatedElement annotatedElement ) {
		WebCmsPageMapping endpointMapping = AnnotatedElementUtils.findMergedAnnotation( annotatedElement, WebCmsPageMapping.class );

		canonicalPath = endpointMapping.canonicalPath();
		pageType = endpointMapping.pageType();
	}

	@Override
	protected Collection<?> getContent() {
		return Lists.asList( canonicalPath, pageType );
	}

	@Override
	protected String getToStringInfix() {
		return " && ";
	}

	@Override
	public WebCmsPageCondition combine( WebCmsPageCondition other ) {
		if ( !( context.isOfType( WebCmsAssetEndpoint.class ) ) ) {
			return null;
		}

		WebCmsPageCondition result = new WebCmsPageCondition( this.context, this.resolver );

		result.canonicalPath = combineStringArrays( this.canonicalPath, other.canonicalPath );
		result.pageType = combineStringArrays( this.pageType, other.pageType );

		return result;
	}

	private String[] combineStringArrays( String[] fromThis, String[] fromOther ) {
		if ( fromThis.length == 0 && fromOther.length == 0 ) {
			return new String[0];
		}

		if ( fromThis.length == 0 ) {
			return fromOther;
		}

		if ( fromOther.length == 0 ) {
			return fromThis;
		}

		List<String> combined = new ArrayList<String>();

		// check that "other" is more specific (being a subset) or equal to "this"
		for ( String otherIdentifier : fromOther ) {
			if ( !ArrayUtils.contains( fromThis, otherIdentifier ) ) {
				throw new InvalidWebCmsEndpointConditionCombination(
						String.format( "Current collection does not contain [%s]", otherIdentifier ) );
			}
			combined.add( otherIdentifier );
		}
		return combined.toArray( new String[combined.size()] );
	}

	@Override
	public WebCmsPageCondition getMatchingCondition( HttpServletRequest request ) {
		if ( !context.isResolved() ) {
			resolver.resolve( context, request );
		}

		WebCmsAsset rawAsset = context.getEndpoint( WebCmsAssetEndpoint.class ).getAsset();
		if ( context.isOfType( WebCmsAssetEndpoint.class ) && WebCmsPage.class.isInstance( rawAsset ) ) {
			WebCmsPage page = (WebCmsPage) rawAsset;

			WebCmsPageCondition result = new WebCmsPageCondition( context, resolver );

			if ( this.canonicalPath.length != 0 && ArrayUtils.contains( this.canonicalPath, page.getCanonicalPath() ) ) {
				result.canonicalPath = this.canonicalPath;
			}
			if ( this.pageType.length != 0 && ArrayUtils.contains( this.pageType, page.getPageType() ) ) {
				result.pageType = this.pageType;
			}

			if ( result.canonicalPath == null && result.pageType == null ) {
				return null;
			}


			LOG.trace( "Matching condition is {}", result );
			return result;
		}
		return null;
	}

	@Override
	public int compareTo( WebCmsPageCondition other, HttpServletRequest request ) {
		if ( pageType.length > 0 && other.pageType.length == 0 ) {
			return -1;
		}
		else if ( pageType.length == 0 && other.pageType.length > 0 ) {
			return 1;
		}
		else if ( pageType.length > 0 || other.pageType.length > 0 ) {
			return Integer.compare( pageType.length, other.pageType.length );
		}

		if ( canonicalPath.length > 0 && other.canonicalPath.length == 0 ) {
			return -1;
		}
		else if ( canonicalPath.length == 0 && other.canonicalPath.length > 0 ) {
			return 1;
		}
		else if ( canonicalPath.length > 0 || other.canonicalPath.length > 0 ) {
			return Integer.compare( canonicalPath.length, other.canonicalPath.length );
		}

		return 0;
	}
}
