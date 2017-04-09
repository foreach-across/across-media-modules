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
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.*;
import java.util.Date;

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
@Table(name = "wcm_type")
@Access(AccessType.FIELD)
@Inheritance(strategy = InheritanceType.JOINED)
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
	 * Key of the type, should be unique within the object type.
	 */
	@Column(name = "type_key")
	@NotBlank
	@Length(max = 255)
	private String typeKey;

	public WebCmsTypeSpecifier() {
		super();
	}

	protected WebCmsTypeSpecifier( Long id,
	                            Long newEntityId,
	                            String objectId,
	                            String createdBy,
	                            Date createdDate,
	                            String lastModifiedBy,
	                            Date lastModifiedDate, String name, String typeKey ) {
		super( id, newEntityId, objectId, createdBy, createdDate, lastModifiedBy, lastModifiedDate );
		this.name = name;
		this.typeKey = typeKey;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{" +
				"objectId='" + getObjectId() + '\'' +
				", typeKey='" + typeKey + '\'' +
				'}';
	}
}
