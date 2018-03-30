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

package com.foreach.across.modules.webcms.domain.component.text;

import com.foreach.across.core.annotations.ConditionalOnAcrossModule;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.bootstrapui.elements.BootstrapUiBuilders;
import com.foreach.across.modules.entity.support.EntityMessageCodeResolver;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.web.WebCmsComponentModelContentAdminRenderer;
import com.foreach.across.modules.webcms.web.TextWebCmsComponentAdminResources;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import static com.foreach.across.modules.webcms.domain.component.web.WebCmsComponentModelFormElementBuilder.COMPONENT_MESSAGE_CODE_PREFIX;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@ConditionalOnAcrossModule(AdminWebModule.NAME)
@Component
@RequiredArgsConstructor
public class TextWebCmsComponentModelAdminRenderer implements WebCmsComponentModelContentAdminRenderer<TextWebCmsComponentModel>
{
	@Override
	public boolean supports( WebCmsComponentModel componentModel ) {
		return TextWebCmsComponentModel.class.isInstance( componentModel );
	}

	@Override
	public ViewElementBuilder createContentViewElementBuilder( TextWebCmsComponentModel componentModel, String controlNamePrefix ) {
		return BootstrapUiBuilders.textbox()
		                          .controlName( controlNamePrefix + ".content" )
		                          .rows(
				                          Integer.parseInt( componentModel.getComponentType()
				                                                          .getAttribute( TextWebCmsComponentModel.Attributes.ROWS, "3" ) )
		                          )
		                          .multiLine( componentModel.isMultiLine() )
		                          .text( componentModel.getContent() )
		                          .attribute( "data-wcm-component-type", componentModel.getComponentType().getTypeKey() )
		                          .attribute( "data-wcm-markup-type", componentModel.getMarkupType().asAttributeValue() )
		                          .attribute( "data-wcm-profile", componentModel.getProfile() )
		                          .postProcessor( ( ( builderContext, element ) -> {
			                          builderContext.getAttribute( WebResourceRegistry.class ).addPackage( TextWebCmsComponentAdminResources.NAME );

			                          EntityMessageCodeResolver codeResolver = builderContext.getAttribute( EntityMessageCodeResolver.class );
			                          if ( codeResolver != null ) {
				                          element.setPlaceholder(
						                          codeResolver.getMessageWithFallback(
								                          builderContext.getAttribute( COMPONENT_MESSAGE_CODE_PREFIX, String.class ) + ".content[placeholder]",
								                          StringUtils.defaultString( element.getPlaceholder() )
						                          )
				                          );
			                          }
		                          } ) );
	}
}
