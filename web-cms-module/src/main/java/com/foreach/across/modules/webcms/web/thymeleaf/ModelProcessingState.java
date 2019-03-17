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

package com.foreach.across.modules.webcms.web.thymeleaf;

import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelHierarchy;
import com.foreach.across.modules.webcms.domain.component.model.create.WebCmsComponentAutoCreateQueue;
import com.foreach.across.modules.webcms.domain.component.placeholder.WebCmsPlaceholderContentModel;
import lombok.*;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.context.WebEngineContext;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Helper for tracking the Thymeleaf model processing state.
 *
 * @author Arne Vandamme
 * @since 0.0.7
 */
@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class ModelProcessingState
{
	@Getter
	private final WebCmsComponentModelHierarchy componentModelHierarchy;

	@Getter
	private final WebCmsComponentAutoCreateQueue autoCreateQueue;

	@Getter
	private final WebCmsPlaceholderContentModel placeholderContentModel;

	private final Deque<Change> stateChanges = new ArrayDeque<>();

	private Change currentState = new Change( false, false, false );

	/**
	 * True if the current model is part of a placeholder content.
	 * If the model is part of a component positioned inside a placeholder,
	 * this should be false.
	 */
	public boolean isInsidePlaceholder() {
		return currentState.placeholder;
	}

	/**
	 * True if current model is a direct part of component content.
	 * This will be false if the model is a direct child of a placeholder,
	 * which is inside a component.
	 */
	public boolean isInsideComponentBeingCreated() {
		return currentState.component;
	}

	/**
	 * True if only placeholder tags should be processed.
	 */
	public boolean isParsingPlaceholders() {
		return currentState.parsePlaceholders;
	}

	boolean isParsePlaceholdersOnly() {
		return isParsingPlaceholders() && !isInsidePlaceholder() && !isInsideComponentBeingCreated();
	}

	public void push( Change change ) {
		if ( change.parsePlaceholders ) {
			change.component |= currentState.component;
		}
//		change.parsePlaceholders |= currentState.parsePlaceholders;
		stateChanges.add( currentState );
		currentState = change;
	}

	public void pop() {
		if ( !stateChanges.isEmpty() ) {
			currentState = stateChanges.removeLast();
		}
	}

	static ModelProcessingState retrieve( ITemplateContext templateContext ) {
		WebEngineContext context = (WebEngineContext) templateContext;
		HttpServletRequest request = context.getRequest();

		ModelProcessingState value = (ModelProcessingState) request.getAttribute( ModelProcessingState.class.getName() );

		if ( value == null ) {
			ApplicationContext applicationContext = RequestContextUtils.findWebApplicationContext( ( context ).getRequest() );
			value = new ModelProcessingState( applicationContext.getBean( WebCmsComponentModelHierarchy.class ),
			                                  applicationContext.getBean( WebCmsComponentAutoCreateQueue.class ),
			                                  applicationContext.getBean( WebCmsPlaceholderContentModel.class ) );
			request.setAttribute( ModelProcessingState.class.getName(), value );
		}

		return value;
	}

	@Data
	@AllArgsConstructor
	static class Change
	{
		private boolean placeholder;
		private boolean component;
		private boolean parsePlaceholders;

		static Change placeholder() {
			return new Change( true, false, false );
		}

		static Change component() {
			return new Change( false, true, false );
		}

		static Change parsePlaceholders() {
			return new Change( false, false, true );
		}
	}
}
