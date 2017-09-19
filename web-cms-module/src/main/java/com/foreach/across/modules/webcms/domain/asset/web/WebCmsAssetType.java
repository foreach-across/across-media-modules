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

package com.foreach.across.modules.webcms.domain.asset.web;

import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifier;
import lombok.*;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.util.Date;
import java.util.Map;

/**
 * Type specifier for a particular asset type
 *
 * @author Raf Ceuls
 * @since 0.0.2
 */
@NotThreadSafe
@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
public abstract class WebCmsAssetType<T extends WebCmsTypeSpecifier<T>> extends WebCmsTypeSpecifier<T>
{
	protected WebCmsAssetType( @Builder.ObtainVia(method = "getId") Long id,
	                           @Builder.ObtainVia(method = "getNewEntityId") Long newEntityId,
	                           @Builder.ObtainVia(method = "getObjectId") String objectId,
	                           @Builder.ObtainVia(method = "getCreatedBy") String createdBy,
	                           @Builder.ObtainVia(method = "getCreatedDate") Date createdDate,
	                           @Builder.ObtainVia(method = "getLastModifiedBy") String lastModifiedBy,
	                           @Builder.ObtainVia(method = "getLastModifiedDate") Date lastModifiedDate,
	                           @Builder.ObtainVia(method = "getDomain") WebCmsDomain domain,
	                           @Builder.ObtainVia(method = "getName") String name,
	                           @Builder.ObtainVia(method = "getTypeKey") String typeKey,
	                           @Builder.ObtainVia(method = "getDescription") String description,
	                           @Singular @Builder.ObtainVia(method = "getAttributes") Map<String, String> attributes ) {
		super( id, newEntityId, objectId, createdBy, createdDate, lastModifiedBy, lastModifiedDate, domain, name, typeKey, description, attributes );
	}

	/**
	 * @return does the associated entity have an endpoint? Defaults to true.
	 */
	@Transient
	public boolean hasEndpoint() {
		return getBooleanAttribute( "hasEndpoint", true );
	}

	/**
	 * @return a boolean representing if the page is publishable or not. Default is true.
	 */
	@Transient
	public boolean isPublishable() {
		return getBooleanAttribute( "isPublishable", true );
	}

}