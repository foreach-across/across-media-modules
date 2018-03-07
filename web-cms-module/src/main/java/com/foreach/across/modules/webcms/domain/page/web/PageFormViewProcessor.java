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

import com.foreach.across.modules.adminweb.ui.PageContentStructure;
import com.foreach.across.modules.bootstrapui.elements.FormGroupElement;
import com.foreach.across.modules.bootstrapui.elements.GlyphIcon;
import com.foreach.across.modules.bootstrapui.elements.LinkViewElement;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.menu.EntityAdminMenuEvent;
import com.foreach.across.modules.entity.views.processors.EntityViewProcessorAdapter;
import com.foreach.across.modules.entity.views.processors.SingleEntityFormViewProcessor;
import com.foreach.across.modules.entity.views.processors.support.EntityPageStructureRenderedEvent;
import com.foreach.across.modules.entity.views.request.EntityViewCommand;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.web.menu.PathBasedMenuBuilder;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.HtmlViewElement;
import com.foreach.across.modules.web.ui.elements.TextViewElement;
import com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpointRepository;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetService;
import com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint;
import com.foreach.across.modules.webcms.domain.menu.QWebCmsMenuItem;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenu;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenuItem;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenuItemRepository;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.config.WebCmsPageConfiguration;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;

import java.util.*;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
@ConditionalOnAdminUI
@Component
@RequiredArgsConstructor
public final class PageFormViewProcessor extends EntityViewProcessorAdapter
{
	private final WebCmsAssetService assetPreviewService;
	private final WebCmsAssetEndpointRepository assetEndpointRepository;
	private final WebCmsMenuItemRepository menuItemRepository;
	private final PageTypeProperties pageTypeProperties;
	private final WebCmsMultiDomainService multiDomainService;

	@SuppressWarnings("unused")
	@EventListener
	void setPreviewLinkOnMenu( EntityPageStructureRenderedEvent<WebCmsPage> event ) {
		if ( event.holdsEntity() ) {
			assetPreviewService
					.buildPreviewUrl( event.getEntity() )
					.ifPresent( previewUrl -> {
						LinkViewElement openLink = new LinkViewElement();
						openLink.setAttribute( "target", "_blank" );
						openLink.setUrl( previewUrl );
						openLink.setTitle( event.getEntityViewContext().getEntityMessages().withNameSingular( "actions.open" ) );
						openLink.addChild( new GlyphIcon( GlyphIcon.EYE_OPEN ) );

						PageContentStructure adminPage = event.getPageContentStructure();
						adminPage.addToPageTitleSubText( TextViewElement.html( "&nbsp;" ) );
						adminPage.addToPageTitleSubText( openLink );
					} );
		}
	}

	@EventListener
	void handleMenuCreationEvent( EntityAdminMenuEvent<WebCmsPage> event ) {
		if ( event.getEntity() != null && event.getEntity().getPageType() != null ) {
			boolean hasEndpoint = event.getEntity().getPageType().hasEndpoint();
			if ( !hasEndpoint ) {
				PathBasedMenuBuilder builder = event.builder();
				event.builder().item( "urls" ).disable()
				     .and().item( "webCmsMenuItems" ).disable();
			}
		}

	}

	@Override
	public void initializeCommandObject( EntityViewRequest entityViewRequest, EntityViewCommand command, WebDataBinder dataBinder ) {
		AdvancedSettings advancedSettings = new AdvancedSettings();
		WebCmsPage page = entityViewRequest.getEntityViewContext().getEntity( WebCmsPage.class );
		WebCmsPage pageFromCommand = command.getEntity( WebCmsPage.class );
		if ( page != null ) {
			WebCmsEndpoint endpoint = assetEndpointRepository.findOneByAssetAndDomain( page, multiDomainService.getCurrentDomainForEntity( page ) );

			if ( endpoint != null ) {
				menuItemRepository.findAll( QWebCmsMenuItem.webCmsMenuItem.endpoint.eq( endpoint ).and( QWebCmsMenuItem.webCmsMenuItem.generated.isTrue() ) )
				                  .forEach( item -> advancedSettings.getAutoCreateMenu().add( item.getMenu() ) );
			}

		}

		if ( pageFromCommand != null && pageFromCommand.getPageType() == null ) {
			pageFromCommand.setPageType( pageTypeProperties.getDefaultType() );
		}

		command.addExtension( "advanced", advancedSettings );
	}

	@Override
	protected void doPost( EntityViewRequest entityViewRequest, EntityView entityView, EntityViewCommand command, BindingResult bindingResult ) {
		if ( !bindingResult.hasErrors() ) {
			val page = command.getEntity( WebCmsPage.class );
			val advancedSettings = command.getExtension( "advanced", AdvancedSettings.class );

			WebCmsEndpoint endpoint = assetEndpointRepository.findOneByAssetAndDomain( page, multiDomainService.getCurrentDomainForEntity( page ) );

			if ( endpoint != null ) {
				menuItemRepository.findAll( QWebCmsMenuItem.webCmsMenuItem.endpoint.eq( endpoint ) )
				                  .forEach( item -> {
					                  if ( !advancedSettings.getAutoCreateMenu().contains( item.getMenu() ) ) {
						                  WebCmsMenuItem remove = item.toDto();
						                  remove.setGenerated( false );
						                  menuItemRepository.save( remove );
					                  }
				                  } );

				advancedSettings.getAutoCreateMenu()
				                .forEach( menu -> {
					                WebCmsMenuItem existing = menuItemRepository.findOne(
							                QWebCmsMenuItem.webCmsMenuItem.menu.eq( menu ).and( QWebCmsMenuItem.webCmsMenuItem.endpoint.eq( endpoint ) )
					                );

					                WebCmsMenuItem dto = existing != null
							                ? existing.toDto()
							                : WebCmsMenuItem.builder().menu( menu ).endpoint( endpoint ).build();

					                dto.setTitle( page.getTitle() );
					                dto.setPath( page.getCanonicalPath() );
					                dto.setGenerated( true );

					                menuItemRepository.save( dto );
				                } );
			}
		}
	}

	@Override
	protected void postRender( EntityViewRequest entityViewRequest,
	                           EntityView entityView,
	                           ContainerViewElement container,
	                           ViewElementBuilderContext builderContext ) {

		addDependency( container, "pathSegment", "pathSegmentGenerated" );
		addDependency( container, WebCmsPageConfiguration.CANONICAL_PATH, "canonicalPathGenerated" );

		ContainerViewElementUtils.move( container, "publish-settings", SingleEntityFormViewProcessor.RIGHT_COLUMN );

		ContainerViewElementUtils.find( container, "formGroup-menu-items", FormGroupElement.class )
		                         .ifPresent( group -> {
			                         if ( !group.getControl( ContainerViewElement.class ).hasChildren() ) {
				                         ContainerViewElementUtils.remove( container, "formGroup-menu-items" );
			                         }
		                         } );

		WebCmsPage page = entityViewRequest.getEntityViewContext().getEntity( WebCmsPage.class );
		if ( page != null && page.getPageType() != null && !page.getPageType().isPublishable() ) {
			ContainerViewElementUtils.remove( container, "publish-settings" );
		}

	}

	private void addDependency( ContainerViewElement elements, String from, String to ) {
		ContainerViewElementUtils
				.find( elements, "formGroup-" + from, FormGroupElement.class )
				.ifPresent( group -> {
					Map<String, Object> qualifiers = new HashMap<>();
					qualifiers.put( "checked", false );

					HtmlViewElement element = group.getControl( HtmlViewElement.class );
					element.setAttribute( "data-dependson", Collections.singletonMap( "[id='entity." + to + "']", qualifiers ) );
				} );
	}

	@Data
	static class AdvancedSettings
	{
		private Set<WebCmsMenu> autoCreateMenu = new HashSet<>();
	}
}
