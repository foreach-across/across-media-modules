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

package com.foreach.across.modules.webcms.domain.redirect;

import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * This endpoint represent a redirect to the configured target URL
 *
 * @author: Sander Van Loock
 * @since: 0.0.1
 */
@NotThreadSafe
@Entity
@Table(name = "wcm_remote_endpoint")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
public class WebCmsRemoteEndpoint extends WebCmsEndpoint
{
	/**
	 * The URL where this endpoint will redirect to.  According to
	 * {@link http://stackoverflow.com/questions/417142/what-is-the-maximum-length-of-a-url-in-different-browsers}
	 * the max size of the path is restricted to 2000 characters.
	 */
	@NotBlank
	@Column(name = "target_url")
	@Length(max = 2000)
	private String targetUrl;
}
