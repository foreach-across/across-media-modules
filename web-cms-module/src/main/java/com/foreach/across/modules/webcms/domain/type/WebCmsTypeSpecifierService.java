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

/**
 * Service for retrieving {@link WebCmsTypeSpecifier} instances.
 * Inspects the multi-domain configuration to determine which domains should be investigated for finding the type specifiers.
 *
 * @author Arne Vandamme
 * @since 0.0.3
 */
public interface WebCmsTypeSpecifierService
{
	/**
	 * Finds the type specifier with that object id.
	 *
	 * @param objectId the type specifier has
	 * @return type specifier or {@code null} if not found
	 */
	WebCmsTypeSpecifier<?> getTypeSpecifier( String objectId );

	/**
	 * Finds the type specifier with that object id, assuming it is of the expected java type.
	 * If type exists but not of the specified type, an {@link IllegalArgumentException} will be thrown.
	 *
	 * @param objectId     the type specifier has
	 * @param expectedType the type should have
	 * @param <T>          expected type
	 * @return type specifier or {@code null} if not found
	 */
	<T extends WebCmsTypeSpecifier> T getTypeSpecifier( String objectId, Class<T> expectedType );

	/**
	 * Finds the type specifier represented by the type key, assuming it is of the expected java type.
	 * Requires all types to be registered in the {@link WebCmsTypeRegistry}.
	 * <p/>
	 * Will inspect the multi-domain configuration and use the current domain as well as fallback
	 * to no-domain if allowed for that type.
	 *
	 * @param typeKey      of the type
	 * @param expectedType output
	 * @param <T>          expected type
	 * @return type specifier or {@code null} if not found
	 */
	<T extends WebCmsTypeSpecifier> T getTypeSpecifierByKey( String typeKey, Class<T> expectedType );

	/**
	 * Finds the type specifier represented by the type key, assuming it is of the expected java type.
	 * Requires all types to be registered in the {@link WebCmsTypeRegistry}.
	 * <p/>
	 * Will inspect the multi-domain configuration and use the current domain as well as fallback
	 * to no-domain if allowed for that type.
	 *
	 * @param <T>          expected type
	 * @param typeKey      of the type
	 * @param expectedType output
	 * @param domain       to look for the type
	 * @return type specifier or {@code null} if not found
	 */
	<T extends WebCmsTypeSpecifier> T getTypeSpecifierByKey( String typeKey, Class<T> expectedType, WebCmsDomain domain );
}
