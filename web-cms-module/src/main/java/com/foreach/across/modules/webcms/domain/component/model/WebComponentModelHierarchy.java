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
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
@Exposed
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WebComponentModelHierarchy
{
	private WebComponentModelSet componentModelSet;

	public void setComponents( WebComponentModelSet componentModelSet ) {
		this.componentModelSet = componentModelSet;
	}

	public WebComponentModelSet getComponentsForScope( String scope ) {
		Assert.notNull( scope );

		WebComponentModelSet candidate = componentModelSet;
		while ( candidate != null && !scope.equals( candidate.getScopeName() ) ) {
			candidate = candidate.getParent();
		}
		return candidate;
	}

	public WebComponentModelSet getComponents() {
		return componentModelSet;
	}
}
