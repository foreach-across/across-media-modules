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

package com.foreach.across.modules.webcms.domain.component;

import com.foreach.across.modules.hibernate.id.AcrossSequenceGenerator;
import com.foreach.across.modules.webcms.domain.WebCmsObject;
import com.foreach.across.modules.webcms.domain.WebCmsObjectSuperClass;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotBlank;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Date;

/**
 * Represents a (visual) component of an asset.
 *
 * @author Arne Vandamme
 * @see com.foreach.across.modules.webcms.domain.asset.WebCmsAsset
 * @since 0.0.1
 */
@NotThreadSafe
@Entity
@Table(name = "wcm_component")
@Access(AccessType.FIELD)
@Getter
@Setter
public class WebCmsComponent extends WebCmsObjectSuperClass<WebCmsComponent>
{
	/**
	 * Prefix that all object ids of a WebCmsComponent have.
	 */
	public static final String COLLECTION_ID = "wcm:component";

	/**
	 * Type of the WebCmsComponent.
	 */
	@ManyToOne
	@JoinColumn(name = "component_type_id")
	@NotNull
	WebCmsComponentType componentType;

	@Id
	@GeneratedValue(generator = "seq_wcm_component_id")
	@GenericGenerator(
			name = "seq_wcm_component_id",
			strategy = AcrossSequenceGenerator.STRATEGY,
			parameters = {
					@org.hibernate.annotations.Parameter(name = "sequenceName", value = "seq_wcm_component_id"),
					@org.hibernate.annotations.Parameter(name = "allocationSize", value = "5")
			}
	)
	private Long id;

	/**
	 * Unique object id of the asset that owns this component.
	 * There is no actual referential integrity here, custom asset implementations must make sure they perform the required cleanup.
	 */
	@Column(name = "owner_object_id")
	@Length(max = 100)
	private String ownerObjectId;

	/**
	 * Optional descriptive title of the component.
	 */
	@Column(name = "title")
	@NotBlank(groups = SharedComponentValidation.class)
	@Length(max = 255)
	private String title;

	/**
	 * Name of the component - if set must be unique within the owner.
	 * Component name can only contain alphanumeric characters, . (dot), - (dash) or _ (underscore).
	 */
	@Column(name = "name")
	@NotBlank(groups = SharedComponentValidation.class)
	@Pattern(regexp = "^[\\p{Alnum}-_.]*$")
	@Length(max = 100)
	private String name;

	/**
	 * Sort index of the component within the owner.
	 */
	@Column(name = "sort_index")
	private int sortIndex;

	/**
	 * Raw body of the component. How the body can be managed is determined by the component type.
	 */
	@Column(name = "body")
	private String body;

	/**
	 * Raw metadata of the component.  How the metadata can be managed is determined by the component type.
	 */
	@Column(name = "metadata")
	private String metadata;

	/**
	 * True if the body of the component contains {@link WebCmsContentMarker} strings.
	 * You should usually not set this value manually.
	 *
	 * @see WebCmsComponentInterceptor
	 */
	@Getter
	@Setter(AccessLevel.PROTECTED)
	@Column(name = "body_contains_markers")
	private boolean bodyWithContentMarkers;

	/**
	 * If this component is a proxy, will contain the proxy target.
	 */
	@ManyToOne
	@JoinColumn(name = "proxied_component_id")
	private WebCmsComponent proxyTarget;

	public WebCmsComponent() {
		super();
	}

	@Builder(toBuilder = true)
	public WebCmsComponent( @Builder.ObtainVia(method = "getId") Long id,
	                        @Builder.ObtainVia(method = "getNewEntityId") Long newEntityId,
	                        @Builder.ObtainVia(method = "getObjectId") String objectId,
	                        @Builder.ObtainVia(method = "getCreatedBy") String createdBy,
	                        @Builder.ObtainVia(method = "getCreatedDate") Date createdDate,
	                        @Builder.ObtainVia(method = "getLastModifiedBy") String lastModifiedBy,
	                        @Builder.ObtainVia(method = "getLastModifiedDate") Date lastModifiedDate,
	                        @Builder.ObtainVia(method = "getDomain") WebCmsDomain domain,
	                        WebCmsComponentType componentType,
	                        String ownerObjectId,
	                        String name,
	                        int sortIndex,
	                        String title,
	                        String body,
	                        String metadata,
	                        WebCmsComponent proxyTarget ) {
		super( id, newEntityId, objectId, createdBy, createdDate, lastModifiedBy, lastModifiedDate, domain );

		this.componentType = componentType;
		this.ownerObjectId = ownerObjectId;
		this.name = name;
		this.sortIndex = sortIndex;
		this.title = title;
		this.body = body;
		this.metadata = metadata;
		this.proxyTarget = proxyTarget;
	}

	/**
	 * Set the owner of this component, will use {@link WebCmsObject#getObjectId()} to get the owner object id.
	 *
	 * @param owner or null to detach
	 */
	public void setOwner( WebCmsObject owner ) {
		setOwnerObjectId( owner != null ? owner.getObjectId() : null );
	}

	/**
	 * @return true if this component has an owner id set
	 */
	public boolean hasOwner() {
		return getOwnerObjectId() != null;
	}

	/**
	 * @return true if this is in fact a proxy component
	 */
	public boolean isProxyComponent() {
		return getProxyTarget() != null;
	}

	@Override
	protected String getObjectCollectionId() {
		return COLLECTION_ID;
	}

	/**
	 * @return a new WebCmsComponent with all properties of this one, but all ids and ownership reset
	 */
	public WebCmsComponent asTemplate() {
		val template = new WebCmsComponent();
		template.componentType = componentType;
		template.title = title;
		template.name = name;
		template.body = body;
		template.metadata = metadata;
		template.sortIndex = sortIndex;
		template.proxyTarget = proxyTarget;
		template.setDomain( getDomain() );
		return template;
	}

	@Override
	public String toString() {
		return "WebCmsComponent{" +
				"objectId='" + getObjectId() + "'," +
				"componentType=" + componentType +
				'}';
	}

	/**
	 * Marker interface for validation related to creating a globally shared component without a specific owner.
	 */
	public interface SharedComponentValidation
	{
	}
}
