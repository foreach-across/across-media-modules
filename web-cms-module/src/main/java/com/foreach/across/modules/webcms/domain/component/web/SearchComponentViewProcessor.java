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

package com.foreach.across.modules.webcms.domain.component.web;

import com.foreach.across.modules.bootstrapui.elements.BootstrapUiBuilders;
import com.foreach.across.modules.bootstrapui.elements.builder.TableViewElementBuilder;
import com.foreach.across.modules.entity.query.EntityQuery;
import com.foreach.across.modules.entity.query.EntityQueryExecutor;
import com.foreach.across.modules.entity.query.EntityQueryParser;
import com.foreach.across.modules.entity.registry.EntityConfiguration;
import com.foreach.across.modules.entity.registry.EntityRegistry;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.EntityViewElementBuilderHelper;
import com.foreach.across.modules.entity.views.bootstrapui.util.SortableTableBuilder;
import com.foreach.across.modules.entity.views.processors.AbstractEntityFetchingViewProcessor;
import com.foreach.across.modules.entity.views.processors.EntityViewProcessorAdapter;
import com.foreach.across.modules.entity.views.processors.ListFormViewProcessor;
import com.foreach.across.modules.entity.views.processors.SortableTableRenderingViewProcessor;
import com.foreach.across.modules.entity.views.processors.support.ViewElementBuilderMap;
import com.foreach.across.modules.entity.views.request.EntityViewCommand;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.entity.views.util.EntityViewElementUtils;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.builder.ContainerViewElementBuilderSupport;
import com.foreach.across.modules.web.ui.elements.builder.NodeViewElementBuilder;
import com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.WebCmsObject;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetRepository;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentRepository;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentUtils;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifierRepository;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collections;

import static com.foreach.across.modules.webcms.config.icons.WebCmsIcons.webCmsIcons;

/**
 * This processor is a work in progress.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@ConditionalOnAdminUI
@Component
@RequiredArgsConstructor
public final class SearchComponentViewProcessor extends EntityViewProcessorAdapter
{
	public static final String COMPONENT_SEARCH_QUERY = SearchComponentViewProcessor.class.getName() + ".SEARCH_QUERY";

	private final WebCmsComponentRepository componentRepository;
	private final WebCmsTypeSpecifierRepository typeSpecifierRepository;
	private final WebCmsAssetRepository assetRepository;
	private final EntityRegistry entityRegistry;
	private final EntityViewElementBuilderHelper builderHelper;

	@Override
	protected void doGet( EntityViewRequest entityViewRequest, EntityView entityView, EntityViewCommand command ) {
		val request = entityViewRequest.getWebRequest();
		String parent = request.getParameter( "parent" );

		if ( parent != null ) {
			entityView.addAttribute( AbstractEntityFetchingViewProcessor.DEFAULT_ATTRIBUTE_NAME,
			                         componentRepository.findAllByOwnerObjectIdAndDomainOrderBySortIndexAsc( parent, WebCmsDomain.NONE ) );
		}
		else {
			entityView.addAttribute( AbstractEntityFetchingViewProcessor.DEFAULT_ATTRIBUTE_NAME, Collections.emptyList() );
		}
	}

	@Override
	protected void createViewElementBuilders( EntityViewRequest entityViewRequest, EntityView entityView, ViewElementBuilderMap builderMap ) {
		val request = entityViewRequest.getWebRequest();
		String parent = request.getParameter( "parent" );
		String filterType = request.getParameter( "filter" );

		if ( parent == null ) {
			builderMap.remove( SortableTableRenderingViewProcessor.TABLE_BUILDER );

			val pills = BootstrapUiBuilders.node( "ul" )
			                               .css( "nav", "nav-pills" );

			entityRegistry.getEntities()
			              .stream()
			              .filter( entityConfiguration -> entityConfiguration.hasAttribute( COMPONENT_SEARCH_QUERY ) )
			              .forEach( entityConfiguration -> {
				              String url = ServletUriComponentsBuilder.fromCurrentRequest()
				                                                      .replaceQueryParam( "filter", entityConfiguration.getName() )
				                                                      .toUriString();

				              pills.add( BootstrapUiBuilders.node( "li" )
				                                            .css( entityConfiguration.getName().equals( filterType ) ? "active" : null )
				                                            .add(
						                                            BootstrapUiBuilders.link()
						                                                               .url( url )
						                                                               .text( entityConfiguration.getEntityMessageCodeResolver()
						                                                                                         .getNameSingular() ) ) );
			              } );

			builderMap.put(
					"filter",
					BootstrapUiBuilders.container()
					                   .name( "filter" )
					                   .add( BootstrapUiBuilders.hidden().controlName( "filter" ).value( filterType ) )
					                   .add( pills )
					                   .add(
							                   BootstrapUiBuilders
									                   .inputGroup(
											                   BootstrapUiBuilders.textbox()
											                                      .controlName( "qs" )
											                                      .text( request.getParameter( "qs" ) )
									                   )
									                   .append(
											                   BootstrapUiBuilders.button()
											                                      .submit()
											                                      .iconOnly( webCmsIcons.component.search() )
									                   )
					                   )
			);
		}
		else {
			val sortableTableBuilder = builderMap.get( SortableTableRenderingViewProcessor.TABLE_BUILDER, SortableTableBuilder.class );
			sortableTableBuilder.tableOnly();

			registerOpenButtonOnRow( sortableTableBuilder );
		}
	}

	@Override
	protected void render( EntityViewRequest entityViewRequest,
	                       EntityView entityView,
	                       ContainerViewElementBuilderSupport<?, ?> containerBuilder,
	                       ViewElementBuilderMap builderMap,
	                       ViewElementBuilderContext builderContext ) {
		val request = entityViewRequest.getWebRequest();
		String parent = request.getParameter( "parent" );

		val breadcrumb = BootstrapUiBuilders.node( "ol" )
		                                    .css( "breadcrumb", "wcm-component-search-trail" );

		addComponentToBreadcrumb( parent, breadcrumb, false );
		breadcrumb.addFirst(
				BootstrapUiBuilders.node( "li" )
				                   .add(
						                   parent != null
								                   ? BootstrapUiBuilders.link()
								                                        .url(
										                                        ServletUriComponentsBuilder.fromCurrentRequest()
										                                                                   .replaceQueryParam( "parent" )
										                                                                   .toUriString()
								                                        )
								                                        .text( "Search components" )
								                   : BootstrapUiBuilders.text( "Search components" )
				                   )
		);

		containerBuilder.addFirst( breadcrumb );

		if ( builderMap.containsKey( "filter" ) ) {
			containerBuilder.add( builderMap.get( "filter" ) );
		}

		String filterType = request.getParameter( "filter" );
		String qs = StringUtils.defaultString( request.getParameter( "qs" ) );

		if ( filterType != null && parent == null ) {
			EntityConfiguration configuration = entityRegistry.getEntityConfiguration( filterType );
			EntityQueryParser entityQueryParser = configuration.getAttribute( EntityQueryParser.class );
			EntityQueryExecutor entityQueryExecutor = configuration.getAttribute( EntityQueryExecutor.class );

			String baseQuery = configuration.getAttribute( SearchComponentViewProcessor.COMPONENT_SEARCH_QUERY, String.class );
			EntityQuery query = entityQueryParser.parse( baseQuery.replace( "{0}", qs ) );

			SortableTableBuilder tableBuilder = builderHelper.createSortableTableBuilder();
			tableBuilder.entityConfiguration( configuration );
			tableBuilder.properties( "title" );
			tableBuilder.items( entityQueryExecutor.findAll( query ) );
			tableBuilder.tableOnly( true );
			tableBuilder.showResultNumber( false );
			tableBuilder.noSorting();

			registerOpenButtonOnRow( tableBuilder );

			containerBuilder.add( tableBuilder );
		}
	}

	@Override
	protected void postRender( EntityViewRequest entityViewRequest,
	                           EntityView entityView,
	                           ContainerViewElement container,
	                           ViewElementBuilderContext builderContext ) {

		ContainerViewElementUtils.move( container, "filter", ListFormViewProcessor.DEFAULT_FORM_NAME + "-header" );
	}

	private void registerOpenButtonOnRow( SortableTableBuilder sortableTableBuilder ) {
		TableViewElementBuilder table = BootstrapUiBuilders.table();

		sortableTableBuilder.headerRowProcessor( ( ( viewElementBuilderContext, row ) -> {
			row.addFirstChild( table.heading().build( viewElementBuilderContext ) );
			row.addChild( table.heading().build( viewElementBuilderContext ) );
		} ) );

		sortableTableBuilder.valueRowProcessor( ( viewElementBuilderContext, row ) -> {
			WebCmsObject object = EntityViewElementUtils.currentEntity( viewElementBuilderContext, WebCmsObject.class );

			if ( object instanceof WebCmsComponent ) {
				row.addFirstChild(
						table.cell()
						     .add(
								     BootstrapUiBuilders.radio()
								                        .unwrapped()
								                        .name( "selectedComponent" )
								                        .value( object.getObjectId() )
						     )
						     .build( viewElementBuilderContext )
				);

				if ( WebCmsComponentUtils.isContainerType( ( (WebCmsComponent) object ).getComponentType() ) ) {
					String url = ServletUriComponentsBuilder.fromCurrentRequest()
					                                        .replaceQueryParam( "parent", object.getObjectId() )
					                                        .toUriString();

					row.addChild(
							table.cell()
							     .add(
									     BootstrapUiBuilders.link()
									                        .url( url )
									                        .title( "View container members" )
									                        .add( webCmsIcons.component.viewMembers() )
							     )
							     .build( viewElementBuilderContext )
					);
				}
				else {
					row.addChild(
							table.cell()
							     .build( viewElementBuilderContext )
					);
				}
			}
			else {
				row.addFirstChild( table.cell().build( viewElementBuilderContext ) );

				String url = ServletUriComponentsBuilder.fromCurrentRequest()
				                                        .replaceQueryParam( "parent", object.getObjectId() )
				                                        .toUriString();
				row.addChild(
						table.cell()
						     .add(
								     BootstrapUiBuilders.link()
								                        .url( url )
								                        .title( "View components" )
								                        .add( webCmsIcons.component.viewMembers() )
						     )
						     .build( viewElementBuilderContext )
				);
			}
		} );
	}

	private void addComponentToBreadcrumb( String parent, NodeViewElementBuilder breadcrumb, boolean asLink ) {
		WebCmsComponent owner = componentRepository.findOneByObjectId( parent ).orElse( null );

		if ( owner != null ) {
			String url = ServletUriComponentsBuilder.fromCurrentRequest()
			                                        .replaceQueryParam( "parent", owner.getObjectId() )
			                                        .toUriString();
			String title = StringUtils.defaultIfBlank( owner.getTitle(), StringUtils.defaultIfBlank( owner.getName(), owner.getComponentType().getName() ) );

			breadcrumb.addFirst(
					BootstrapUiBuilders.node( "li" )
					                   .add(
							                   asLink
									                   ? BootstrapUiBuilders.link()
									                                        .url( url )
									                                        .title( owner.getName() )
									                                        .text( title )
									                   : BootstrapUiBuilders.text( title )
					                   )
			);

			if ( owner.hasOwner() ) {
				addComponentToBreadcrumb( owner.getOwnerObjectId(), breadcrumb, true );
			}
		}

		WebCmsObject nonObjectOwner = retrieveOwner( parent );

		if ( nonObjectOwner != null ) {
			addObjectToBreadcrumb( nonObjectOwner, breadcrumb, asLink );
		}
	}

	private void addObjectToBreadcrumb( WebCmsObject object, NodeViewElementBuilder breadcrumb, boolean asLink ) {
		String url = ServletUriComponentsBuilder.fromCurrentRequest()
		                                        .replaceQueryParam( "parent", object.getObjectId() )
		                                        .toUriString();

		EntityConfiguration<Object> configuration = entityRegistry.getEntityConfiguration( object );
		String name = configuration.getEntityMessageCodeResolver().getNameSingular();
		String title = name + ": " + configuration.getLabel( object );

		breadcrumb.addFirst(
				BootstrapUiBuilders.node( "li" )
				                   .add(
						                   asLink
								                   ? BootstrapUiBuilders.link()
								                                        .url( url )
								                                        .title( title )
								                                        .text( title )
								                   : BootstrapUiBuilders.text( title )
				                   )
		);

	}

	private WebCmsObject retrieveOwner( String ownerId ) {
		WebCmsObject owner = assetRepository.findOneByObjectId( ownerId ).orElse( null );

		if ( owner == null ) {
			owner = typeSpecifierRepository.findOneByObjectId( ownerId ).orElse( null );
		}

		return owner;
	}
}
