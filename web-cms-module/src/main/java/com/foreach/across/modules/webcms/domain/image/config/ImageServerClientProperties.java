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

package com.foreach.across.modules.webcms.domain.image.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Contains configuration properties for auto-creation of a {@link com.foreach.imageserver.client.RemoteImageServerClient}.
 * If a {@link #hashToken} is specified, the client will get a {@link com.foreach.imageserver.client.ImageRequestHashBuilder} added.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Data
@ConfigurationProperties(prefix = "web-cms-module.images.image-server")
class ImageServerClientProperties
{
	/**
	 * Base URL for the remote ImageServer.
	 */
	private String url;

	/**
	 * Access token that should be used for REST services.
	 */
	private String accessToken;

	/**
	 * Sets the hash token that should be used when building image URLs.
	 */
	private String hashToken;
}
