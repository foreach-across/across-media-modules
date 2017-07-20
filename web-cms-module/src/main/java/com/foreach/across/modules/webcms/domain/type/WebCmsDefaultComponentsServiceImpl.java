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
import com.foreach.across.modules.webcms.domain.component.WebCmsContentMarkerService;
import com.foreach.across.modules.webcms.domain.component.container.ContainerWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import com.foreach.across.modules.webcms.domain.component.text.TextWebCmsComponentModel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Default implementation of {@link WebCmsDefaultComponentsService}.
 *
 * @author Raf Ceuls
 * @since 0.0.2
 */
@Service
@RequiredArgsConstructor
final class WebCmsDefaultComponentsServiceImpl implements WebCmsDefaultComponentsService
{
	private static final String DEFAULT_CONTENT_COMPONENT = "content";

	private final WebCmsComponentModelService componentModelService;
	private final WebCmsTypeSpecifierRepository typeSpecifierRepository;
	private final WebCmsContentMarkerService contentMarkerService;

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
				template = template.asComponentTemplate();
				template.setOwner( asset );
				replaceAttributesInTextComponents( asset, template, markerValues );
				componentModelService.save( template );
			}
		}
	}

	private void replaceAttributesInTextComponents( WebCmsObject asset,
	                                                WebCmsComponentModel componentModel,
	                                                Map<String, String> markerValues ) {
		if ( componentModel instanceof TextWebCmsComponentModel ) {
			TextWebCmsComponentModel text = (TextWebCmsComponentModel) componentModel;
			text.setContent( contentMarkerService.replaceMarkers( text.getContent(), markerValues ) );
		}
		else if ( componentModel instanceof ContainerWebCmsComponentModel ) {
			( (ContainerWebCmsComponentModel) componentModel ).getMembers().forEach( m -> replaceAttributesInTextComponents( asset, m, markerValues ) );
		}
	}

	private WebCmsComponentModel retrieveContentTemplate( WebCmsTypeSpecifier<?> cmsTypeSpecifier ) {
		String contentTemplateName = StringUtils.defaultString( cmsTypeSpecifier.getAttribute( "contentTemplate" ), DEFAULT_CONTENT_COMPONENT );
		WebCmsComponentModel model = componentModelService.getComponentModelByName( contentTemplateName, cmsTypeSpecifier );

		if ( model == null && cmsTypeSpecifier.hasAttribute( "parent" ) ) {
			WebCmsTypeSpecifier<?> parentCmsType = typeSpecifierRepository.findOneByObjectTypeAndTypeKey( cmsTypeSpecifier.getTypeKey(),
			                                                                                              cmsTypeSpecifier.getAttribute( "parent" ) );
			if ( parentCmsType != null ) {
				return retrieveContentTemplate( parentCmsType );
			}
		}

		return model;
	}
}
