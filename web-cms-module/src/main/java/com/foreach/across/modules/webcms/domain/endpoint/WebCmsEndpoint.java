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

package com.foreach.across.modules.webcms.domain.endpoint;

import com.foreach.across.modules.hibernate.business.SettableIdBasedEntity;
import com.foreach.across.modules.hibernate.id.AcrossSequenceGenerator;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * An endpoint can be anything that can be accessed by a collection or URLs.  The endpoint should contain all the data needed to
 * display itself when accessing.
 *
 * @author Sander Van Loock
 * @since 0.0.1
 */
@NotThreadSafe
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@Entity
@Table(name = "wcm_endpoint")
@Inheritance(strategy = InheritanceType.JOINED)
@SuppressWarnings("squid:S2160")
public abstract class WebCmsEndpoint extends SettableIdBasedEntity<WebCmsEndpoint>
{
	@Id
	@GeneratedValue(generator = "seq_wcm_endpoint_id")
	@GenericGenerator(
			name = "seq_wcm_endpoint_id",
			strategy = AcrossSequenceGenerator.STRATEGY,
			parameters = {
					@org.hibernate.annotations.Parameter(name = "sequenceName", value = "seq_wcm_endpoint_id"),
					@org.hibernate.annotations.Parameter(name = "allocationSize", value = "10")
			}
	)
	@Column(name = "id")
	protected Long id;

	@OneToMany(mappedBy = "endpoint")
	protected Collection<WebCmsUrl> urls = Collections.emptySet();

	/**
	 * Get the primary url for this {@code WebCmsEndpoint}
	 *
	 * @see WebCmsUrl#isPrimary()
	 */
	public Optional<WebCmsUrl> getPrimaryUrl() {
		return getUrls().stream()
		                .filter( WebCmsUrl::isPrimary )
		                .findFirst();
	}

	public Optional<WebCmsUrl> getUrlWithPath( String path ) {
		return getUrls().stream()
		                .filter( url -> StringUtils.equalsIgnoreCase( url.getPath(), path ) )
		                .findFirst();
	}
}
