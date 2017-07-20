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

import lombok.Data;
import lombok.NonNull;
import org.springframework.util.Assert;

import java.util.*;

/**
 * Represents a set of data to be imported.
 * Data should be either a {@link Map} or a {@link Collection} (usually of {@link Map} items).
 * <p/>
 *
 * @author Arne Vandamme
 * @see WebCmsDataImportServiceImpl
 * @see WebCmsDataImportAction
 * @since 0.0.1
 */
@Data
public final class WebCmsDataEntry
{
	/**
	 * Key of this mapData entry item.
	 */
	private final String key;

	/**
	 * Optional parent data entry.
	 */
	private final WebCmsDataEntry parent;

	/**
	 * Data itself - should not be null.
	 */
	private final Map<String, Object> mapData;

	/**
	 * Data itself as a collection of objects.
	 */
	private final Collection<Object> collectionData;

	/**
	 * Action that should be performed with this data.
	 */
	@NonNull
	private WebCmsDataImportAction importAction = WebCmsDataImportAction.CREATE_OR_UPDATE;

	public WebCmsDataEntry( String key, Object data ) {
		this( key, null, data );
	}

	@SuppressWarnings("unchecked")
	public WebCmsDataEntry( String key, WebCmsDataEntry parent, Object data ) {
		Assert.notNull( data );

		this.key = key;
		this.parent = parent;

		if ( parent != null ) {
			importAction = parent.importAction;
		}

		if ( data instanceof Map ) {
			Map<String, Object> map = new LinkedHashMap<>( (Map<String, Object>) data );
			importAction = Optional.ofNullable( WebCmsDataImportAction.fromAttributeValue( (String) map.remove( WebCmsDataImportAction.ATTRIBUTE_NAME ) ) )
			                       .orElse( importAction );

			this.mapData = Collections.unmodifiableMap( map );
			this.collectionData = null;
		}
		else if ( data instanceof Collection ) {
			this.collectionData = (Collection) data;
			this.mapData = null;
		}
		else {
			throw new IllegalArgumentException( "Only Collection or Map data is supported." );
		}
	}

	/**
	 * @return true if data is of type map
	 */
	public boolean isMapData() {
		return mapData != null;
	}

	/**
	 * @return true if the data entry has a parent data entry
	 */
	public boolean hasParent() {
		return parent != null;
	}

	/**
	 * Shortcut to retrieve the key of the optional parent data.
	 *
	 * @return parent key or {@code null} if no parent data
	 */
	public String getParentKey() {
		return hasParent() ? parent.getKey() : null;
	}
}
