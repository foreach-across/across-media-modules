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

package com.foreach.across.modules.webcms.domain.component.container;

import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.bootstrapui.elements.BootstrapUiFactory;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.web.ui.elements.builder.ContainerViewElementBuilder;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.web.WebCmsComponentModelAdminRenderService;
import com.foreach.across.modules.webcms.domain.component.web.WebCmsComponentModelContentAdminRenderer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@ConditionalOnAdminUI
@Component
@RequiredArgsConstructor
public class ContainerWebCmsComponentModelAdminRenderer implements WebCmsComponentModelContentAdminRenderer<ContainerWebCmsComponentModel>
{
	private final BootstrapUiFactory bootstrapUiFactory;
	private final WebCmsComponentModelAdminRenderService adminRenderService;

	@Override
	public boolean supports( WebCmsComponentModel componentModel ) {
		return ContainerWebCmsComponentModel.class.isInstance( componentModel );
	}

	@Override
	public ViewElementBuilder createContentViewElementBuilder( ContainerWebCmsComponentModel componentModel, String controlNamePrefix ) {
		/*FieldsetFormElementBuilder fieldset = bootstrapUiFactory
				.fieldset( componentModel.getTitle() )
				.attribute( "title", componentModel.getName() );

		for ( int i = 0; i < componentModel.getMembers().size(); i++ ) {
			String scopedPrefix = controlNamePrefix + ".members[" + i + "]";
			fieldset.add( adminRenderService.createContentViewElementBuilder( componentModel.getMembers().get( i ), scopedPrefix ) );
		}

		return fieldset;*/

		ContainerViewElementBuilder fieldset = bootstrapUiFactory.container();
		for ( int i = 0; i < componentModel.getMembers().size(); i++ ) {
			String scopedPrefix = controlNamePrefix + ".members[" + i + "]";
			fieldset.add( adminRenderService.createFormElement( componentModel.getMembers().get( i ), scopedPrefix ) );
		}

		return fieldset;
	}
}
