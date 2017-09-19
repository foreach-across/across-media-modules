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

package com.foreach.across.modules.webcms.domain.type.config;

import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifier;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifierLink;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@ConditionalOnAdminUI
@Configuration
class WebCmsTypeSpecifierConfiguration implements EntityConfigurer
{
	@Override
	public void configure( EntitiesConfigurationBuilder entities ) {
		entities.assignableTo( WebCmsTypeSpecifier.class )
		        .hide()
		        .properties(
				        props -> props.property( "attributes" ).hidden( true ).and()
		        )
		        .updateFormView( fvb -> fvb.showProperties( "name", "typeKey", "created", "lastModified" ) );

		entities.assignableTo( WebCmsTypeSpecifierLink.class ).hide();
	}
}
