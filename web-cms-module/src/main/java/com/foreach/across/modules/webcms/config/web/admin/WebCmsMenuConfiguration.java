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

package com.foreach.across.modules.webcms.config.web.admin;

import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.modules.bootstrapui.elements.TextboxFormElement;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.entity.registry.EntityAssociation;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenu;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenuItem;
import org.springframework.context.annotation.Configuration;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Configuration
@AcrossDepends(required = { "EntityModule", "AdminWebModule" })
public class WebCmsMenuConfiguration implements EntityConfigurer
{
	@Override
	public void configure( EntitiesConfigurationBuilder entities ) {
		entities.withType( WebCmsMenuItem.class )
		        .properties( props -> props.property( "url" ).attribute( TextboxFormElement.Type.class, TextboxFormElement.Type.TEXT ) )
		        .hide();

		entities.withType( WebCmsMenu.class )
		        .association(
				        ab -> ab.name( "webCmsMenuItem.menu" )
				                .show()
				                .associationType( EntityAssociation.Type.EMBEDDED )
				                .listView( lvb -> lvb.showProperties( "path", "group", "title", "linkedPage", "sortIndex" ) )
				                .createOrUpdateFormView(
						                fvb -> fvb.showProperties( "group", "*", "~menu" )
				                )
		        );
	}
}
