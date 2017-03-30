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

package com.foreach.across.modules.webcms.domain.page;

import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.infrastructure.WebCmsUtils;
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
 * Main entity representing a custom - static - web page.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@NotThreadSafe
@Entity
@DiscriminatorValue("page")
@Table(name = "wcm_page")
@NoArgsConstructor
@Getter
@Setter
@SuppressWarnings("squid:S2160")
public class WebCmsPage extends WebCmsAsset<WebCmsPage>
{
	/**
	 * Prefix that all asset ids for a WebCmsPage should have.
	 */
	public static final String COLLECTION_ID = "wcm:asset:page";

	/**
	 * Title of the page.
	 */
	@Column
	@NotBlank
	@Length(max = 255)
	private String title;

	/**
	 * Parent page.  Will determine the administrative hierarchy of pages, but properties
	 * from the parent might also be inherited or used for generation of values (depending on settings).
	 */
	@ManyToOne
	@JoinColumn(name = "parent_id")
	private WebCmsPage parent;

	/**
	 * Path segment.  Used as an identification in the hierarchical context.
	 * Every page should have a unique path segment relative to its parents' path, resulting in a globally
	 * unique path containing all its ancestors.
	 */
	@Column(name = "path_segment")
	@NotNull
	@Length(max = 255)
	private String pathSegment;

	/**
	 * Should the path segment be generated based on the title.
	 */
	@Column(name = "path_segment_generated")
	private boolean pathSegmentGenerated = true;

	/**
	 * Fully qualified canonical path of this page, usually corresponds with the path relative within
	 * the application.  The canonical path is unique for every page and will always start with a /.
	 */
	@Column(name = "canonical_path")
	@NotNull
	@Length(max = 500)
	private String canonicalPath;

	/**
	 * Should the canonical path be generated based on the path segment and ancestor pages.
	 */
	@Column(name = "canonical_path_generated")
	private boolean canonicalPathGenerated = true;

	/**
	 * Template to use when rendering the page.
	 */
	@Column
	@Length(max = 255)
	private String template;

	@Override
	public final void setAssetId( String assetId ) {
		super.setAssetId( WebCmsUtils.prefixUniqueKeyForCollection( assetId, COLLECTION_ID ) );
	}

	@Builder(toBuilder = true)
	protected WebCmsPage( @Builder.ObtainVia(method = "getId") Long id,
	                      @Builder.ObtainVia(method = "getNewEntityId") Long newEntityId,
	                      @Builder.ObtainVia(method = "getAssetId") String assetId,
	                      @Builder.ObtainVia(method = "getCreatedBy") String createdBy,
	                      @Builder.ObtainVia(method = "getCreatedDate") Date createdDate,
	                      @Builder.ObtainVia(method = "getLastModifiedBy") String lastModifiedBy,
	                      @Builder.ObtainVia(method = "getLastModifiedDate") Date lastModifiedDate,
	                      String title,
	                      WebCmsPage parent,
	                      String pathSegment,
	                      boolean pathSegmentGenerated,
	                      String canonicalPath,
	                      boolean canonicalPathGenerated,
	                      String template ) {
		super( id, newEntityId, assetId, createdBy, createdDate, lastModifiedBy, lastModifiedDate );
		this.title = title;
		this.parent = parent;
		this.pathSegment = pathSegment;
		this.pathSegmentGenerated = pathSegmentGenerated;
		this.canonicalPath = canonicalPath;
		this.canonicalPathGenerated = canonicalPathGenerated;
		this.template = template;
	}

	@SuppressWarnings("all")
	public static class WebCmsPageBuilder
	{
		private String assetId = UUID.randomUUID().toString();
		private boolean pathSegmentGenerated = true;
		private boolean canonicalPathGenerated = true;
	}

	@Override
	public String toString() {
		return "WebCmsPage{assetId='" +
				getAssetId() + "'," +
				"canonicalPath='" + canonicalPath + '\'' +
				'}';
	}
}
