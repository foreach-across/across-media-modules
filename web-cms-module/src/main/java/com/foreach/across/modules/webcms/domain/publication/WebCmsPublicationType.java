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

package com.foreach.across.modules.webcms.domain.publication;

import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifier;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

/**
 * A type specifier for a {@link WebCmsPublication}.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@NotThreadSafe
@Entity
@DiscriminatorValue(WebCmsPublicationType.TYPE_GROUP)
@Table(name = "wcm_publication_type")
@Getter
@Setter
@NoArgsConstructor
public class WebCmsPublicationType extends WebCmsTypeSpecifier<WebCmsPublicationType>
{
	/**
	 * Type group name.
	 */
	public static final String TYPE_GROUP = "publication";

	/**
	 * Prefix that all unique keys for a WebCmsPublicationType should have.
	 */
	public static final String COLLECTION_ID = "wcm:type:publication";

	//private WebCmsTagCollection tagCollection;

	@Override
	public final String getTypeGroup() {
		return TYPE_GROUP;
	}

	@Override
	protected final String getTypeCollectionId() {
		return COLLECTION_ID;
	}

	@Builder(toBuilder = true)
	public WebCmsPublicationType( @Builder.ObtainVia(method = "getId") Long id,
	                              @Builder.ObtainVia(method = "getNewEntityId") Long newEntityId,
	                              @Builder.ObtainVia(method = "getUniqueKey") String uniqueKey,
	                              @Builder.ObtainVia(method = "getName") String name,
	                              @Builder.ObtainVia(method = "getTypeKey") String typeKey,
	                              @Builder.ObtainVia(method = "getCreatedBy") String createdBy,
	                              @Builder.ObtainVia(method = "getCreatedDate") Date createdDate,
	                              @Builder.ObtainVia(method = "getLastModifiedBy") String lastModifiedBy,
	                              @Builder.ObtainVia(method = "getLastModifiedDate") Date lastModifiedDate ) {
		super( id, newEntityId, uniqueKey, name, typeKey, createdBy, createdDate, lastModifiedBy, lastModifiedDate );
	}

	@SuppressWarnings("all")
	public static class WebCmsPublicationTypeBuilder
	{
		private String uniqueKey = UUID.randomUUID().toString();
	}
}
