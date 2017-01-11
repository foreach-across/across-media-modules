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
import com.foreach.across.modules.bootstrapui.elements.*;
import com.foreach.across.modules.entity.EntityAttributes;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.entity.registry.EntityAssociation;
import com.foreach.across.modules.entity.registry.properties.EntityPropertySelector;
import com.foreach.across.modules.entity.views.EntityFormView;
import com.foreach.across.modules.entity.views.EntityListView;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.entity.views.bootstrapui.processors.element.EntityListActionsProcessor;
import com.foreach.across.modules.entity.views.bootstrapui.util.SortableTableBuilder;
import com.foreach.across.modules.entity.views.processors.ListViewProcessorAdapter;
import com.foreach.across.modules.entity.views.processors.WebViewProcessorAdapter;
import com.foreach.across.modules.entity.views.support.EntityMessages;
import com.foreach.across.modules.entity.views.util.EntityViewElementUtils;
import com.foreach.across.modules.entity.web.WebViewCreationContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.HtmlViewElement;
import com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.WebCmsPageSection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.foreach.across.modules.entity.views.EntityFormViewFactory.FORM_RIGHT;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Configuration
@AcrossDepends(required = "EntityModule")
public class WebCmsAdminConfiguration implements EntityConfigurer
{
	private static final String CANONICAL_PATH = "canonicalPath";

	@Override
	public void configure( EntitiesConfigurationBuilder entities ) {
		entities.withType( WebCmsPage.class )
		        .properties(
				        props -> props.property( CANONICAL_PATH )
				                      .attribute( TextboxFormElement.Type.class, TextboxFormElement.Type.TEXT )
		        )
		        .listView(
				        lvb -> lvb.showProperties( CANONICAL_PATH, "title", "parent" )
				                  .defaultSort( CANONICAL_PATH )
				                  .entityQueryFilter( true )
				                  .viewProcessor( pageListViewProcessor() )
		        )
		        .createOrUpdateFormView( fvb -> fvb
				        .properties( props -> props
						        .property( "url-settings" )
						        .displayName( "URL settings" )
						        .viewElementType( ViewElementMode.FORM_WRITE, BootstrapUiElements.FIELDSET )
						        .attribute(
								        EntityAttributes.FIELDSET_PROPERTY_SELECTOR,
								        EntityPropertySelector.of( "pathSegment", "pathSegmentGenerated",
								                                   CANONICAL_PATH, "canonicalPathGenerated" )
						        )
				        )
				        .showProperties(
						        "*", "~canonicalPath", "~canonicalPathGenerated", "~pathSegment",
						        "~pathSegmentGenerated"
				        )
				        .viewProcessor( new PageFormViewProcessor() )
		        )
		        .association(
				        ab -> ab.name( "webCmsPage.parent" )
				                .listView( lvb -> lvb.showProperties( CANONICAL_PATH, "title" )
				                                     .defaultSort( CANONICAL_PATH )
				                                     .viewProcessor( pageListViewProcessor() ) )
				                .createOrUpdateFormView( fvb -> fvb.viewProcessor( new PageFormViewProcessor() ) )
		        )
		        .association(
				        ab -> ab.name( "webCmsPageSection.page" )
				                .show()
				                .associationType( EntityAssociation.Type.EMBEDDED )
				                .listView( lvb -> lvb.showProperties( "name", "sortIndex" )
				                                     .defaultSort( new Sort( "sortIndex", "name" ) ) )
		        );

		entities.withType( WebCmsPageSection.class )
		        .properties( props -> props.property( "page" ).hidden( true ) )
		        .hide();
	}

	@Bean
	PageListViewProcessor pageListViewProcessor() {
		return new PageListViewProcessor();
	}

	private static class PageListViewProcessor extends ListViewProcessorAdapter
	{
		private BootstrapUiFactory bootstrapUiFactory;

		@Override
		public void configureSortableTable( WebViewCreationContext creationContext,
		                                    EntityListView view,
		                                    SortableTableBuilder sortableTableBuilder ) {
			EntityMessages messages = view.getEntityMessages();

			sortableTableBuilder.valueRowProcessor( ( ctx, row ) -> {
				WebCmsPage page = EntityViewElementUtils.currentEntity( ctx, WebCmsPage.class );
				ContainerViewElementUtils
						.find( row, EntityListActionsProcessor.CELL_NAME, TableViewElement.Cell.class )
						.ifPresent( cell -> cell.addFirstChild(
								bootstrapUiFactory.button()
								                  .link( page.getCanonicalPath() )
								                  .attribute( "target", "_blank" )
								                  .iconOnly( new GlyphIcon( GlyphIcon.EYE_OPEN ) )
								                  .text( messages.withNameSingular( "actions.open" ) )
								                  .build( ctx ) )
						);
			} );
		}

		@Autowired
		public void setBootstrapUiFactory( BootstrapUiFactory bootstrapUiFactory ) {
			this.bootstrapUiFactory = bootstrapUiFactory;
		}
	}

	private static class PageFormViewProcessor extends WebViewProcessorAdapter<EntityFormView>
	{
		@Override
		protected void modifyViewElements( ContainerViewElement elements ) {
			addDependency( elements, "pathSegment", "pathSegmentGenerated" );
			addDependency( elements, CANONICAL_PATH, "canonicalPathGenerated" );
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

		@Override
		protected void applyCustomPostProcessing( WebViewCreationContext creationContext, EntityFormView view ) {
			WebCmsPage page = view.getEntity();

			if ( !page.isNew() ) {
				ButtonViewElement button = new ButtonViewElement();
				button.setIcon( new GlyphIcon( GlyphIcon.EYE_OPEN ) );
				button.setType( ButtonViewElement.Type.LINK );
				button.setAttribute( "target", "_blank" );
				button.setUrl( page.getCanonicalPath() );
				button.setTitle( view.getEntityMessages().withNameSingular( "actions.open" ) );
				button.addCssClass( "pull-right" );

				ContainerViewElementUtils.find( view.getViewElements(), FORM_RIGHT, ContainerViewElement.class )
				                         .ifPresent( c -> c.addFirstChild( button ) );
			}
		}
	}

}
