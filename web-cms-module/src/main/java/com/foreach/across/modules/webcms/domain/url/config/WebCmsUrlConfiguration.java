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

package com.foreach.across.modules.webcms.domain.url.config;

import com.foreach.across.modules.entity.EntityAttributes;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.util.EnumSet;

/**
 * Base configuration for the {@link com.foreach.across.modules.webcms.domain.url.WebCmsUrl} entity.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@ConditionalOnAdminUI
@Configuration
class WebCmsUrlConfiguration implements EntityConfigurer
{
	@Override
	public void configure( EntitiesConfigurationBuilder entities ) {
		entities.withType( WebCmsUrl.class )
		        .properties(
				        props -> props.property( "httpStatus" )
				                      .attribute(
						                      EntityAttributes.OPTIONS_ALLOWED_VALUES,
						                      EnumSet.of( HttpStatus.OK, HttpStatus.MOVED_PERMANENTLY, HttpStatus.MOVED_TEMPORARILY, HttpStatus.NOT_FOUND )
				                      )
		        );
	}
}
