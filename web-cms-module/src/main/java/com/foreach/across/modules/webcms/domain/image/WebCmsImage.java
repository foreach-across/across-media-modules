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

package com.foreach.across.modules.webcms.domain.image;

import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

/**
 * A single image asset.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@NotThreadSafe
@Entity
@DiscriminatorValue("image")
@Table(name = "wcm_image")
@NoArgsConstructor
@Getter
@Setter
public class WebCmsImage extends WebCmsAsset<WebCmsImage>
{
	/**
	 * Prefix that all object ids for a WebCmsImage should have.
	 */
	public static final String COLLECTION_ID = "wcm:asset:image";

	/**
	 * Title of the article. Used in previews, list views etc.
	 */
	@NotBlank
	@Column(name = "name")
	@Length(max = 255)
	private String name;

	/**
	 * External system id of the image.
	 */
	@Column(name = "external_id")
	@Length(max = 255)
	private String externalId;

	@Builder(toBuilder = true)
	protected WebCmsImage( @Builder.ObtainVia(method = "getId") Long id,
	                       @Builder.ObtainVia(method = "getNewEntityId") Long newEntityId,
	                       @Builder.ObtainVia(method = "getAssetId") String assetId,
	                       @Builder.ObtainVia(method = "isPublished") boolean published,
	                       @Builder.ObtainVia(method = "getPublicationDate") Date publicationDate,
	                       @Builder.ObtainVia(method = "getCreatedBy") String createdBy,
	                       @Builder.ObtainVia(method = "getCreatedDate") Date createdDate,
	                       @Builder.ObtainVia(method = "getLastModifiedBy") String lastModifiedBy,
	                       @Builder.ObtainVia(method = "getLastModifiedDate") Date lastModifiedDate,
	                       String name,
	                       String externalId ) {
		super( id, newEntityId, assetId, published, publicationDate, createdBy, createdDate, lastModifiedBy, lastModifiedDate );
		this.name = name;
		this.externalId = externalId;
	}

	@SuppressWarnings("all")
	public static class WebCmsImageBuilder
	{
		private String assetId = UUID.randomUUID().toString();
	}

	@Override
	public String toString() {
		return "WebCmsImage{" +
				"assetId='" + getAssetId() + "\'," +
				"externalId='" + name + '\'' +
				'}';
	}
}
