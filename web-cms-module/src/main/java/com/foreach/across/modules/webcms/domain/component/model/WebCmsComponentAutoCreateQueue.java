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

package com.foreach.across.modules.webcms.domain.component.model;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.entity.util.EntityUtils;
import com.foreach.across.modules.webcms.domain.WebCmsObject;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentRepository;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentType;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentTypeRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
@Exposed
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
@RequiredArgsConstructor
public class WebCmsComponentAutoCreateQueue
{
	private final Map<String, Task> tasksByKey = new HashMap<>();
	private final ArrayDeque<Task> tasks = new ArrayDeque<>();
	private final ArrayDeque<Task> outputQueue = new ArrayDeque<>();

	private final WebCmsComponentRepository componentRepository;
	private final WebCmsComponentTypeRepository componentTypeRepository;
	private final WebCmsComponentModelHierarchy componentModelHierarchy;

	public String schedule( String componentName, String scope, String type ) {
		String key = componentName + ":" + scope;

		Task creationTask = tasksByKey.computeIfAbsent( key, k -> new Task( componentName, scope, type ) );
		tasks.add( creationTask );

		return creationTask.getObjectId();
	}

	public Task getCurrentTask() {
		return outputQueue.peek();
	}

	public void outputStarted( String objectId ) {
		Task current;

		do {
			current = tasks.remove();
		}
		while ( current != null && !objectId.equals( current.getObjectId() ) );

		outputQueue.push( current );
	}

	public void outputFinished( String objectId, String output ) {
		Task current = outputQueue.pop();
		Assert.isTrue( objectId.equals( current.getObjectId() ) );

		current.setOutput( output );

		Task next = outputQueue.peek();

		// todo: nested components in case of container
		if ( next != null ) {
			next.addChild( current );
		}
		else {
			OrderedWebComponentModelSet componentModelSet = componentModelHierarchy.getComponentsForScope( current.getScopeName() );
			saveComponent( current, output, componentModelSet.getOwner() );
		}
	}

	private void saveComponent( Task creationTask, String output, WebCmsObject owner ) {
		WebCmsComponent component = creationTask.getComponent();
		component.setComponentType( determineComponentType( creationTask.getComponentType() ) );
		component.setTitle( EntityUtils.generateDisplayName( component.getName().replace( '-', '_' ) ) );
		component.setOwner( owner );
		component.setBody( output );
		componentRepository.save( component );

		creationTask.getChildren().forEach( childTask -> saveComponent( childTask, childTask.getOutput(), component ) );
	}

	private WebCmsComponentType determineComponentType( String requestedComponentType ) {
		return componentTypeRepository.findOneByTypeKey( StringUtils.isEmpty( requestedComponentType ) ? "rich-text" : requestedComponentType );
	}

	@Getter
	@Setter
	public static class Task
	{
		private final WebCmsComponent component;
		private final String scopeName;
		private final String componentType;
		private final Deque<Task> children = new ArrayDeque<>();
		private int sortIndex;

		public Task( String componentName, String scopeName, String componentType ) {
			this.scopeName = scopeName;
			this.componentType = componentType;

			component = new WebCmsComponent();
			component.setName( componentName );
		}

		private String output;

		public String getObjectId() {
			return component.getObjectId();
		}

		public void addChild( Task task ) {
			task.sortIndex = children.size() + 1;
			children.add( task );
		}
	}
}
