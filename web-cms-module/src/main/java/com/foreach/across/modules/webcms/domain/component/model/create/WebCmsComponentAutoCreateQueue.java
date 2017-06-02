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

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentType;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelSet;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelHierarchy;
import com.foreach.across.modules.webcms.domain.component.placeholder.PlaceholderWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.proxy.ProxyWebCmsComponentModel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents the request-bound queue for auto-creation of components.
 * Used by template parsers to signal to manage the actual component rendering blocks.
 *
 * @author Arne Vandamme
 * @see WebCmsComponentAutoCreateService
 * @since 0.0.2
 */
@Component
@Exposed
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
@RequiredArgsConstructor
public class WebCmsComponentAutoCreateQueue
{
	public static final String CONTAINER_MEMBER_SCOPE = WebCmsComponentModelHierarchy.CONTAINER;

	private final Map<String, WebCmsComponentModel> componentsCreated = new HashMap<>();
	private final Map<String, WebCmsComponentAutoCreateTask> tasksByKey = new HashMap<>();
	private final ArrayDeque<WebCmsComponentAutoCreateTask> tasks = new ArrayDeque<>();
	private final ArrayDeque<WebCmsComponentAutoCreateTask> outputQueue = new ArrayDeque<>();

	private final WebCmsComponentModelHierarchy componentModelHierarchy;
	private final WebCmsComponentAutoCreateService autoCreateService;

	public WebCmsComponentAutoCreateTask schedule( String componentName, String scope, String type ) {
		String key = CONTAINER_MEMBER_SCOPE.equals( scope ) ? UUID.randomUUID().toString() : componentName + ":" + scope;

		WebCmsComponentAutoCreateTask creationTask = tasksByKey.computeIfAbsent( key, k -> {
			WebCmsComponentType componentType = autoCreateService.resolveComponentType( type );
			return new WebCmsComponentAutoCreateTask( componentName, scope, componentType );
		} );
		tasks.add( creationTask );

		return creationTask;
	}

	public WebCmsComponentAutoCreateTask getCurrentTask() {
		return outputQueue.peek();
	}

	public WebCmsComponentAutoCreateTask getTask( String taskId ) {
		return tasksByKey.values().stream().filter( t -> taskId.equals( t.getTaskId() ) ).findFirst().orElse( null );
	}

	public void outputStarted( String taskId ) {
		WebCmsComponentAutoCreateTask current;

		do {
			current = tasks.remove();
		}
		while ( current != null && !taskId.equals( current.getTaskId() ) );

		outputQueue.push( current );
	}

	public void outputFinished( String taskId, String output ) {
		if ( !componentsCreated.containsKey( taskId ) ) {
			WebCmsComponentAutoCreateTask current = outputQueue.pop();
			Assert.isTrue( taskId.equals( current.getTaskId() ) );

			current.setOutput( output );

			WebCmsComponentAutoCreateTask next = outputQueue.peek();

			if ( next != null && CONTAINER_MEMBER_SCOPE.equals( current.getScopeName() ) ) {
				next.addChild( current );
			}
			else {
				WebCmsComponentModelSet componentModelSet = componentModelHierarchy.getComponentsForScope( current.getScopeName() );
				current.setOwner( componentModelSet.getOwner() );

				WebCmsComponentModel componentModel = autoCreateService.createComponent( current );
				componentModelSet.add( componentModel );

				componentsCreated.put( current.getTaskId(), componentModel );
			}
		}
	}

	/**
	 * Single a placeholder block has just been rendered.
	 * If a component is being created, the placeholder should be added to that component.
	 *
	 * @param placeholderName name of the placeholder
	 */
	public void placeholderRendered( String placeholderName ) {
		val current = getCurrentTask();

		if ( current != null ) {
			current.addChild(
					new WebCmsComponentAutoCreateTask( placeholderName, null, autoCreateService.resolveComponentType( PlaceholderWebCmsComponentModel.TYPE ) )
			);
		}
	}

	/**
	 * Proxy to the component with the specific object id should be created.
	 *
	 * @param objectId of the component to proxy
	 */
	public void createProxy( String objectId ) {
		val current = getCurrentTask();

		if ( current != null ) {
			current.addChild(
					new WebCmsComponentAutoCreateTask(
							objectId, CONTAINER_MEMBER_SCOPE, autoCreateService.resolveComponentType( ProxyWebCmsComponentModel.TYPE )
					)
			);
		}
	}

	/**
	 * @return component created by that task
	 */
	public WebCmsComponentModel getComponentCreated( String taskId ) {
		return componentsCreated.get( taskId );
	}
}
