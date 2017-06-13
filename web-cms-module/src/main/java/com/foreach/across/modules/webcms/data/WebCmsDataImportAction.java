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

package com.foreach.across.modules.webcms.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

/**
 * Represents the default action to perform with the data represented by a {@link WebCmsDataEntry}.
 * Can be present in a data collection as the {@code wcm:action} key.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@RequiredArgsConstructor
public enum WebCmsDataImportAction
{
	/**
	 * Only create the entities if they do not yet exist.
	 */
	CREATE( "create" ),

	/**
	 * Create the entities if they do not yet exist, or update any existing entities.
	 * Updating will only apply the defined properties and will keep all others untouched.
	 */
	CREATE_OR_UPDATE( "create-update" ),

	/**
	 * Only update the entities, do nothing if they do not yet exist.
	 */
	UPDATE( "update" ),

	/**
	 * Delete the entities.
	 */
	DELETE( "delete" ),

	/**
	 * Replace the entities.  This is should wipe all values of the existing entities except
	 * ID properties (like objectId).
	 */
	REPLACE( "replace" );

	public static final String ATTRIBUTE_NAME = "wcm:action";

	@Getter
	private final String attributeValue;

	/**
	 * Retrieve the action represented by an attribute value.
	 *
	 * @param attributeValue that represents the action
	 * @return action or null if could not be mapped
	 */
	public WebCmsDataImportAction fromAttributeValue( String attributeValue ) {
		return Stream.of( values() )
		             .filter( v -> v.name().equalsIgnoreCase( attributeValue ) )
		             .findFirst()
		             .orElse( null );
	}
}
