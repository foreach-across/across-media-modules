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

package com.foreach.across.modules.webcms.domain.type;

import com.foreach.across.modules.webcms.domain.WebCmsObject;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.WebCmsContentMarkerService;
import com.foreach.across.modules.webcms.domain.component.container.ContainerWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import com.foreach.across.modules.webcms.domain.component.text.TextWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainBound;
import com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Default implementation of {@link WebCmsDefaultComponentsService}.
 *
 * @author Raf Ceuls
 * @since 0.0.2
 */
@Slf4j
@Service
@RequiredArgsConstructor
final class WebCmsDefaultComponentsServiceImpl implements WebCmsDefaultComponentsService
{
	private static final String DEFAULT_CONTENT_TEMPLATE_COMPONENT = "contentTemplate";

	private final WebCmsComponentModelService componentModelService;
	private final WebCmsContentMarkerService contentMarkerService;
	private final WebCmsMultiDomainService multiDomainService;
	private final WebCmsTypeSpecifierService typeSpecifierService;

	/**
	 * Accepts a single {@link WebCmsObject}. If no {@link WebCmsTypeSpecifier} is specified, nothing happens. Otherwise
	 * retrieves the content template from said {@link WebCmsTypeSpecifier} and injects the values into the relevant parts of the {@link WebCmsObject}.
	 * Any marker values specified in it's respective parameter are used to update the components as they occur in any
	 * {@link com.foreach.across.modules.webcms.domain.component.WebCmsComponent} that is encountered.
	 *
	 * @param asset        The asset that will receive the generated data.
	 * @param markerValues Any marker values, specified as a map of the format ('@@marker@@', 'replace value')
	 */
	@Override
	public void createDefaultComponents( WebCmsAsset<?> asset,
	                                     Map<String, String> markerValues ) {
		this.createDefaultComponents( asset, asset.getAssetType(), markerValues );
	}

	/**
	 * Accepts a single {@link WebCmsObject}. If no {@link WebCmsTypeSpecifier} is specified, nothing happens. Otherwise
	 * retrieves the content template from said {@link WebCmsTypeSpecifier} and injects the values into the relevant parts of the {@link WebCmsObject}.
	 * Any marker values specified in it's respective parameter are used to update the components as they occur in any
	 * {@link com.foreach.across.modules.webcms.domain.component.WebCmsComponent} that is encountered.
	 *
	 * @param asset         The asset that will receive the generated data.
	 * @param typeSpecifier The type of the asset
	 * @param markerValues  Any marker values, specified as a map of the format ('@@marker@@', 'replace value')
	 */
	@Override
	public void createDefaultComponents( WebCmsObject asset, WebCmsTypeSpecifier<?> typeSpecifier, Map<String, String> markerValues ) {
		if ( typeSpecifier != null ) {
			WebCmsComponentModel template = retrieveContentTemplate( typeSpecifier );

			if ( template != null ) {
				if ( template instanceof ContainerWebCmsComponentModel ) {
					LOG.trace( "Copying members of contentTemplate {} to {}", template, asset );
					ContainerWebCmsComponentModel container = (ContainerWebCmsComponentModel) template;
					List<WebCmsComponentModel> members = container.getMembers();
					members.forEach( m -> {
						WebCmsComponentModel clone = m.asComponentTemplate();
						clone.setOwner( asset );
						if ( asset instanceof WebCmsDomainBound ) {
							clone.setDomain( ( (WebCmsDomainBound) asset ).getDomain() );
						}
						replaceAttributesInTextComponents( clone, markerValues );
						componentModelService.save( clone );
					} );
				}
				else {
					LOG.warn( "A contentTemplate was found but was not of type ContainerWebCmsComponentModel. " +
							          "Only container models are supported as the members of the container will be copied to the asset." +
							          "Ignoring the content template." );
				}
			}
		}
	}

	private void replaceAttributesInTextComponents( WebCmsComponentModel componentModel, Map<String, String> markerValues ) {
		if ( componentModel instanceof TextWebCmsComponentModel ) {
			TextWebCmsComponentModel text = (TextWebCmsComponentModel) componentModel;
			text.setContent( contentMarkerService.replaceMarkers( text.getContent(), markerValues ) );
		}
		else if ( componentModel instanceof ContainerWebCmsComponentModel ) {
			ContainerWebCmsComponentModel container = (ContainerWebCmsComponentModel) componentModel;
			if ( container.hasMarkup() ) {
				container.setMarkup( contentMarkerService.replaceMarkers( container.getMarkup(), markerValues ) );
			}
			container.getMembers().forEach( m -> replaceAttributesInTextComponents( m, markerValues ) );
		}
	}

	private WebCmsComponentModel retrieveContentTemplate( WebCmsTypeSpecifier<?> cmsTypeSpecifier ) {
		String contentTemplateName = StringUtils.defaultString( cmsTypeSpecifier.getAttribute( "contentTemplate" ), DEFAULT_CONTENT_TEMPLATE_COMPONENT );

		WebCmsDomain currentDomain = multiDomainService.getCurrentDomainForType( WebCmsComponent.class );
		WebCmsDomain typeDomain = cmsTypeSpecifier.getDomain();

		WebCmsComponentModel template = componentModelService.getComponentModelByNameAndDomain( contentTemplateName, cmsTypeSpecifier, currentDomain );

		if ( template == null && !Objects.equals( currentDomain, typeDomain ) ) {
			template = componentModelService.getComponentModelByNameAndDomain( contentTemplateName, cmsTypeSpecifier, typeDomain );
		}

		if ( template == null && cmsTypeSpecifier.hasAttribute( "parent" ) ) {
			WebCmsTypeSpecifier<?> parentCmsType = typeSpecifierService.getTypeSpecifierByKey( cmsTypeSpecifier.getAttribute( "parent" ), cmsTypeSpecifier.getClass() );
			if ( parentCmsType != null ) {
				return retrieveContentTemplate( parentCmsType );
			}
		}

		return template;
	}
}