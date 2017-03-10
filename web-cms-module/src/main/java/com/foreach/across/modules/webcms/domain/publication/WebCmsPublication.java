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

import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * A single publication for a set of articles or other publication-linked assets.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@NotThreadSafe
@Entity
@DiscriminatorValue("publication")
@Table(name = "wcm_publication")
@Getter
@Setter
@NoArgsConstructor
public class WebCmsPublication extends WebCmsAsset<WebCmsPublication>
{
	/**
	 * Unique descriptive name of the publication.
	 */
	@Column(name = "name", unique = true)
	@NotBlank
	@Length(max = 255)
	private String name;

	/**
	 * Unique key of the publication.
	 * Do not confuse with {@link #getAssetKey()} which is a globally unique key across all assets.
	 */
	@Column(name = "publication_key", unique = true)
	@NotBlank
	@Length(max = 255)
	private String publicationKey;

	@Builder(toBuilder = true)
	public WebCmsPublication( @Builder.ObtainVia(method = "getId") Long id,
	                          @Builder.ObtainVia(method = "getNewEntityId") Long newEntityId,
	                          @Builder.ObtainVia(method = "getAssetKey") String assetKey,
	                          @Builder.ObtainVia(method = "getCreatedBy") String createdBy,
	                          @Builder.ObtainVia(method = "getCreatedDate") Date createdDate,
	                          @Builder.ObtainVia(method = "getLastModifiedBy") String lastModifiedBy,
	                          @Builder.ObtainVia(method = "getLastModifiedDate") Date lastModifiedDate,
	                          String name,
	                          String publicationKey ) {
		super( id, newEntityId, assetKey, createdBy, createdDate, lastModifiedBy, lastModifiedDate );
		this.name = name;
		this.publicationKey = publicationKey;
	}

	//private WebCmsTagCollection tagCollection;
}
