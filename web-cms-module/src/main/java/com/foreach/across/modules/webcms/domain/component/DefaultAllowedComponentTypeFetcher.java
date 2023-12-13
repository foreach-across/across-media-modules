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

package com.foreach.across.modules.webcms.domain.component;

import com.foreach.across.modules.webcms.data.WebCmsDataConversionService;
import com.foreach.across.modules.webcms.domain.WebCmsObject;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifier;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifierLink;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifierLinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Fallback {@link WebCmsAllowedComponentTypeFetcher}.
 * <p>
 * Fetches all linked {@link WebCmsComponentType}s if the owner is {@link com.foreach.across.modules.webcms.domain.WebCmsChildComponentRestrictable#CHILD_COMPONENT_RESTRICTED},
 * otherwise fetches all non-{@link WebCmsComponentType#COMPONENT_RESTRICTED} component types.
 *
 * @author Steven Gentens
 * @since 0.0.3
 */
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
@Component
public class DefaultAllowedComponentTypeFetcher implements WebCmsAllowedComponentTypeFetcher
{
	public static final String ALLOWED_COMPONENT_LINK = "allowed-component";

	private final WebCmsComponentTypeRepository componentTypeRepository;
	private final WebCmsDataConversionService dataConversionService;
	private final WebCmsTypeSpecifierLinkRepository typeLinkRepository;
	private final WebCmsMultiDomainService multiDomainService;

	@Override
	public boolean supports( WebCmsObject owner, WebCmsDomain domain ) {
		return true;
	}

	@Override
	public List<WebCmsComponentType> fetchComponentTypes( WebCmsObject owner, WebCmsDomain domain ) {
		if ( owner != null ) {
			WebCmsTypeSpecifier type = getType( owner );
			if ( type != null && type.isChildComponentRestricted() ) {
				return fetchLinkedTypes( type, domain );
			}
		}
		return fetchAllTypes( domain );
	}

	private List<WebCmsComponentType> fetchLinkedTypes( WebCmsTypeSpecifier type, WebCmsDomain domain ) {
		return typeLinkRepository.findAllByOwnerObjectIdAndLinkTypeOrderBySortIndexAsc(
				type.getObjectId(), ALLOWED_COMPONENT_LINK )
		                         .stream()
		                         .map( WebCmsTypeSpecifierLink::getTypeSpecifier )
		                         .map( WebCmsComponentType.class::cast )
		                         .collect( Collectors.toList() );
	}

	private List<WebCmsComponentType> fetchAllTypes( WebCmsDomain domain ) {
		WebCmsDomain domainToUse = domain;
		if ( !multiDomainService.isDomainBound( WebCmsComponentType.class ) ) {
			domainToUse = WebCmsDomain.NONE;
		}
		val query = QWebCmsComponentType.webCmsComponentType;
		return componentTypeRepository.findAll( domainToUse != null ? query.domain.eq( domain ) : query.domain.isNull() )
		                              .stream()
		                              .filter( type -> {
			                              String attributeValue = type.getAttribute( WebCmsComponentType.COMPONENT_RESTRICTED );
			                              return attributeValue == null || !(Boolean) dataConversionService.convert( attributeValue,
			                                                                                                         TypeDescriptor.valueOf( Boolean.class ) );
		                              } )
		                              .collect( Collectors.toList() );
	}

	private WebCmsTypeSpecifier getType( WebCmsObject owner ) {
		if ( owner instanceof WebCmsAsset ) {
			return ( (WebCmsAsset) owner ).getAssetType();
		}
		else if ( owner instanceof WebCmsComponent ) {
			return ( (WebCmsComponent) owner ).getComponentType();
		}
		return null;
	}

}
