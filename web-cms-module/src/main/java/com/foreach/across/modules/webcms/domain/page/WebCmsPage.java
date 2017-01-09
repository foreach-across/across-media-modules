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

import com.foreach.across.modules.hibernate.business.SettableIdAuditableEntity;
import com.foreach.across.modules.hibernate.id.AcrossSequenceGenerator;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Main entity representing a custom - static - web page.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@NotThreadSafe
@Entity
@Table(name = "wcm_page")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@SuppressWarnings("squid:S2160")
public class WebCmsPage extends SettableIdAuditableEntity<WebCmsPage>
{
	@Id
	@GeneratedValue(generator = "seq_wcm_page_id")
	@GenericGenerator(
			name = "seq_wcm_page_id",
			strategy = AcrossSequenceGenerator.STRATEGY,
			parameters = {
					@org.hibernate.annotations.Parameter(name = "sequenceName", value = "seq_wcm_page_id"),
					@org.hibernate.annotations.Parameter(name = "allocationSize", value = "1")
			}
	)
	private Long id;

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

	@SuppressWarnings( "all" )
	public static class WebCmsPageBuilder {
		private boolean pathSegmentGenerated = true;
		private boolean canonicalPathGenerated = true;
	}
}
