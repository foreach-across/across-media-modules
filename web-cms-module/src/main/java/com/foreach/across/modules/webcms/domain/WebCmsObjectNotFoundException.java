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

import lombok.Getter;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Getter
public final class WebCmsObjectNotFoundException extends RuntimeException
{
	private final String identifier;
	private final Class<?> objectType;

	public WebCmsObjectNotFoundException( String identifier ) {
		this( identifier, WebCmsObject.class );
	}

	public WebCmsObjectNotFoundException( String identifier, Class<? extends WebCmsObject> objectType ) {
		this( identifier, objectType, null );
	}

	public WebCmsObjectNotFoundException( String identifier, Throwable cause ) {
		this( identifier, WebCmsObject.class, cause );
	}

	public WebCmsObjectNotFoundException( String identifier, Class<? extends WebCmsObject> objectType, Throwable cause ) {
		super( "Could not find a " + objectType.getSimpleName() + " for identifier " + identifier, cause );
		this.identifier = identifier;
		this.objectType = objectType;
	}
}
