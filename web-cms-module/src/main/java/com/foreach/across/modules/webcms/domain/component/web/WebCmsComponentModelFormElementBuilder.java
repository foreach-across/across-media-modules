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

import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.AbstractNodeViewElement;
import com.foreach.across.modules.web.ui.elements.builder.AbstractNodeViewElementBuilder;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import lombok.Getter;
import lombok.Setter;

/**
 * Configurable builder for a form element for manage a single {@link WebCmsComponentModel}.
 * Will contain either
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
public final class WebCmsComponentModelFormElementBuilder extends AbstractNodeViewElementBuilder<WebCmsComponentModelFormElementBuilder.WebCmsComponentModelFormElement, WebCmsComponentModelFormElementBuilder>
{
	private ViewElementBuilder contentViewElementBuilder, membersViewElementBuilder, settingsViewElementBuilder, metadataViewElementBuilder;

	public WebCmsComponentModelFormElementBuilder( WebCmsComponentModel componentModel ) {
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
	protected WebCmsComponentModelFormElement createElement( ViewElementBuilderContext builderContext ) {
		WebCmsComponentModelFormElement element = new WebCmsComponentModelFormElement();

		if ( contentViewElementBuilder != null ) {
			element.setContent( contentViewElementBuilder.build( builderContext ) );
		}

		if ( membersViewElementBuilder != null ) {
			element.setMembers( membersViewElementBuilder.build( builderContext ) );
		}

		if ( metadataViewElementBuilder != null ) {
			element.setMetadata( metadataViewElementBuilder.build( builderContext ) );
		}

		if ( settingsViewElementBuilder != null ) {
			element.setSettings( settingsViewElementBuilder.build( builderContext ) );
		}

		return apply( element, builderContext );
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
	}
}
