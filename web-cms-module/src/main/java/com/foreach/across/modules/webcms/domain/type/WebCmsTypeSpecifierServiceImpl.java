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

package com.foreach.across.modules.webcms.domain.type;

import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsMultiDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * @author Arne Vandamme
 * @since 0.0.3
 */
@Service
@RequiredArgsConstructor
class WebCmsTypeSpecifierServiceImpl implements WebCmsTypeSpecifierService
{
	private final WebCmsMultiDomainService multiDomainService;
	private final WebCmsTypeRegistry typeRegistry;
	private final WebCmsTypeSpecifierRepository typeSpecifierRepository;

	@Override
	public <T extends WebCmsTypeSpecifier> T getTypeSpecifier( String objectId, Class<T> expectedType ) {
		Assert.notNull( expectedType, "Expected type is required" );

		WebCmsTypeSpecifier<?> typeSpecifier = getTypeSpecifier( objectId );

		if ( typeSpecifier != null && !expectedType.isInstance( typeSpecifier ) ) {
			throw new IllegalArgumentException( "Object with objectId " + objectId + " exists but is not of type " + expectedType.getName() );
		}

		return expectedType.cast( typeSpecifier );
	}

	@Override
	public WebCmsTypeSpecifier<?> getTypeSpecifier( String objectId ) {
		return typeSpecifierRepository.findOneByObjectId( objectId );
	}

	@Override
	public <T extends WebCmsTypeSpecifier> T getTypeSpecifierByKey( String typeKey, Class<T> expectedType ) {
		return getTypeSpecifierByKey( typeKey, expectedType, multiDomainService.getCurrentDomainForType( expectedType ) );
	}

	@Override
	public <T extends WebCmsTypeSpecifier> T getTypeSpecifierByKey( String typeKey, Class<T> expectedType, WebCmsDomain domain ) {
		String objectType = typeRegistry.retrieveObjectType( expectedType ).orElseThrow(
				() -> new IllegalArgumentException( "Unknown WebCmsTypeSpecifier: " + expectedType + ", must be registered in the WebCmsTypeRegistry" )
		);

		WebCmsTypeSpecifier candidate = typeSpecifierRepository.findOneByObjectTypeAndTypeKeyAndDomain( objectType, typeKey, domain );

		if ( candidate == null && !WebCmsDomain.isNoDomain( domain ) && multiDomainService.isNoDomainAllowed( expectedType ) ) {
			candidate = typeSpecifierRepository.findOneByObjectTypeAndTypeKeyAndDomain( objectType, typeKey, WebCmsDomain.NONE );
		}

		return expectedType.cast( candidate );
	}
}
