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

import com.foreach.across.modules.hibernate.id.AcrossSequenceGenerator;
import com.foreach.across.modules.webcms.domain.WebCmsObjectInheritanceSuperClass;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.SortNatural;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.*;
import javax.validation.constraints.Pattern;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.foreach.across.modules.webcms.domain.WebCmsObjectInheritanceSuperClass.DISCRIMINATOR_COLUMN;

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
@NoArgsConstructor
@Table(name = "wcm_type")
@Access(AccessType.FIELD)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = DISCRIMINATOR_COLUMN, discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
public abstract class WebCmsTypeSpecifier<T extends WebCmsTypeSpecifier<T>> extends WebCmsObjectInheritanceSuperClass<T>
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

	/**
	 * Name of the type, should be unique within the object type.
	 */
	@Column(name = "name")
	@NotBlank
	@Length(max = 255)
	private String name;

	/**
	 * Key of the type, should be unique within the object type and domain.
	 * If the specified {@link WebCmsTypeSpecifier} is not {@link com.foreach.across.modules.webcms.domain.domain.WebCmsDomainBound}
	 * then the key has to be unique.
	 */
	@Column(name = "type_key")
	@NotBlank
	@Length(max = 255)
	@Pattern(regexp = "[^\\s]*")
	private String typeKey;

	/**
	 * Description of the component type.
	 */
	@Column(name = "description")
	@Length(max = 500)
	private String description;

	/**
	 * Attributes of the type specifier, simple key values usually used for rendering.
	 */
	@ElementCollection(fetch = FetchType.EAGER)
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	@CollectionTable(name = "wcm_type_attributes", joinColumns = @JoinColumn(name = "type_id"))
	@MapKeyColumn(name = "attribute_key")
	@Column(name = "attribute_value")
	@SortNatural
	private SortedMap<String, String> attributes = new TreeMap<>();

	protected WebCmsTypeSpecifier( Long id,
	                               Long newEntityId,
	                               String objectId,
	                               String createdBy,
	                               Date createdDate,
	                               String lastModifiedBy,
	                               Date lastModifiedDate,
	                               WebCmsDomain domain,
	                               String name,
	                               String typeKey,
	                               String description,
	                               Map<String, String> attributes ) {
		super( id, newEntityId, objectId, createdBy, createdDate, lastModifiedBy, lastModifiedDate, domain );
		this.name = name;
		this.description = description;
		this.attributes = new TreeMap<>( attributes );
		this.typeKey = typeKey;
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

	protected final boolean getBooleanAttribute( String attributeKey, boolean defaultValue ) {
		String action = getAttributes().get( attributeKey );
		if ( StringUtils.isEmpty( action ) ) {
			return defaultValue;
		}
		return BooleanUtils.toBoolean( action );
	}

	/**
	 * @return true if the attribute key is present (value can be null)
	 */
	public boolean hasAttribute( String attributeKey ) {
		return getAttributes().containsKey( attributeKey );
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{" +
				"objectId='" + getObjectId() + '\'' +
				", typeKey='" + typeKey + '\'' +
				'}';
	}
}