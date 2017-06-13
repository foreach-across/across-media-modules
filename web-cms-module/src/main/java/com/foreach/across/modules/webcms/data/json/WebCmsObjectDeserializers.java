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

package com.foreach.across.modules.webcms.data.json;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.foreach.across.modules.webcms.data.WebCmsDataConversionService;
import com.foreach.across.modules.webcms.domain.WebCmsObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Automatically registers a new {@link WebCmsObjectDeserializer} for any java type that implements {@link WebCmsObject}.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Component
@RequiredArgsConstructor
class WebCmsObjectDeserializers extends SimpleDeserializers
{
	private final WebCmsDataConversionService dataConversionService;

	@SuppressWarnings( "unchecked" )
	@Override
	public JsonDeserializer<?> findBeanDeserializer( JavaType type, DeserializationConfig config, BeanDescription beanDesc ) throws JsonMappingException {
		JsonDeserializer found = super.findBeanDeserializer( type, config, beanDesc );

		if ( found == null ) {
			Class<?> rawClass = type.getRawClass();

			if ( WebCmsObject.class.isAssignableFrom( rawClass ) ) {
				found = new WebCmsObjectDeserializer( rawClass, dataConversionService );
				addDeserializer( rawClass, found );
			}
		}

		return found;
	}

}
