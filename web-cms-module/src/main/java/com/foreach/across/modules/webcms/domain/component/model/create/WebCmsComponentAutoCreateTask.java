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

package com.foreach.across.modules.webcms.domain.component.model.create;

import com.foreach.across.modules.webcms.domain.WebCmsObject;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentType;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.*;

/**
 * Represents a task for creating components.
 * A task always has a single component but can in fact represent an entire hierarchy components to be created,
 * through its {@link #getChildren()} property.
 * <p/>
 * Tasks are usually created by the {@link WebCmsComponentAutoCreateQueue} and then executed
 * by the {@link WebCmsComponentAutoCreateService}.
 *
 * @author Arne Vandamme
 * @see WebCmsComponentAutoCreateService
 * @since 0.0.2
 */
@Getter
@Setter
@RequiredArgsConstructor
public class WebCmsComponentAutoCreateTask
{
	private final String taskId = UUID.randomUUID().toString();

	private final String componentName;
	private final String scopeName;
	private final WebCmsComponentType componentType;

	private final Deque<WebCmsComponentAutoCreateTask> children = new ArrayDeque<>();

	@Getter
	private final Map<String, Object> metadata = new LinkedHashMap<>();

	@Getter
	private final List<AttributeValue> attributeValues = new ArrayList<>();

	private int sortIndex;
	private WebCmsObject owner;
	private WebCmsDomain domain;

	private String output;

	public void addChild( WebCmsComponentAutoCreateTask task ) {
		task.sortIndex = children.size() + 1;
		children.add( task );
	}

	/**
	 * Add an attribute value to the component: this will first attempt to set
	 * the property directly on the component model, and if the property does
	 * not exist there, will attempt to set it on the metadata.
	 */
	public void addAttributeValue( String key, Object value ) {
		attributeValues.add( new AttributeValue( Attribute.ANY, key, value ) );
	}

	public void addAttributeValue( Attribute attributeType, String key, Object value ) {
		attributeValues.add( new AttributeValue( attributeType, key, value ) );
	}

	/**
	 * Add a value for a direct property of the component model.
	 */
	public void addPropertyValue( String key, Object value ) {
		attributeValues.add( new AttributeValue( Attribute.PROPERTY, key, value ) );
	}

	/**
	 * Add a value for a metadata property.
	 */
	public void addMetadataValue( String key, Object value ) {
		attributeValues.add( new AttributeValue( Attribute.METADATA, key, value ) );
	}

	public boolean hasChildren() {
		return !children.isEmpty();
	}

	public enum Attribute
	{
		ANY,
		METADATA,
		PROPERTY
	}

	@Getter
	@RequiredArgsConstructor
	@ToString(of = { "key", "value" })
	static class AttributeValue
	{
		private final Attribute attribute;
		private final String key;
		private final Object value;
	}
}
