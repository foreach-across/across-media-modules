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

import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Maps the type group (String) to the actual {@link WebCmsTypeSpecifier} type.
 * Required by the {@link WebCmsTypeSpecifierImporter} for importing type instances.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Service
public final class WebCmsTypeRegistry
{
	private final Map<String, Class<? extends WebCmsTypeSpecifier>> typesForGroup = new HashMap<>();
	private final Map<String, String> groupsForType = new HashMap<>();
	private final Map<String, Supplier> suppliersForImplementationType = new HashMap<>();

	/**
	 * Registers an object type to an implementation type along with a {@link Supplier} that returns a new instance.
	 *
	 * @param objectType          name
	 * @param implementationType class
	 * @param supplier           that returns a new instance
	 * @param <U>                actual type
	 */
	public <U extends WebCmsTypeSpecifier> void register( @NonNull String objectType, @NonNull Class<U> implementationType, @NonNull Supplier<U> supplier ) {
		typesForGroup.put( objectType, implementationType );
		suppliersForImplementationType.put( implementationType.getName(), supplier );
		groupsForType.put( implementationType.getName(), objectType );
	}

	/**
	 * @param objectType for which to get the implementation type
	 * @return implementation type or {@code null} if not exists
	 */
	public Optional<Class<? extends WebCmsTypeSpecifier>> retrieveTypeSpecifierClass( @NonNull String objectType ) {
		return Optional.ofNullable( typesForGroup.get( objectType ) );
	}

	@SuppressWarnings("unchecked")
	public <U extends WebCmsTypeSpecifier> Optional<Supplier<U>> retrieveSupplier( @NonNull Class<U> implementationType ) {
		return Optional.ofNullable( suppliersForImplementationType.get( implementationType.getName() ) );
	}

	public <U extends WebCmsTypeSpecifier> Optional<String> retrieveObjectType( @NonNull Class<U> implementationType ) {
		return Optional.ofNullable( groupsForType.get( implementationType.getName() ) );
	}
}
