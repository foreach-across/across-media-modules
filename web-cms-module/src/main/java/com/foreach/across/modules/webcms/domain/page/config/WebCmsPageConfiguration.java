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

package com.foreach.across.modules.webcms.domain.page.config;

import com.foreach.across.modules.adminweb.ui.PageContentStructure;
import com.foreach.across.modules.bootstrapui.elements.*;
import com.foreach.across.modules.entity.EntityAttributes;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.entity.registry.EntityAssociation;
import com.foreach.across.modules.entity.registry.properties.EntityPropertySelector;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.entity.views.bootstrapui.processors.element.EntityListActionsProcessor;
import com.foreach.across.modules.entity.views.bootstrapui.util.SortableTableBuilder;
import com.foreach.across.modules.entity.views.context.EntityViewContext;
import com.foreach.across.modules.entity.views.processors.EntityViewProcessorAdapter;
import com.foreach.across.modules.entity.views.processors.SortableTableRenderingViewProcessor;
import com.foreach.across.modules.entity.views.processors.support.ViewElementBuilderMap;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.entity.views.support.EntityMessages;
import com.foreach.across.modules.entity.views.util.EntityViewElementUtils;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.HtmlViewElement;
import com.foreach.across.modules.web.ui.elements.TextViewElement;
import com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.component.config.WebCmsObjectComponentViewsConfiguration;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.redirect.WebCmsRemoteEndpoint;
import com.foreach.across.modules.webcms.domain.url.config.WebCmsAssetUrlConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Configuration
@RequiredArgsConstructor
class WebCmsPageConfiguration
{
	private static final String CANONICAL_PATH = "canonicalPath";

	@Autowired
	void enableUrls( WebCmsAssetUrlConfiguration urlConfiguration ) {
		urlConfiguration.enable( WebCmsPage.class );
	}

	@ConditionalOnAdminUI
	@Configuration
	static class AdminUi implements EntityConfigurer
	{
		@Autowired
		void enableComponents( WebCmsObjectComponentViewsConfiguration componentViewsConfiguration ) {
			componentViewsConfiguration.enable( WebCmsPage.class );
		}

		@Override
		public void configure( EntitiesConfigurationBuilder entities ) {
			entities.withType( WebCmsRemoteEndpoint.class )
			        .association( ab -> ab.name( "webCmsRemoteEndpoint.urls" ).show() );

			entities.withType( WebCmsPage.class )
			        .properties(
					        props -> props.property( "objectId" ).hidden( true ).and()
					                      .property( CANONICAL_PATH )
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
					        .viewProcessor( pageFormViewProcessor() )
			        )
			        .association(
					        ab -> ab.name( "webCmsPage.parent" )
					                .listView( lvb -> lvb.showProperties( CANONICAL_PATH, "title" )
					                                     .defaultSort( CANONICAL_PATH )
					                                     .viewProcessor( pageListViewProcessor() ) )
					                .createOrUpdateFormView( fvb -> fvb.viewProcessor( pageFormViewProcessor() ) )
			        )
			        .association(
					        ab -> ab.name( "webCmsMenuItem.linkedPage" )
					                .show()
					                .associationType( EntityAssociation.Type.EMBEDDED )
			        );
		}

		@ConditionalOnAdminUI
		@Bean
		PageListViewProcessor pageListViewProcessor() {
			return new PageListViewProcessor();
		}

		@ConditionalOnAdminUI
		@Bean
		PageFormViewProcessor pageFormViewProcessor() {
			return new PageFormViewProcessor();
		}
	}

	private static class PageListViewProcessor extends EntityViewProcessorAdapter
	{
		private BootstrapUiFactory bootstrapUiFactory;

		@Override
		protected void createViewElementBuilders( EntityViewRequest entityViewRequest, EntityView entityView, ViewElementBuilderMap builderMap ) {
			builderMap.get( SortableTableRenderingViewProcessor.TABLE_BUILDER, SortableTableBuilder.class )
			          .valueRowProcessor( ( ctx, row ) -> {
				          WebCmsPage page = EntityViewElementUtils.currentEntity( ctx, WebCmsPage.class );
				          ContainerViewElementUtils
						          .find( row, EntityListActionsProcessor.CELL_NAME, TableViewElement.Cell.class )
						          .ifPresent( cell -> {
							                      EntityMessages entityMessages = entityViewRequest.getEntityViewContext().getEntityMessages();
							                      cell.addFirstChild(
									                      bootstrapUiFactory.button()
									                                        .link( page.getCanonicalPath() )
									                                        .attribute( "target", "_blank" )
									                                        .iconOnly( new GlyphIcon( GlyphIcon.EYE_OPEN ) )
									                                        .text( entityMessages.withNameSingular( "actions.open" ) )
									                                        .build( ctx ) );
						                      }
						          );
			          } );
		}

		@Autowired
		public void setBootstrapUiFactory( BootstrapUiFactory bootstrapUiFactory ) {
			this.bootstrapUiFactory = bootstrapUiFactory;
		}
	}

	private static class PageFormViewProcessor extends EntityViewProcessorAdapter
	{
		@Override
		protected void postRender( EntityViewRequest entityViewRequest,
		                           EntityView entityView,
		                           ContainerViewElement container,
		                           ViewElementBuilderContext builderContext ) {
			addDependency( container, "pathSegment", "pathSegmentGenerated" );
			addDependency( container, CANONICAL_PATH, "canonicalPathGenerated" );

			EntityViewContext viewContext = entityViewRequest.getEntityViewContext();

			if ( viewContext.holdsEntity() ) {
				WebCmsPage page = viewContext.getEntity( WebCmsPage.class );

				LinkViewElement openLink = new LinkViewElement();
				openLink.setAttribute( "target", "_blank" );
				openLink.setUrl( page.getCanonicalPath() );
				openLink.setTitle( viewContext.getEntityMessages().withNameSingular( "actions.open" ) );
				openLink.addChild( new GlyphIcon( GlyphIcon.EYE_OPEN ) );

				PageContentStructure adminPage = entityViewRequest.getPageContentStructure();
				adminPage.addToPageTitleSubText( TextViewElement.html( "&nbsp;" ) );
				adminPage.addToPageTitleSubText( openLink );
			}
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
