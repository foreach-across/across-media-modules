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

import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifier;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;
import java.util.Map;

/**
 * Represents a type of {@link WebCmsComponent}.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@NotThreadSafe
@Entity
@DiscriminatorValue(WebCmsComponentType.OBJECT_TYPE)
@Table(name = "wcm_component_type")
@Getter
@Setter
@NoArgsConstructor
public class WebCmsComponentType extends WebCmsTypeSpecifier<WebCmsComponentType>
{
	/**
	 * Object type name (discriminator value).
	 */
	public static final String OBJECT_TYPE = "component";

	/**
	 * Prefix that all object ids of a WebCmsComponentType have.
	 */
	public static final String COLLECTION_ID = "wcm:type:component";

	/**
	 * Description of the component type.
	 */
	@Column(name = "description")
	@Length(max = 500)
	private String description;

	@Builder(toBuilder = true)
	protected WebCmsComponentType( @Builder.ObtainVia(method = "getId") Long id,
	                               @Builder.ObtainVia(method = "getNewEntityId") Long newEntityId,
	                               @Builder.ObtainVia(method = "getObjectId") String objectId,
	                               @Builder.ObtainVia(method = "getCreatedBy") String createdBy,
	                               @Builder.ObtainVia(method = "getCreatedDate") Date createdDate,
	                               @Builder.ObtainVia(method = "getLastModifiedBy") String lastModifiedBy,
	                               @Builder.ObtainVia(method = "getLastModifiedDate") Date lastModifiedDate,
	                               @Builder.ObtainVia(method = "getName") String name,
	                               @Builder.ObtainVia(method = "getTypeKey") String typeKey,
	                               @Singular @Builder.ObtainVia(method = "getAttributes") Map<String, String> attributes,
	                               String description ) {
		super( id, newEntityId, objectId, createdBy, createdDate, lastModifiedBy, lastModifiedDate, name, typeKey, attributes );

		setDescription( description );
	}

	@Override
	public final String getObjectType() {
		return OBJECT_TYPE;
	}

	@Override
	protected final String getObjectCollectionId() {
		return COLLECTION_ID;
	}
}
