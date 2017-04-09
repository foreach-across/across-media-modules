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

import com.foreach.across.modules.hibernate.id.AcrossSequenceGenerator;
import com.foreach.across.modules.webcms.domain.WebCmsObjectInheritanceSuperClass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.*;
import java.util.Date;

import static com.foreach.across.modules.webcms.domain.WebCmsObjectInheritanceSuperClass.DISCRIMINATOR_COLUMN;

/**
 * Generic base class for an identifiable asset in the WebCmsModule.  An asset is of a particular type - identified by the subclass -
 * and has an automatic long id along with a manually set {@link #setObjectId(String)}.
 * <p/>
 * An asset implements the {@link com.foreach.across.modules.hibernate.business.Auditable} interface.
 * <p/>
 * Any module can extend this base class to hook into the default asset support.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@NotThreadSafe
@Entity
@Table(name = "wcm_asset")
@Access(AccessType.FIELD)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = DISCRIMINATOR_COLUMN, discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
public abstract class WebCmsAsset<T extends WebCmsAsset<T>> extends WebCmsObjectInheritanceSuperClass<T>
{
	@Id
	@GeneratedValue(generator = "seq_wcm_asset_id")
	@GenericGenerator(
			name = "seq_wcm_asset_id",
			strategy = AcrossSequenceGenerator.STRATEGY,
			parameters = {
					@org.hibernate.annotations.Parameter(name = "sequenceName", value = "seq_wcm_asset_id"),
					@org.hibernate.annotations.Parameter(name = "allocationSize", value = "5")
			}
	)
	private Long id;

	/**
	 * Is this asset published.  The exact meaning of published depends on the type of asset, but in general
	 * not published means it would not be available online.
	 */
	@Column(name = "published")
	private boolean published;

	/**
	 * Timestamp when this asset was published.
	 */
	@Column(name = "publication_date")
	@Temporal(TemporalType.TIMESTAMP)
	private Date publicationDate;

	public WebCmsAsset() {
		super();
	}

	protected WebCmsAsset( Long id,
	                    Long newEntityId,
	                    String objectId,
	                    String createdBy,
	                    Date createdDate,
	                    String lastModifiedBy,
	                    Date lastModifiedDate,
	                    boolean published,
	                    Date publicationDate ) {
		super( id, newEntityId, objectId, createdBy, createdDate, lastModifiedBy, lastModifiedDate );
		setPublished( published );
		setPublicationDate( publicationDate );
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId( Long id ) {
		this.id = id;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{" +
				"objectId='" + getObjectId() + '\'' +
				'}';
	}
}
