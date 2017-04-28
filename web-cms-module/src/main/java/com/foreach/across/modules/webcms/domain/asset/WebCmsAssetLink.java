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

package com.foreach.across.modules.webcms.domain.asset;

import com.foreach.across.modules.hibernate.business.SettableIdAuditableEntity;
import com.foreach.across.modules.hibernate.id.AcrossSequenceGenerator;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifier;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Length;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * Base class that maps the relationship between a {@link com.foreach.across.modules.webcms.domain.WebCmsObject} and a {@link WebCmsTypeSpecifier}.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@NotThreadSafe
@MappedSuperclass
@Table(name = "wcm_object_asset_link")
@Access(AccessType.FIELD)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "discriminator", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
public abstract class WebCmsAssetLink<T extends WebCmsAssetLink<T, U>, U extends WebCmsAsset<U>>
		extends SettableIdAuditableEntity<T>
{
	@Id
	@GeneratedValue(generator = "seq_wcm_object_asset_link_id")
	@GenericGenerator(
			name = "seq_wcm_object_asset_link_id",
			strategy = AcrossSequenceGenerator.STRATEGY,
			parameters = {
					@org.hibernate.annotations.Parameter(name = "sequenceName", value = "seq_wcm_object_asset_link_id"),
					@org.hibernate.annotations.Parameter(name = "allocationSize", value = "5")
			}
	)
	private Long id;

	/**
	 * Unique object id of the object that owns this link.
	 * There is no actual referential integrity here, custom implementations must make sure they perform the required cleanup.
	 */
	@Column(name = "owner_object_id")
	@Length(max = 100)
	private String ownerObjectId;

	/**
	 * The asset being linked to.
	 */
	@NotNull
	@ManyToOne
	@JoinColumn(name = "asset_id")
	private U asset;

	/**
	 * Optional type descriptor for the link.
	 */
	@Column(name = "link_type")
	@Length(max = 255)
	String linkType;

	/**
	 * Sort index of the link in case they are ordered.
	 */
	@Column(name = "sort_index")
	private int sortIndex;

	public WebCmsAssetLink() {
		super();
	}

	protected WebCmsAssetLink( Long id,
	                           Long newEntityId,
	                           String createdBy,
	                           Date createdDate,
	                           String lastModifiedBy,
	                           Date lastModifiedDate,
	                           U asset,
	                           String linkType,
	                           int sortIndex ) {
		setId( id );
		setNewEntityId( newEntityId );
		setCreatedBy( createdBy );
		setCreatedDate( createdDate );
		setLastModifiedBy( lastModifiedBy );
		setLastModifiedDate( lastModifiedDate );
		setAsset( asset );
		setLinkType( linkType );
		setSortIndex( sortIndex );
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{" +
				"ownerObjectId='" + ownerObjectId + '\'' +
				", asset=" + asset +
				", linkType='" + linkType + '\'' +
				'}';
	}
}
