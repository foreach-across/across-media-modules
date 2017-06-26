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
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

import static com.foreach.across.modules.webcms.domain.publication.WebCmsPublication.OBJECT_TYPE;

/**
 * A single publication for a set of articles or other publication-linked assets.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@NotThreadSafe
@Entity
@DiscriminatorValue(OBJECT_TYPE)
@Table(name = "wcm_publication")
@Getter
@Setter
@NoArgsConstructor
public class WebCmsPublication extends WebCmsAsset<WebCmsPublication>
{
	/**
	 * Object type name (discriminator value).
	 */
	public static final String OBJECT_TYPE = "publication";

	/**
	 * Prefix that all asset ids for a WebCmsPublication should have.
	 */
	public static final String COLLECTION_ID = "wcm:asset:publication";

	/**
	 * Unique descriptive name of the publication.
	 */
	@NotBlank
	@Length(max = 255)
	@Column(name = "name", unique = true)
	private String name;

	/**
	 * Unique key of the publication.
	 * Do not confuse with {@link #getObjectId()} which is a globally unique key across all assets.
	 */
	@NotBlank
	@Length(max = 255)
	@Column(name = "publication_key", unique = true)
	private String publicationKey;

	/**
	 * Type of the publication.
	 */
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "publication_type_id")
	private WebCmsPublicationType publicationType;

	/**
	 * Page that is considered the template for an article detail.
	 * This page should be loaded when rendering an article from this publication, and the
	 * canonicalPath of the page will be used when generating the default urls.
	 */
	@ManyToOne
	@JoinColumn(name = "article_page_id")
	private WebCmsPage articleTemplatePage;

	//private WebCmsTagCollection tagCollection;

	@Builder(toBuilder = true)
	protected WebCmsPublication( @Builder.ObtainVia(method = "getId") Long id,
	                             @Builder.ObtainVia(method = "getNewEntityId") Long newEntityId,
	                             @Builder.ObtainVia(method = "getObjectId") String objectId,
	                             @Builder.ObtainVia(method = "getCreatedBy") String createdBy,
	                             @Builder.ObtainVia(method = "getCreatedDate") Date createdDate,
	                             @Builder.ObtainVia(method = "getLastModifiedBy") String lastModifiedBy,
	                             @Builder.ObtainVia(method = "getLastModifiedDate") Date lastModifiedDate,
	                             @Builder.ObtainVia(method = "isPublished") boolean published,
	                             @Builder.ObtainVia(method = "getPublicationDate") Date publicationDate,
	                             String name,
	                             String publicationKey,
	                             WebCmsPage articleTemplatePage ) {
		super( id, newEntityId, objectId, createdBy, createdDate, lastModifiedBy, lastModifiedDate, published, publicationDate );
		this.name = name;
		this.publicationKey = publicationKey;
		this.articleTemplatePage = articleTemplatePage;
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
