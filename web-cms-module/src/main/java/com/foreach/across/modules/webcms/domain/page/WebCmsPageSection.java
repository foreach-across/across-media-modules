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
 * Represents a single content section on a {@link WebCmsPage}.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@NotThreadSafe
@Entity
@Table(name = "wcm_page_section")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
public class WebCmsPageSection extends SettableIdAuditableEntity<WebCmsPage>
{
	@Id
	@GeneratedValue(generator = "seq_wcm_page_section_id")
	@GenericGenerator(
			name = "seq_wcm_page_section_id",
			strategy = AcrossSequenceGenerator.STRATEGY,
			parameters = {
					@org.hibernate.annotations.Parameter(name = "sequenceName", value = "seq_wcm_page_section_id"),
					@org.hibernate.annotations.Parameter(name = "allocationSize", value = "1")
			}
	)
	private Long id;

	/**
	 * Page this content section belongs to.
	 */
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "page_id")
	private WebCmsPage page;

	/**
	 * Name of this content section - unique within the scope of a page.
	 */
	@NotBlank
	@Length(max = 255)
	@Column
	private String name;

	/**
	 * Content of the page.
	 */
	@Column
	private String content;

	/**
	 * Index for sorting the sections of a page.
	 */
	@NotNull
	@Column(name = "sort_index")
	private int sortIndex = 0;
}
