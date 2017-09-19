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
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.Consumer;

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
	public final static String ROOT = "<root>";
	/**
	 * Key of this mapData entry item.
	 */
	private final String key;

	/**
	 * Data itself.
	 */
	private final Map<String, Object> mapData;

	/**
	 * Data itself as a collection of objects.
	 */
	private final Collection<Object> collectionData;

	/**
	 * Data itself if neither collection nor map.
	 */
	private final Object singleValue;

	/**
	 * Optional parent data entry.
	 */
	private final WebCmsDataEntry parent;

	/**
	 * Optional identifier of the data entry, child entries have the same identifier as their parent.
	 */
	private String identifier;

	/**
	 * Action that should be performed with this data.
	 */
	@NonNull
	private WebCmsDataImportAction importAction = WebCmsDataImportAction.CREATE_OR_UPDATE;

	/**
	 * Callback functions that should run when the import of this data entry has been completed.
	 */
	private List<Consumer<WebCmsDataEntry>> completedCallbacks;

	public WebCmsDataEntry( String key, Object data ) {
		this( key, (WebCmsDataEntry) null, data );
	}

	public WebCmsDataEntry( String identifier, String key, Object data ) {
		this( key, (WebCmsDataEntry) null, data );
		this.identifier = identifier;
	}

	@SuppressWarnings("unchecked")
	public WebCmsDataEntry( String key, WebCmsDataEntry parent, Object data ) {
		Assert.notNull( data );
		completedCallbacks = new ArrayList<>();

		this.key = key;
		this.parent = parent;

		if ( parent != null ) {
			importAction = parent.importAction;
			identifier = parent.identifier;
		}

		if ( data instanceof Map ) {
			Map<String, Object> map = new LinkedHashMap<>( (Map<String, Object>) data );
			importAction = Optional.ofNullable( WebCmsDataImportAction.fromAttributeValue( (String) map.remove( WebCmsDataImportAction.ATTRIBUTE_NAME ) ) )
			                       .orElse( importAction );

			this.mapData = Collections.unmodifiableMap( map );
			this.collectionData = null;
			this.singleValue = null;
		}
		else if ( data instanceof Collection ) {
			this.collectionData = (Collection) data;
			this.mapData = null;
			this.singleValue = null;
		}
		else {
			this.singleValue = data;
			this.mapData = null;
			this.collectionData = null;
		}
	}

	/**
	 * @return true if data is of type map
	 */
	public boolean isMapData() {
		return mapData != null;
	}

	/**
	 * @return true if data is of type collection
	 */
	public boolean isCollectionData() {
		return collectionData != null;
	}

	/**
	 * Get the single value and cast it to the expected type.
	 *
	 * @param expectedType to cast to
	 * @param <V>          expected type
	 * @param <U>          specific (generic-aware) implementation of the expected type
	 * @return single value as type
	 */
	@SuppressWarnings("unchecked")
	public <V, U extends V> U getSingleValue( Class<V> expectedType ) {
		return (U) expectedType.cast( getSingleValue() );
	}

	/**
	 * @return true if data is a single (usually primitive) value
	 */
	public boolean isSingleValue() {
		return singleValue != null;
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

	/**
	 * Attempt to build a relative location of this data entry in the parent data.
	 *
	 * @return location (null if no parent)
	 */
	public String getLocation() {
		String current = "/" +
				( StringUtils.isEmpty( key )
						? ( isMapData() ? "<map>" : ( isCollectionData() ? "<list>" : "" ) )
						: ( ROOT.equals( key ) ) ? "" : key );

		if ( hasParent() ) {
			String parent = getParent().getLocation();
			if ( !"/".equals( parent ) ) {
				return parent + current;
			}
		}

		return current;
	}

	public String toString() {
		Object data = isMapData() ? mapData : ( isCollectionData() ? collectionData : singleValue );

		return "WebCmsDataEntry(" +
				"identifier='" + identifier + "'," +
				"location='" + getLocation() + "'," +
				"data=" + data +
				")";
	}

	/**
	 * Adds a callback to execute once the data entry is completed
	 *
	 * @param consumer the callback
	 */
	public void addCompletedCallback( Consumer consumer ) {
		this.completedCallbacks.add( consumer );
	}
}
