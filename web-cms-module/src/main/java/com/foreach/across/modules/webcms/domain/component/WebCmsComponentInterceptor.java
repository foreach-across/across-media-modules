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

import com.foreach.across.modules.hibernate.aop.EntityInterceptorAdapter;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Component
@RequiredArgsConstructor
class WebCmsComponentInterceptor extends EntityInterceptorAdapter<WebCmsComponent>
{
	private static final String TEMPLATE_COMPONENT = "componentTemplate";

	private final WebCmsComponentModelService componentModelService;
	private final WebCmsContentMarkerService contentMarkerService;

	@Override
	public boolean handles( Class<?> aClass ) {
		return WebCmsComponent.class.isAssignableFrom( aClass );
	}

	@Override
	public void beforeCreate( WebCmsComponent component ) {
		updateBodyContainsMarkers( component );
	}

	@Override
	public void beforeUpdate( WebCmsComponent component ) {
		updateBodyContainsMarkers( component );
	}

	private void updateBodyContainsMarkers( WebCmsComponent component ) {
		if ( contentMarkerService.containsMarkers( component.getBody() ) ) {
			component.setBodyWithContentMarkers( true );
		}
	}

	@Override
	public void afterCreate( WebCmsComponent component ) {
		WebCmsComponentModel model = componentModelService.getComponentModelByName( TEMPLATE_COMPONENT, component.getComponentType() );

		if ( model != null ) {
			WebCmsComponentModel template = model.asComponentTemplate();
			template.setComponent( component );

			componentModelService.save( template );
		}
	}
}
