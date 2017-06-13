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

package com.foreach.across.modules.webcms.domain.asset.web;

import com.foreach.across.modules.web.mvc.condition.AbstractCustomRequestCondition;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint;
import com.foreach.across.modules.webcms.domain.endpoint.web.WebCmsEndpointContextResolver;
import com.foreach.across.modules.webcms.domain.endpoint.web.context.ConfigurableWebCmsEndpointContext;
import com.foreach.across.modules.webcms.domain.endpoint.web.controllers.InvalidWebCmsEndpointConditionCombination;
import com.foreach.across.modules.webcms.domain.endpoint.web.controllers.WebCmsEndpointMapping;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.Collections;

/**
 * A condition to use for retrieving the correct {@link WebCmsEndpoint}.  This condition takes all properties of
 * {@link WebCmsEndpointMapping} into account.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@RequiredArgsConstructor
@Slf4j
public class WebCmsAssetCondition extends AbstractCustomRequestCondition<WebCmsAssetCondition>
{
	private final ConfigurableWebCmsEndpointContext context;
	private final WebCmsEndpointContextResolver resolver;

	private Class<?> assetType;

	/**
	 * Set the values for this condition based on the attributes of the annotated element.
	 *
	 * @param annotatedElement this condition is attached to
	 */
	@Override
	public void setAnnotatedElement( AnnotatedElement annotatedElement ) {
		WebCmsAssetMapping endpointMapping = AnnotatedElementUtils.findMergedAnnotation( annotatedElement, WebCmsAssetMapping.class );

		assetType = endpointMapping.value();
	}

	@Override
	protected Collection<?> getContent() {
		return Collections.singleton( assetType );
	}

	@Override
	protected String getToStringInfix() {
		return " && ";
	}

	@Override
	public WebCmsAssetCondition combine( WebCmsAssetCondition other ) {
		WebCmsAssetCondition result = new WebCmsAssetCondition( this.context, this.resolver );
		if ( this.assetType.isAssignableFrom( other.assetType ) ) {
			result.assetType = other.assetType;
		}
		else if ( !other.assetType.isAssignableFrom( this.assetType ) ) {
			String message = String.format( "A condition with asset type %s and type %s cannot be merged", this.assetType,
			                                other.assetType );
			throw new InvalidWebCmsEndpointConditionCombination( message );
		}
		else {
			result.assetType = this.assetType;
		}
		return result;
	}

	@Override
	public WebCmsAssetCondition getMatchingCondition( HttpServletRequest request ) {
		if ( !context.isResolved() ) {
			resolver.resolve( context, request );
		}
		if ( context.isOfType( WebCmsAssetEndpoint.class ) && assetType.isInstance( context.getEndpoint( WebCmsAssetEndpoint.class ).getAsset() ) ) {
			WebCmsAssetCondition result = new WebCmsAssetCondition( context, resolver );
			result.assetType = this.assetType;
			LOG.trace( "Matching condition is {}", result );
			return result;
		}
		return null;
	}

	@Override
	public int compareTo( WebCmsAssetCondition other, HttpServletRequest request ) {
		if ( assetType != null && other.assetType != null && !assetType.equals( other.assetType ) ) {
			return assetType.isAssignableFrom( other.assetType ) ? 1 : -1;
		}
		return 0;
	}
}
