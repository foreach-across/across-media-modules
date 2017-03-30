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

package com.foreach.across.modules.webcms.domain.type;

import com.foreach.across.modules.hibernate.business.SettableIdAuditableEntity;
import com.foreach.across.modules.hibernate.id.AcrossSequenceGenerator;
import com.foreach.across.modules.webcms.infrastructure.WebCmsUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

/**
 * Represents a sub-type specifier for a particular asset or component. This serves as a base class.
 * Every sub-type has a unique id, a type and key that is expected to be unique within the type.
 * A sub-type also has a more descriptive name that is also expected to be unique within the type.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@NotThreadSafe
@Entity
@Table(name = "wcm_type")
@Access(AccessType.FIELD)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type_group", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
public abstract class WebCmsTypeSpecifier<T extends WebCmsTypeSpecifier<T>> extends SettableIdAuditableEntity<T>
{
	@Id
	@GeneratedValue(generator = "seq_wcm_type_id")
	@GenericGenerator(
			name = "seq_wcm_type_id",
			strategy = AcrossSequenceGenerator.STRATEGY,
			parameters = {
					@org.hibernate.annotations.Parameter(name = "sequenceName", value = "seq_wcm_type_id"),
					@org.hibernate.annotations.Parameter(name = "allocationSize", value = "1")
			}
	)
	private Long id;

	@Setter(AccessLevel.NONE)
	@Column(name = "type_group", insertable = false, updatable = false)
	private String typeGroup = getTypeGroup();

	/**
	 * Globally unique key for this type. Alternative for the generated id property as the key should be set manually.
	 * Consumer code should use {@link #isNew()} to determine if the
	 * asset is represented by a persisted entity or if it is new.
	 * <p/>
	 * Can be used for synchronization of assets between environments.  Like the regular id the key should preferably
	 * never be modified after creation of an entity, as it determines the global identity of the asset.
	 */
	@Column(name = "unique_key", unique = true)
	@NotBlank
	@Length(max = 255)
	private String uniqueKey;

	/**
	 * Name of the type, should be unique within the type group.
	 */
	@Column(name = "name")
	@NotBlank
	@Length(max = 255)
	private String name;

	/**
	 * Key of the type, should be unique within the type group.
	 */
	@Column(name = "type_key")
	@NotBlank
	@Length(max = 255)
	private String typeKey;

	protected WebCmsTypeSpecifier( Long id,
	                               Long newEntityId,
	                               String uniqueKey,
	                               String name,
	                               String typeKey,
	                               String createdBy,
	                               Date createdDate,
	                               String lastModifiedBy,
	                               Date lastModifiedDate ) {
		setNewEntityId( newEntityId );
		setId( id );
		setCreatedBy( createdBy );
		setCreatedDate( createdDate );
		setLastModifiedBy( lastModifiedBy );
		setLastModifiedDate( lastModifiedDate );

		setUniqueKey( uniqueKey );
		setName( name );
		setTypeKey( typeKey );
	}

	protected WebCmsTypeSpecifier() {
		setUniqueKey( UUID.randomUUID().toString() );
	}

	public final void setUniqueKey( String uniqueKey ) {
		this.uniqueKey = WebCmsUtils.prefixUniqueKeyForCollection( uniqueKey, getTypeCollectionId() );
	}

	/**
	 * @return the type group name
	 */
	public abstract String getTypeGroup();

	protected abstract String getTypeCollectionId();

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{" +
				"uniqueKey='" + uniqueKey + '\'' +
				", typeKey='" + typeKey + '\'' +
				'}';
	}
}
