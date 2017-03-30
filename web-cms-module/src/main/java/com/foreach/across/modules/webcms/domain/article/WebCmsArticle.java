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

package com.foreach.across.modules.webcms.domain.article;

import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.publication.WebCmsPublication;
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
import java.util.UUID;

/**
 * A single article from a {@link com.foreach.across.modules.webcms.domain.publication.WebCmsPublication}.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@NotThreadSafe
@Entity
@DiscriminatorValue("article")
@Table(name = "wcm_article")
@NoArgsConstructor
@Getter
@Setter
public class WebCmsArticle extends WebCmsAsset<WebCmsArticle>
{
	/**
	 * Prefix that all object ids for a WebCmsArticle should have.
	 */
	public static final String COLLECTION_ID = "wcm:asset:article";

	/**
	 * Publication this article belongs to.
	 */
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "publication_id")
	private WebCmsPublication publication;

	/**
	 * Title of the article. Used in previews, list views etc.
	 */
	@Column(name = "title")
	@NotBlank
	@Length(max = 255)
	private String title;

	/**
	 * Sub title of the article.
	 */
	@Column(name = "sub_title")
	@Length(max = 255)
	private String subTitle;

	/**
	 * Short description of the article contents.
	 */
	@Column(name = "description")
	@Length(max = 255)
	private String description;

	@Builder(toBuilder = true)
	protected WebCmsArticle( @Builder.ObtainVia(method = "getId") Long id,
	                         @Builder.ObtainVia(method = "getNewEntityId") Long newEntityId,
	                         @Builder.ObtainVia(method = "getAssetId") String assetId,
	                         @Builder.ObtainVia(method = "getCreatedBy") String createdBy,
	                         @Builder.ObtainVia(method = "getCreatedDate") Date createdDate,
	                         @Builder.ObtainVia(method = "getLastModifiedBy") String lastModifiedBy,
	                         @Builder.ObtainVia(method = "getLastModifiedDate") Date lastModifiedDate,
	                         WebCmsPublication publication,
	                         String title,
	                         String subTitle,
	                         String description ) {
		super( id, newEntityId, assetId, createdBy, createdDate, lastModifiedBy, lastModifiedDate );
		this.publication = publication;
		this.title = title;
		this.subTitle = subTitle;
		this.description = description;
	}

	@SuppressWarnings("all")
	public static class WebCmsArticleBuilder
	{
		private String assetId = UUID.randomUUID().toString();
	}

	@Override
	public String toString() {
		return "WebCmsArticle{" +
				"assetId='" + getAssetId() + "\'," +
				"title='" + title + '\'' +
				'}';
	}
}
