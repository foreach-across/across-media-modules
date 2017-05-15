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

package com.foreach.across.modules.webcms.domain.component.model;

import com.foreach.across.modules.hibernate.business.EntityWithDto;
import com.foreach.across.modules.hibernate.util.DtoUtils;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.webcms.domain.WebCmsObject;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentType;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import javax.validation.Valid;
import java.util.Objects;

/**
 * Represents a base model for strongly typed web component implementations,
 * backed by a {@link WebCmsComponent} entity.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
public abstract class WebCmsComponentModel implements ViewElement, WebCmsObject
{
	/**
	 * Common attribute used to specify the base type of the component type.
	 */
	public static final String TYPE_ATTRIBUTE = "type";

	/**
	 * Common attribute used to specify the (Thymeleaf) template that should be used to render components of that type.
	 */
	public static final String TEMPLATE_ATTRIBUTE = "template";

	/**
	 * Common attribute used to specify the fully qualified class name of the metadata for a particular component type.
	 */
	public static final String METADATA_CLASS_ATTRIBUTE = "metadata";

	/**
	 * Original component this model represents.
	 */
	@Valid
	@Getter
	private WebCmsComponent component;

	/**
	 * Specific template that should be used to render this component.
	 */
	@Getter
	@Setter
	private String renderTemplate;

	/**
	 * The typed metadata for this component, can be {@code null} if no metadata is supported,
	 * else should contain an instance.
	 */
	@Valid
	@Getter
	@Setter
	private Object metadata;

	protected WebCmsComponentModel() {
		this.component = new WebCmsComponent();
	}

	/**
	 * Base constructor for {@link #asComponentTemplate()} calls, uses the parent component as template
	 * and attempts to convert the metadata to a new instance.
	 */
	protected WebCmsComponentModel( WebCmsComponentModel template ) {
		setComponent( template.getComponent().asTemplate() );
		setMetadata( cloneMetadata( template ) );
	}

	protected WebCmsComponentModel( WebCmsComponent component ) {
		setComponent( component );
	}

	/**
	 * Implements {@link ViewElement#getCustomTemplate()}.  This method returns the custom template that should be used
	 * for rendering this component model.  If none is configured using {@link #setRenderTemplate(String)}, the template set
	 * as {@link WebCmsComponentModel#TEMPLATE_ATTRIBUTE} attribute on the {@link WebCmsComponentType} will be used.
	 *
	 * @return optional custom template for rendering
	 * @see #renderTemplate
	 */
	@Override
	public String getCustomTemplate() {
		if ( renderTemplate != null ) {
			return renderTemplate;
		}

		val componentType = getComponentType();
		return componentType != null ? componentType.getAttribute( TEMPLATE_ATTRIBUTE ) : null;
	}

	/**
	 * @return true if there is metadata attached to this model
	 */
	public boolean hasMetadata() {
		return metadata != null;
	}

	/**
	 * Coerce the metadata to the type specified.
	 */
	@SuppressWarnings("unchecked")
	public <V extends U, U> V getMetadata( Class<U> metadataType ) {
		return (V) metadataType.cast( metadata );
	}

	/**
	 * Set the original component backing this model, can never be {@code null}.
	 * If you want to create a new instance with all settings but remove the attachment
	 * to the original component, use {@link #asComponentTemplate()}.
	 *
	 * @param component that backs this model
	 */
	public void setComponent( WebCmsComponent component ) {
		Assert.notNull( component );
		this.component = component.toDto();
	}

	/**
	 * @return Type of the WebCmsComponent.
	 */
	public WebCmsComponentType getComponentType() {
		return component.getComponentType();
	}

	/**
	 * @return unique object id of the component
	 */
	@Override
	public String getObjectId() {
		return component.getObjectId();
	}

	/**
	 * Set the unique object id of the backing component
	 *
	 * @param objectId to use
	 */
	public void setObjectId( String objectId ) {
		component.setObjectId( objectId );
	}

	/**
	 * @return the optional owner object id of the backing component
	 */
	public String getOwnerObjectId() {
		return component.getOwnerObjectId();
	}

	/**
	 * Set the owner object of the backing component.
	 *
	 * @param owner instance
	 */
	public void setOwner( WebCmsObject owner ) {
		component.setOwner( owner );
	}

	/**
	 * Set the owner object id of the backing component.
	 *
	 * @param ownerObjectId to use
	 */
	public void setOwnerObjectId( String ownerObjectId ) {
		component.setOwnerObjectId( ownerObjectId );
	}

	/**
	 * @return true if the backing component has a specific owner
	 */
	public boolean hasOwner() {
		return component.hasOwner();
	}

	/**
	 * Set the title on the backing component.
	 *
	 * @param title to use
	 */
	public void setTitle( String title ) {
		component.setTitle( title );
	}

	/**
	 * @return title (if set) or name
	 */
	public String getTitle() {
		return StringUtils.defaultString( component.getTitle(), component.getName() );
	}

	/**
	 * @return the name of the backing component
	 */
	@Override
	public String getName() {
		return component.getName();
	}

	/**
	 * Set the name on the backing component.
	 *
	 * @param name to use
	 */
	public void setName( String name ) {
		component.setName( name );
	}

	/**
	 * @return sortIndex of the component in its owner scope
	 */
	public int getSortIndex() {
		return component.getSortIndex();
	}

	/**
	 * @param sortIndex of the component in its owner scope
	 */
	public void setSortIndex( int sortIndex ) {
		component.setSortIndex( sortIndex );
	}

	/**
	 * @return fixed ViewElement type
	 */
	@Override
	public final String getElementType() {
		return WebCmsComponentModel.class.getSimpleName();
	}

	/**
	 * @return true if this model is an unpersisted component
	 */
	public final boolean isNew() {
		return component.isNew();
	}

	/**
	 * Convert the current model to a templated version: keep all properties but create a new backing component.
	 *
	 * @return templated version of this model - attached to a new backing component
	 * @see WebCmsComponent#asTemplate()
	 * @see #cloneMetadata(WebCmsComponentModel)
	 */
	public abstract WebCmsComponentModel asComponentTemplate();

	/**
	 * Base implementation that can be used to check if the component contains any relevant data.
	 *
	 * @return {@code true} if no relevant data is present
	 */
	public abstract boolean isEmpty();

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}
		WebCmsComponentModel that = (WebCmsComponentModel) o;
		return Objects.equals( component, that.component );
	}

	@Override
	public int hashCode() {
		return Objects.hash( component );
	}

	@Override
	public String toString() {
		return "WebCmsComponentModel{" +
				"name='" + getName() + '\'' +
				", componentType=" + getComponentType() +
				", objectId='" + getObjectId() + '\'' +
				'}';
	}

	/**
	 * Attempt to forcibly clone the metadata of a {@link WebCmsComponentModel}.
	 * Throws an exception if impossible.
	 *
	 * @param componentModel to clone the metadata from
	 * @return clone
	 * @see com.foreach.across.modules.hibernate.util.DtoUtils
	 * @see com.foreach.across.modules.hibernate.business.EntityWithDto
	 */
	protected static Object cloneMetadata( WebCmsComponentModel componentModel ) {
		if ( componentModel.hasMetadata() ) {
			val metadata = componentModel.getMetadata();
			if ( metadata instanceof EntityWithDto ) {
				return ( (EntityWithDto) metadata ).toDto();
			}
			if ( metadata instanceof Cloneable ) {
				return org.apache.commons.lang3.ObjectUtils.clone( metadata );
			}
			return DtoUtils.createDto( metadata );
		}

		return null;
	}
}
