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

import com.foreach.across.modules.bootstrapui.elements.*;
import com.foreach.across.modules.entity.EntityAttributes;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.entity.registry.properties.EntityPropertySelector;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.entity.views.bootstrapui.processors.element.EntityListActionsProcessor;
import com.foreach.across.modules.entity.views.bootstrapui.util.SortableTableBuilder;
import com.foreach.across.modules.entity.views.processors.EntityViewProcessorAdapter;
import com.foreach.across.modules.entity.views.processors.SortableTableRenderingViewProcessor;
import com.foreach.across.modules.entity.views.processors.support.ViewElementBuilderMap;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.entity.views.support.EntityMessages;
import com.foreach.across.modules.entity.views.util.EntityViewElementUtils;
import com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.component.config.WebCmsObjectComponentViewsConfiguration;
import com.foreach.across.modules.webcms.domain.component.web.SearchComponentViewProcessor;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpointService;
import com.foreach.across.modules.webcms.domain.menu.config.WebCmsAssetMenuViewsConfiguration;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.web.MenuItemsViewElementBuilder;
import com.foreach.across.modules.webcms.domain.page.web.PageFormViewProcessor;
import com.foreach.across.modules.webcms.domain.redirect.WebCmsRemoteEndpoint;
import com.foreach.across.modules.webcms.domain.url.config.WebCmsAssetUrlConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Configuration
@RequiredArgsConstructor
public class WebCmsPageConfiguration
{
	public static final String PATH_SEGMENT = "pathSegment";
	public static final String CANONICAL_PATH = "canonicalPath";
	private static final String PAGE_TYPE = "pageType";
	private static final String PUBLISH_SETTINGS = "publish-settings";

	@Autowired
	void enableUrls( WebCmsAssetUrlConfiguration urlConfiguration ) {
		urlConfiguration.enable( WebCmsPage.class );
	}

	@ConditionalOnAdminUI
	@Configuration
	@RequiredArgsConstructor
	static class PageAdminUi implements EntityConfigurer
	{
		private final PageFormViewProcessor pageFormViewProcessor;
		private final MenuItemsViewElementBuilder menuItemsViewElementBuilder;

		@Autowired
		void enableComponents( WebCmsObjectComponentViewsConfiguration componentViewsConfiguration ) {
			componentViewsConfiguration.enable( WebCmsPage.class );
		}

		@Autowired
		void enableMenuItems( WebCmsAssetMenuViewsConfiguration menuViewsConfiguration ) {
			menuViewsConfiguration.enable( WebCmsPage.class );
		}

		@Override
		public void configure( EntitiesConfigurationBuilder entities ) {
			entities.withType( WebCmsRemoteEndpoint.class )
			        .association( ab -> ab.name( "webCmsRemoteEndpoint.urls" ).show() );

			entities.withType( WebCmsPage.class )
			        .attribute(
					        SearchComponentViewProcessor.COMPONENT_SEARCH_QUERY,
					        "title like '%{0}%'"
			        )
			        .properties(
					        props -> props.property( "objectId" ).hidden( true ).and()
					                      .property( CANONICAL_PATH )
					                      .attribute( TextboxFormElement.Type.class, TextboxFormElement.Type.TEXT )
			        )
			        .listView(
					        lvb -> lvb.showProperties( CANONICAL_PATH, PAGE_TYPE, "title", "parent" )
					                  .defaultSort( CANONICAL_PATH )
					                  .entityQueryFilter( true )
					                  .viewProcessor( pageListViewProcessor() )
			        )
			        .updateFormView( fvb -> fvb
					        .properties( props -> props
							        .property( PAGE_TYPE ).readable( true ).writable( false ).order( 0 )
							        .and().property( PUBLISH_SETTINGS )
							        .attribute(
									        EntityAttributes.FIELDSET_PROPERTY_SELECTOR,
									        EntityPropertySelector.of( "published", "publicationDate", "menu-items" )
							        )
					        ) )
			        .createFormView( fvb -> fvb
					        .properties( props -> props.property( PUBLISH_SETTINGS )
					                                   .and().property( PAGE_TYPE ).order( 0 ) ) )
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
							        .and()
							        .property( "menu-items" )
							        .displayName( "Auto-create menu items" )
							        .writable( true )
							        .readable( false )
							        .hidden( true )
							        .viewElementBuilder( ViewElementMode.CONTROL, menuItemsViewElementBuilder )

					        )
					        .showProperties(
							        "*", "~canonicalPath", "~canonicalPathGenerated", "~pathSegment", "~pathSegmentGenerated"
					        )
					        .viewProcessor( pageFormViewProcessor )
			        )
			        .association(
					        ab -> ab.name( "webCmsPage.parent" )
					                .listView( lvb -> lvb.showProperties( CANONICAL_PATH, "title" )
					                                     .defaultSort( CANONICAL_PATH )
					                                     .viewProcessor( pageListViewProcessor() ) )
					                .createOrUpdateFormView( fvb -> fvb.viewProcessor( pageFormViewProcessor ) ) );
			;

		}

		@ConditionalOnAdminUI
		@Bean
		PageListViewProcessor pageListViewProcessor() {
			return new PageListViewProcessor();
		}
	}

	private static class PageListViewProcessor extends EntityViewProcessorAdapter
	{
		private BootstrapUiFactory bootstrapUiFactory;
		private WebCmsEndpointService endpointService;

		@Override
		protected void createViewElementBuilders( EntityViewRequest entityViewRequest, EntityView entityView, ViewElementBuilderMap builderMap ) {
			builderMap.get( SortableTableRenderingViewProcessor.TABLE_BUILDER, SortableTableBuilder.class )
			          .valueRowProcessor( ( ctx, row ) -> {
				          WebCmsPage page = EntityViewElementUtils.currentEntity( ctx, WebCmsPage.class );
				          ContainerViewElementUtils
						          .find( row, EntityListActionsProcessor.CELL_NAME, TableViewElement.Cell.class )
						          .ifPresent( cell ->
								                      endpointService
										                      .buildPreviewUrl( page )
										                      .ifPresent( previewUrl -> {
											                      EntityMessages entityMessages = entityViewRequest.getEntityViewContext().getEntityMessages();
											                      cell.addFirstChild(
													                      bootstrapUiFactory.button()
													                                        .link( previewUrl )
													                                        .attribute( "target", "_blank" )
													                                        .iconOnly( new GlyphIcon( GlyphIcon.EYE_OPEN ) )
													                                        .text( entityMessages.withNameSingular( "actions.open" ) )
													                                        .build( ctx ) );
										                      } )
						          );
			          } );
		}

		@Autowired
		public void setBootstrapUiFactory( BootstrapUiFactory bootstrapUiFactory ) {
			this.bootstrapUiFactory = bootstrapUiFactory;
		}

		@Autowired
		public void setEndpointService( WebCmsEndpointService endpointService ) {
			this.endpointService = endpointService;
		}

	}
}
