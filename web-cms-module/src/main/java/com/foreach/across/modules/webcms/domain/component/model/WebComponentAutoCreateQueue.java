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
import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
@Exposed
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WebComponentAutoCreateQueue
{
	private final ArrayDeque<Task> tasks = new ArrayDeque<>();
	private final ArrayDeque<Task> outputQueue = new ArrayDeque<>();

	public void schedule( String componentName, String scope, String type ) {
		tasks.add( new Task( componentName, scope, type ) );
	}

	public void outputStarted( String componentName ) {
		Task current;

		do {
			current = tasks.remove();
		}
		while ( current != null && !componentName.equals( current.componentName ) );

		outputQueue.push( current );
	}

	public void outputFinished( String componentName, String output ) {
		Task current = outputQueue.pop();
		Assert.isTrue( componentName.equals( current.componentName ) );

		current.setOutput( output );

		Task next = outputQueue.peek();

		if ( next != null ) {
			next.addChild( current );
		}

		System.err.println( current );
	}

	@Data
	private static class Task
	{
		private final String componentName;
		private final String scopeName;
		private final String componentType;
		private final Deque<Task> children = new ArrayDeque<>();

		private String output;

		public void addChild( Task task ) {
			children.add( task );
		}
	}
}
