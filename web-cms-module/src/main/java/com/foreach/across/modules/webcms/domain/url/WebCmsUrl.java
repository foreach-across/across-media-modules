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

package com.foreach.across.modules.webcms.domain.url;

import com.foreach.across.modules.hibernate.business.SettableIdBasedEntity;
import com.foreach.across.modules.hibernate.id.AcrossSequenceGenerator;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.http.HttpStatus;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Entity for targeting a specific endpoint
 *
 * @author Sander Van Loock
 * @since 0.0.1
 */
@NotThreadSafe
@Entity
@Table(name = "wcm_url")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@SuppressWarnings("squid:S2160")
public class WebCmsUrl extends SettableIdBasedEntity<WebCmsUrl>
{
	@Id
	@GeneratedValue(generator = "seq_wcm_url_id")
	@GenericGenerator(
			name = "seq_wcm_url_id",
			strategy = AcrossSequenceGenerator.STRATEGY,
			parameters = {
					@org.hibernate.annotations.Parameter(name = "sequenceName", value = "seq_wcm_url_id"),
					@org.hibernate.annotations.Parameter(name = "allocationSize", value = "10")
			}
	)
	private Long id;

	/**
	 * The path for this URL.  The max size of the path is restricted to 255 characters so unique constraint on this
	 * field can be enforced on INNODB databases.
	 */
	@NotBlank
	@Length(max = 255)
	@Column(unique = true)
	private String path;

	/**
	 * HTTP status for this URL.  The status code of the HttpStatus enum is persisted.
	 *
	 * @see org.springframework.http.HttpStatus
	 */
	@NotNull
	@Type(type = "com.foreach.across.modules.webcms.domain.url.HttpStatusType")
	@Column(name = "http_status")
	private HttpStatus httpStatus;

	/**
	 * WebCmsEndpoint for this URL.  Will be used to get the correct content that belongs to this URL.
	 */
	@NotNull
	@ManyToOne
	@JoinColumn(name = "endpoint_id")
	private WebCmsEndpoint endpoint;

	/**
	 * The primary {@code WebCmsUrl} will be used for all redirection targets and should have a
	 * status code in the HTTP series {@link org.springframework.http.HttpStatus.Series#SUCCESSFUL}
	 */
	@NotNull
	@Column(name = "is_primary")
	private boolean primary;

	/**
	 * This fields controls whether or not the primary url will be updated whenever updating the path segment.
	 */
	@NotNull
	@Column(name = "is_primary_locked")
	private boolean primaryLocked;
}
