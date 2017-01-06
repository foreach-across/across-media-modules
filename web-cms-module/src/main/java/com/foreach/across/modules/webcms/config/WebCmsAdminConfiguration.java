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

package com.foreach.across.modules.webcms.config;

import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.modules.bootstrapui.elements.BootstrapUiElements;
import com.foreach.across.modules.bootstrapui.elements.FormGroupElement;
import com.foreach.across.modules.bootstrapui.elements.TextboxFormElement;
import com.foreach.across.modules.entity.EntityAttributes;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.entity.registry.properties.EntityPropertySelector;
import com.foreach.across.modules.entity.views.EntityFormView;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.entity.views.processors.WebViewProcessorAdapter;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.HtmlViewElement;
import com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Configuration
@AcrossDepends(required = "EntityModule")
public class WebCmsAdminConfiguration implements EntityConfigurer
{
	@Override
	public void configure( EntitiesConfigurationBuilder entities ) {
		entities.withType( WebCmsPage.class )
		        .properties(
				        props -> props.property( "canonicalPath" )
				                      .attribute( TextboxFormElement.Type.class, TextboxFormElement.Type.TEXT )
		        )
		        .listView(
				        lvb -> lvb.showProperties( "canonicalPath", "title", "parent" )
				                  .defaultSort( "canonicalPath" )
		        )
		        .createOrUpdateFormView( fvb -> fvb
				        .properties( props -> props
						        .property( "url-settings" )
						        .displayName( "URL settings" )
						        .viewElementType( ViewElementMode.FORM_WRITE, BootstrapUiElements.FIELDSET )
						        .attribute(
								        EntityAttributes.FIELDSET_PROPERTY_SELECTOR,
								        EntityPropertySelector.of( "pathSegmentGenerated", "pathSegment",
								                                   "canonicalPathGenerated", "canonicalPath" )
						        )
				        )
				        .showProperties(
						        "*", "~canonicalPath", "~canonicalPathGenerated", "~pathSegment",
						        "~pathSegmentGenerated"
				        )
				        .viewProcessor( new PageFormDependsOnProcessor() )
		        );
	}

	static class PageFormDependsOnProcessor extends WebViewProcessorAdapter<EntityFormView>
	{
		@Override
		protected void modifyViewElements( ContainerViewElement elements ) {
			addDependency( elements, "pathSegment", "pathSegmentGenerated" );
			addDependency( elements, "canonicalPath", "canonicalPathGenerated" );
		}

		private void addDependency( ContainerViewElement elements, String from, String to ) {
			ContainerViewElementUtils
					.find( elements, "formGroup-" + from, FormGroupElement.class )
					.ifPresent( group -> {
						Map<String, Object> qualifiers = new HashMap<>();
						qualifiers.put( "checked", false );

						group.getControl( HtmlViewElement.class )
						     .setAttribute(
								     "data-dependson",
								     Collections.singletonMap( "[id='entity." + to + "']", qualifiers )
						     );
					} );
		}
	}

}
