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

import com.foreach.across.modules.bootstrapui.elements.BootstrapUiFactory;
import com.foreach.across.modules.entity.views.request.EntityViewCommand;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.builder.ContainerViewElementBuilder;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Component
@ConditionalOnAdminUI
@RequiredArgsConstructor
public class MenuItemsViewElementBuilder implements ViewElementBuilder<ContainerViewElement>
{
	private final WebCmsMenuRepository menuRepository;
	private final BootstrapUiFactory bootstrapUiFactory;

	@Override
	public ContainerViewElement build( ViewElementBuilderContext builderContext ) {
		EntityViewCommand command = builderContext.getAttribute( EntityViewCommand.class );
		val settings = command.getExtension( "advanced", PageFormViewProcessor.AdvancedSettings.class );

		ContainerViewElementBuilder options = bootstrapUiFactory.container();

		menuRepository.findAll( new Sort( "description", "name" ) )
		              .forEach( menu ->
				                        options.add(
						                        bootstrapUiFactory.checkbox()
						                                          .controlName( "extensions[advanced].autoCreateMenu" )
						                                          .selected( settings.getAutoCreateMenu().contains( menu ) )
						                                          .label( StringUtils.defaultIfBlank( menu.getDescription(), menu.getName() ) )
						                                          .value( menu.getId() )
				                        )
		              );

		return options.build( builderContext );
	}
}
