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

import com.foreach.across.modules.web.thymeleaf.ThymeleafModelBuilder;
import com.foreach.across.modules.webcms.domain.component.container.ContainerWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelHierarchy;
import com.foreach.across.modules.webcms.web.thymeleaf.WebCmsComponentContentMarkerRenderer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelHierarchy.CONTAINER;

/**
 * Replaces <strong>wcm:component</strong> content markers by the actual components
 * or empty string if not present.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Slf4j
@Component
@RequiredArgsConstructor
class ComponentContentMarkerRenderer implements WebCmsComponentContentMarkerRenderer<WebCmsComponentModel>
{
	public static final String MARKER_KEY = "wcm:component";

	private final WebCmsComponentModelHierarchy componentModelHierarchy;

	@Override
	public boolean supports( WebCmsComponentModel componentModel, WebCmsContentMarker marker ) {
		return marker.hasParameters() && MARKER_KEY.equals( marker.getKey() );
	}

	@Override
	public void writeMarkerOutput( WebCmsComponentModel component, WebCmsContentMarker marker, ThymeleafModelBuilder model ) {
		String[] parameters = marker.getParameterString().split( "," );

		if ( parameters.length == 3 ) {
			Optional.ofNullable( retrieveComponentModelToRender( component, parameters[0], parameters[1], Boolean.parseBoolean( parameters[2] ) ) )
			        .ifPresent( model::addViewElement );
		}
		else {
			LOG.error( "Illegal wcm:component marker: {} - parameters could not be parsed, required parameters are (componentName,scope,searchParentScopes)",
			           marker );
		}
	}

	private WebCmsComponentModel retrieveComponentModelToRender( WebCmsComponentModel parent,
	                                                             String componentName,
	                                                             String scopeName,
	                                                             boolean searchParentScopes ) {
		WebCmsComponentModel componentToRender = null;

		if ( CONTAINER.equals( scopeName ) ) {
			if ( parent instanceof ContainerWebCmsComponentModel ) {
				componentToRender = ( (ContainerWebCmsComponentModel) parent ).getMember( componentName );
			}

			if ( componentToRender == null && searchParentScopes ) {
				componentToRender = componentModelHierarchy.getFromScope( componentName, scopeName, true );
			}
		}
		else {
			componentToRender = componentModelHierarchy.getFromScope( componentName, scopeName, searchParentScopes );
		}

		return componentToRender;
	}
}
