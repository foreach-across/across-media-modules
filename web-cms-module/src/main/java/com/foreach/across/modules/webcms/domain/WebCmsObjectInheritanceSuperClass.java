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

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.util.Date;

/**
 * Extends the {@link WebCmsObjectSuperClass} with the base columns for a joined inheritance strategy, using an object type discriminator column.
 * Implementing entities should have their discriminator column named <strong>object_type</strong> and should map it readonly to {@link #getObjectType()}.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Access(AccessType.FIELD)
@MappedSuperclass
public abstract class WebCmsObjectInheritanceSuperClass<T extends WebCmsObjectInheritanceSuperClass<T>> extends WebCmsObjectSuperClass<T>
{
	/**
	 * Name of the discriminator column, mapped readonly.
	 */
	public static final String DISCRIMINATOR_COLUMN = "object_type";

	@Column(name = DISCRIMINATOR_COLUMN, insertable = false, updatable = false)
	private String objectType = getObjectType();

	protected WebCmsObjectInheritanceSuperClass() {
		super();
	}

	protected WebCmsObjectInheritanceSuperClass( Long id,
	                                             Long newEntityId,
	                                             String objectId,
	                                             String createdBy,
	                                             Date createdDate,
	                                             String lastModifiedBy,
	                                             Date lastModifiedDate ) {
		super( id, newEntityId, objectId, createdBy, createdDate, lastModifiedBy, lastModifiedDate );
	}

	/**
	 * @return the object type name
	 */
	public abstract String getObjectType();
}
