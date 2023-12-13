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

package com.foreach.across.modules.webcms.domain;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */

public interface WebCmsObject
{
	/**
	 * @return the globally unique id of this object in the entire repository
	 */
	String getObjectId();

	/**
	 * @return true if we're dealing with a new entity and the object id might not be set
	 */
	boolean isNew();

	/**
	 * Wraps an objectId as representing an existing {@link WebCmsObject}.
	 *
	 * @param objectId string
	 * @return object instance
	 */
	static WebCmsObject forObjectId( String objectId ) {
		return new WebCmsObject()
		{
			@Override
			public String getObjectId() {
				return objectId;
			}

			@Override
			public boolean isNew() {
				return false;
			}
		};
	}
}
