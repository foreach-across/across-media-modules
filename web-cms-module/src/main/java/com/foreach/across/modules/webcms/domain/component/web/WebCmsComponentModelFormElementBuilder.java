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

import com.foreach.across.modules.bootstrapui.styles.BootstrapStyles;
import com.foreach.across.modules.entity.support.EntityMessageCodeResolver;
import com.foreach.across.modules.web.ui.DefaultViewElementBuilderContext;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.AbstractNodeViewElement;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.NodeViewElement;
import com.foreach.across.modules.web.ui.elements.TextViewElement;
import com.foreach.across.modules.web.ui.elements.builder.AbstractNodeViewElementBuilder;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * Configurable builder for a form element for manage a single {@link WebCmsComponentModel}.
 * Will contain either
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
public final class WebCmsComponentModelFormElementBuilder extends AbstractNodeViewElementBuilder<WebCmsComponentModelFormElementBuilder.WebCmsComponentModelFormElement, WebCmsComponentModelFormElementBuilder>
{
	public static final String COMPONENT_MESSAGE_CODE_PREFIX = WebCmsComponentModelFormElement.class.getName() + ".messageCodePrefix";

	private final WebCmsComponentModel componentModel;

	private ViewElementBuilder contentViewElementBuilder, membersViewElementBuilder, settingsViewElementBuilder, metadataViewElementBuilder;

	public WebCmsComponentModelFormElementBuilder( WebCmsComponentModel componentModel ) {
		this.componentModel = componentModel;

		customTemplate( "th/webCmsModule/admin/fragments :: componentModelFormElement(${component},${component.componentModel})" );
		name( "formControl-" + componentModel.getName() );
		attribute( "componentModel", componentModel );
		htmlId( componentModel.getObjectId() );
		showSettings( true );
	}

	public WebCmsComponentModelFormElementBuilder showDeleteButton( boolean showButton ) {
		attribute( "showDeleteButton", showButton );
		return this;
	}

	public WebCmsComponentModelFormElementBuilder showAddComponentButton( boolean showButton ) {
		attribute( "showAddComponentButton", showButton );
		return this;
	}

	/**
	 * Show this component as a linked component (only with edit and delete options).
	 * Useful for dynamic container.
	 */
	public WebCmsComponentModelFormElementBuilder showAsLinkedComponent( boolean linkedComponent ) {
		attribute( "showAsLinkedComponent", linkedComponent );
		return this;
	}

	/**
	 * Indicate if the component is a sortable container.  If so it is expected to contain only linked component members.
	 */
	public WebCmsComponentModelFormElementBuilder sortableContainer( boolean sortable ) {
		attribute( "sortableContainer", sortable );
		return this;
	}

	/**
	 * Set content of the "members" pane, this contains any related components owned by this one.
	 */
	public WebCmsComponentModelFormElementBuilder members( ViewElementBuilder membersViewElementBuilder ) {
		this.membersViewElementBuilder = membersViewElementBuilder;
		return this;
	}

	/**
	 * Set content of the "content" pane.  Set to {@code null} if you don't want a content pane.
	 */
	public WebCmsComponentModelFormElementBuilder content( ViewElementBuilder contentViewElementBuilder ) {
		this.contentViewElementBuilder = contentViewElementBuilder;
		return this;
	}

	/**
	 * Set content of the "settings" pane.  Set to {@code null} if you don't want a settings pane.
	 */
	public WebCmsComponentModelFormElementBuilder settings( ViewElementBuilder settingsViewElementBuilder ) {
		this.settingsViewElementBuilder = settingsViewElementBuilder;
		return this;
	}

	/**
	 * Set content of the "metadata" pane.  Set to {@code null} if you don't want a metadata pane.
	 */
	public WebCmsComponentModelFormElementBuilder metadata( ViewElementBuilder metadataViewElementBuilder ) {
		this.metadataViewElementBuilder = metadataViewElementBuilder;
		return this;
	}

	/**
	 * Should settings be shown.
	 */
	public WebCmsComponentModelFormElementBuilder showSettings( boolean showSettings ) {
		attribute( "showSettings", showSettings );
		return this;
	}

	@Override
	protected WebCmsComponentModelFormElement createElement( ViewElementBuilderContext parentBuilderContext ) {
		ViewElementBuilderContext builderContext = new DefaultViewElementBuilderContext( parentBuilderContext );

		WebCmsComponentModelFormElement element = new WebCmsComponentModelFormElement();

		EntityMessageCodeResolver codeResolver = builderContext.getAttribute( EntityMessageCodeResolver.class );
		String messageCodePrefix = StringUtils.defaultString( builderContext.getAttribute( COMPONENT_MESSAGE_CODE_PREFIX, String.class ) );

		if ( messageCodePrefix.isEmpty() ) {
			messageCodePrefix = componentModel.getComponentType().getTypeKey();
		}
		else {
			messageCodePrefix += ".members." + componentModel.getName();
		}

		builderContext.setAttribute( COMPONENT_MESSAGE_CODE_PREFIX, messageCodePrefix );
		EntityMessageCodeResolver newCodeResolver = new EntityMessageCodeResolver( codeResolver );
		newCodeResolver.addPrefixes( "webCmsComponents" );
		builderContext.setAttribute( EntityMessageCodeResolver.class, newCodeResolver );

		if ( contentViewElementBuilder != null ) {
			element.setContent(
					applyDescription( contentViewElementBuilder.build( builderContext ), messageCodePrefix + ".content", newCodeResolver )
			);
		}

		if ( membersViewElementBuilder != null ) {
			element.setMembers(
					applyDescription( membersViewElementBuilder.build( builderContext ), messageCodePrefix + ".members", newCodeResolver )
			);
		}

		if ( metadataViewElementBuilder != null ) {
			element.setMetadata(
					applyDescription( metadataViewElementBuilder.build( builderContext ), messageCodePrefix + ".metadata", newCodeResolver )
			);
		}

		if ( settingsViewElementBuilder != null ) {
			element.setSettings(
					applyDescription( settingsViewElementBuilder.build( builderContext ), messageCodePrefix + ".settings", newCodeResolver )
			);
		}

		return apply( element, builderContext );
	}

	private ViewElement applyDescription( ViewElement original, String messageCodePrefix, EntityMessageCodeResolver codeResolver ) {
		String descriptionText = codeResolver.getMessageWithFallback( messageCodePrefix + "[description]", "" );
		String additionalDescriptionText = codeResolver.getMessageWithFallback( messageCodePrefix + "[additionalDescription]", "" );

		if ( StringUtils.isNotEmpty( descriptionText ) || StringUtils.isNotEmpty( additionalDescriptionText ) ) {
			ContainerViewElement container = new ContainerViewElement();

			if ( StringUtils.isNotEmpty( descriptionText ) ) {
				NodeViewElement helpBlock = new NodeViewElement( "p" );
				helpBlock.set( BootstrapStyles.css.form.text, BootstrapStyles.css.text.muted, BootstrapStyles.css.margin.bottom.s1 );
				helpBlock.addCssClass( "help-block", "description-block" );
				helpBlock.addChild( TextViewElement.html( descriptionText ) );
				container.addChild( helpBlock );
			}

			container.addChild( original );

			if ( StringUtils.isNotEmpty( additionalDescriptionText ) ) {
				NodeViewElement helpBlock = new NodeViewElement( "p" );
				helpBlock.set( BootstrapStyles.css.form.text, BootstrapStyles.css.text.muted, BootstrapStyles.css.margin.bottom.s1 );
				helpBlock.addCssClass( "help-block", "description-block-additional" );
				helpBlock.addChild( TextViewElement.html( additionalDescriptionText ) );
				container.addChild( helpBlock );
			}

			return container;
		}

		return original;
	}

	static class WebCmsComponentModelFormElement extends AbstractNodeViewElement
	{
		@Setter
		@Getter
		private ViewElement content, settings, metadata, members;

		private WebCmsComponentModelFormElement() {
			super( "div" );
		}

		public WebCmsComponentModel getComponentModel() {
			return getAttribute( "componentModel", WebCmsComponentModel.class );
		}

		public boolean isShowDeleteButton() {
			return Boolean.TRUE.equals( getAttribute( "showDeleteButton" ) );
		}

		public boolean isShowAddComponentButton() {
			return Boolean.TRUE.equals( getAttribute( "showAddComponentButton" ) );
		}

		public boolean isSortableContainer() {
			return Boolean.TRUE.equals( getAttribute( "sortableContainer" ) );
		}

		public boolean isShowSettings() {
			return Boolean.TRUE.equals( getAttribute( "showSettings" ) );
		}

		public boolean isShowAsLinkedComponent() {
			return Boolean.TRUE.equals( getAttribute( "showAsLinkedComponent" ) );
		}

		public boolean hasMembers() {
			return !isShowAsLinkedComponent() && members != null;
		}

		public boolean hasContent() {
			return !isShowAsLinkedComponent() && content != null;
		}

		public boolean hasSettings() {
			return !isShowAsLinkedComponent() && isShowSettings() && settings != null;
		}

		public boolean hasMetadata() {
			return !isShowAsLinkedComponent() && metadata != null;
		}

		public String getLabel() {
			WebCmsComponentModel componentModel = getComponentModel();
			return StringUtils.defaultIfBlank(
					componentModel.getTitle(),
					StringUtils.defaultIfBlank( componentModel.getName(), componentModel.getComponentType().getName() )
			);
		}
	}
}
