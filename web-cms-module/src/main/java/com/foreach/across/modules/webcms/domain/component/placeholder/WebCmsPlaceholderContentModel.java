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

package com.foreach.across.modules.webcms.domain.component.placeholder;

import com.foreach.across.core.annotations.Exposed;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.*;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Component
@Exposed
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WebCmsPlaceholderContentModel implements WebCmsPlaceholderLookupService
{
	private static final Object NULL = new Object();

	private Map<String, Object> currentData = new HashMap<>();
	private final Deque<Map<String, Object>> allData = new ArrayDeque<>();

	public WebCmsPlaceholderContentModel() {
		increaseLevel();
	}

	public void setPlaceholderContent( String placeholderName, Object content ) {
		Assert.notNull( placeholderName );
		currentData.put( placeholderName, content != null ? content : NULL );
	}

	@Override
	public Optional<Object> getPlaceholderContent( String placeholderName ) {
		return allData.stream()
		              .map( m -> m.get( placeholderName ) )
		              .filter( Objects::nonNull )
		              .findFirst()
		              .map( v -> v == NULL ? null : v );
	}

	public void increaseLevel() {
		currentData = new HashMap<>();
		allData.addFirst( currentData );
	}

	public void decreaseLevel() {
		allData.removeFirst();
		if ( allData.isEmpty() ) {
			increaseLevel();
		}
	}
}
