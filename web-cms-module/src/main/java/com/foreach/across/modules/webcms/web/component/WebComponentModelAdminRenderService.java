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

package com.foreach.across.modules.webcms.web.component;

import com.foreach.across.core.annotations.RefreshableCollection;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.webcms.domain.component.UnknownWebComponentModelException;
import com.foreach.across.modules.webcms.domain.component.model.WebComponentModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Service
public class WebComponentModelAdminRenderService
{
	private Collection<WebComponentModelAdminRenderer> renderers = Collections.emptyList();

	@SuppressWarnings("unchecked")
	public ViewElementBuilder createContentViewElementBuilder( WebComponentModel componentModel, String controlNamePrefix ) {
		return renderers.stream()
		                .filter( r -> r.supports( componentModel ) )
		                .findFirst()
		                .orElseThrow( () -> new UnknownWebComponentModelException( componentModel ) )
		                .createContentViewElementBuilder( componentModel, controlNamePrefix );
	}

	@Autowired
	void setRenderers( @RefreshableCollection(includeModuleInternals = true) Collection<WebComponentModelAdminRenderer> renderers ) {
		this.renderers = renderers;
	}
}
