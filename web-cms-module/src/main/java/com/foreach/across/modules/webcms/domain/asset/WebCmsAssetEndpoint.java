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

package com.foreach.across.modules.webcms.domain.asset;

import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint;
import lombok.*;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * This endpoint represents a {@link WebCmsAsset}.
 *
 * @author Sander Van Loock
 * @since 0.0.1
 */
@NotThreadSafe
@Entity
@Table(name = "wcm_asset_endpoint")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
public class WebCmsAssetEndpoint<T extends WebCmsAsset> extends WebCmsEndpoint
{
	@NotNull
	@JoinColumn(name = "asset_id")
	@ManyToOne(targetEntity = WebCmsAsset.class)
	private T asset;
}
