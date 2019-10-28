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

package com.foreach.across.modules.webcms.domain.domain;

import com.foreach.across.modules.hibernate.business.SettableIdAuditableEntity;
import com.foreach.across.modules.hibernate.id.AcrossSequenceGenerator;
import com.foreach.across.modules.webcms.domain.WebCmsObject;
import com.foreach.across.modules.webcms.infrastructure.WebCmsUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.SortNatural;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotBlank;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.*;
import javax.validation.constraints.Pattern;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A {@link WebCmsDomain} represents a collection of other {@link WebCmsObject} entities.
 * Usually a domain represents an actual website or is linked to DNS records, however the domain
 * concept can be used for any arbitrary grouping of entities.
 *
 * @author Arne Vandamme
 * @since 0.0.3
 */
@NotThreadSafe
@Entity
@Table(name = "wcm_domain")
@Access(AccessType.FIELD)
@Getter
@Setter
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class WebCmsDomain extends SettableIdAuditableEntity<WebCmsDomain> implements WebCmsObject
{
	/**
	 * Constant identifier for no-domain which is in fact a {@code null} value.
	 */
	public static final WebCmsDomain NONE = null;
	public static final String NO_DOMAIN_KEY = null;

	/**
	 * Prefix that all object ids of a WebCmsDomain have.
	 */
	public static final String COLLECTION_ID = "wcm:domain";

	@Id
	@GeneratedValue(generator = "seq_wcm_domain_id")
	@GenericGenerator(
			name = "seq_wcm_domain_id",
			strategy = AcrossSequenceGenerator.STRATEGY,
			parameters = {
					@org.hibernate.annotations.Parameter(name = "sequenceName", value = "seq_wcm_domain_id"),
					@org.hibernate.annotations.Parameter(name = "allocationSize", value = "1")
			}
	)
	private Long id;

	/**
	 * Globally unique id for this object. Alternative for the generated id property as the key should be set manually.
	 * Consumer code should use {@link #isNew()} to determine if the object is represented by a persisted entity or if it is new.
	 * <p/>
	 * Can be used for synchronization of assets between environments.  Like the regular id the key should preferably
	 * never be modified after creation of an entity, as it determines the global identity of the asset.
	 */
	@Column(name = "object_id", unique = true)
	@NotBlank
	@Length(max = 100)
	private String objectId;

	/**
	 * Descriptive name of the domain, must be unique.
	 */
	@Column(name = "name")
	@NotBlank
	@Length(max = 255)
	private String name;

	/**
	 * Technical key of the domain, must be unique.
	 * Key can only contain alphanumeric characters, . (dot), - (dash) or _ (underscore).
	 */
	@Column(name = "domain_key")
	@NotBlank
	@Length(max = 255)
	@Pattern(regexp = "^[\\p{Alnum}-_.]*$")
	private String domainKey;

	/**
	 * Optional description text for this domain.
	 */
	@Column(name = "description")
	@Length(max = 500)
	private String description;

	/**
	 * Attributes of the domain, simple key/value pairs.
	 */
	@ElementCollection(fetch = FetchType.EAGER)
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	@CollectionTable(name = "wcm_domain_attributes", joinColumns = @JoinColumn(name = "type_id"))
	@MapKeyColumn(name = "attribute_key")
	@Column(name = "attribute_value")
	@SortNatural
	private SortedMap<String, String> attributes = new TreeMap<>();

	/**
	 * Should this domain still be used?
	 */
	@Column(name = "active")
	private boolean active = true;

	public WebCmsDomain() {
	}

	@Builder(toBuilder = true)
	public WebCmsDomain( @Builder.ObtainVia(method = "getId") Long id,
	                     @Builder.ObtainVia(method = "getNewEntityId") Long newEntityId,
	                     @Builder.ObtainVia(method = "getObjectId") String objectId,
	                     @Builder.ObtainVia(method = "getCreatedBy") String createdBy,
	                     @Builder.ObtainVia(method = "getCreatedDate") Date createdDate,
	                     @Builder.ObtainVia(method = "getLastModifiedBy") String lastModifiedBy,
	                     @Builder.ObtainVia(method = "getLastModifiedDate") Date lastModifiedDate,
	                     String name,
	                     String domainKey,
	                     String description,
	                     @Singular Map<String, String> attributes,
	                     @Builder.ObtainVia(method = "isActive") boolean active ) {
		setNewEntityId( newEntityId );
		setId( id );
		setCreatedBy( createdBy );
		setCreatedDate( createdDate );
		setLastModifiedBy( lastModifiedBy );
		setLastModifiedDate( lastModifiedDate );
		setName( name );
		setObjectId( objectId );
		setDomainKey( domainKey );
		setDescription( description );
		setActive( active );

		this.attributes = new TreeMap<>( attributes );
	}

	/**
	 * @param domain to check
	 * @return true if the domain represents {@link WebCmsDomain#NONE} (null value)
	 */
	public static boolean isNoDomain( WebCmsDomain domain ) {
		return domain == null;
	}

	/**
	 * @return the globally unique object id
	 */
	@Override
	public String getObjectId() {
		return objectId;
	}

	/**
	 * Manually set the unique object id.  If the id set does not start with the collection id, it will be prefixed.
	 *
	 * @param objectId to use
	 */
	public void setObjectId( String objectId ) {
		this.objectId = StringUtils.isEmpty( objectId ) ? null : WebCmsUtils.prefixObjectIdForCollection( objectId, COLLECTION_ID );
	}

	/**
	 * Set the unique domain key for this domain.
	 *
	 * @param domainKey identifying the domain
	 */
	public void setDomainKey( String domainKey ) {
		this.domainKey = domainKey;
		if ( getObjectId() == null && StringUtils.isNotEmpty( domainKey ) ) {
			setObjectId( domainKey );
		}
	}

	/**
	 * @return attribute value or null if not present (or value is null)
	 */
	public final String getAttribute( String attributeKey ) {
		return getAttributes().get( attributeKey );
	}

	/**
	 * @return attribute value, null if present and null value, defaultValue if not present
	 */
	public final String getAttribute( String attributeKey, String defaultValue ) {
		return getAttributes().getOrDefault( attributeKey, defaultValue );
	}

	/**
	 * @return true if the attribute key is present (value can be null)
	 */
	public boolean hasAttribute( String attributeKey ) {
		return getAttributes().containsKey( attributeKey );
	}

	@Override
	public String toString() {
		return "WebCmsDomain{" +
				"objectId='" + getObjectId() + "\'," +
				"domainKey='" + domainKey + '\'' +
				'}';
	}

	@SuppressWarnings({ "unused", "findbugs:EI_EXPOSE_REP2" })
	public static class WebCmsDomainBuilder
	{
		private boolean active = true;
	}
}
