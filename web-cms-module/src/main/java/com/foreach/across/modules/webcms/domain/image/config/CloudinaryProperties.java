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

import java.util.HashMap;
import java.util.Map;

/**
 * Contains configuration properties for auto-creation of a {@link com.cloudinary.Cloudinary}.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Data
@ConfigurationProperties(prefix = "webCmsModule.images.cloudinary")
class CloudinaryProperties
{
	/**
	 * Cloudinary cloud name.
	 */
	private String cloudName;

	/**
	 * Cloudinary API key for secure operations (eg. file upload).
	 */
	private String apiKey;

	/**
	 * Cloudinary API secret for secure operations (eg. file upload).
	 */
	private String apiSecret;

	/**
	 * Additional settings to be configured on the Cloudinary.
	 */
	Map<String, String> settings = new HashMap<>();
}
