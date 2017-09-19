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

package com.foreach.across.modules.webcms.config;

import com.foreach.across.core.annotations.OrderInModule;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.webcms.domain.WebCmsObject;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Contains the base configuration for the admin UI.
 *
 * @author Arne Vandamme
 * @since 0.0.3
 */
@OrderInModule(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnAdminUI
@Configuration
class WebCmsAdminUiConfiguration implements EntityConfigurer
{
	@Override
	public void configure( EntitiesConfigurationBuilder entities ) {
		// Object ID is generally not writable and is hidden
		entities.assignableTo( WebCmsObject.class )
		        .properties(
				        props -> props.property( "objectId" ).order( Ordered.HIGHEST_PRECEDENCE ).writable( false ).hidden( true )
		        );
	}
}
