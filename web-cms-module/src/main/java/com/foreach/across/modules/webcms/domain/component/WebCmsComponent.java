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
import com.foreach.across.modules.webcms.domain.WebCmsObjectSuperClass;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Length;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
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

	// WebCmsComponentType componentType;

	/**
	 * Unique object id of the asset that owns this component.
	 * There is no actual referential integrity here, custom asset implementations must make sure they perform the required cleanup.
	 */
	@Column(name = "owner_object_id")
	@NotNull
	@Length(max = 100)
	private String ownerObjectId;

	/**
	 * Optional descriptive title of the component.
	 */
	@Column(name = "title")
	@Length(max = 255)
	private String title;

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
	                        String ownerObjectId, String title, String body, String metadata ) {
		super( id, newEntityId, objectId, createdBy, createdDate, lastModifiedBy, lastModifiedDate );

		this.ownerObjectId = ownerObjectId;
		this.title = title;
		this.body = body;
		this.metadata = metadata;
	}

	@Override
	protected String getObjectCollectionId() {
		return COLLECTION_ID;
	}
}
