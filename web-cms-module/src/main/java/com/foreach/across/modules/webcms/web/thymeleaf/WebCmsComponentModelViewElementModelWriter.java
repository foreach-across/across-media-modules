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

import com.foreach.across.core.annotations.RefreshableCollection;
import com.foreach.across.modules.web.thymeleaf.ThymeleafModelBuilder;
import com.foreach.across.modules.web.ui.thymeleaf.ViewElementModelWriter;
import com.foreach.across.modules.web.ui.thymeleaf.ViewElementModelWriterRegistry;
import com.foreach.across.modules.webcms.domain.component.UnknownWebCmsComponentModelException;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Base Thymeleaf writer for {@link WebCmsComponentModel} view element.
 * Simply dispatches to a {@link WebCmsComponentModelRenderer} for the corresponding component model.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@ConditionalOnBean(ViewElementModelWriterRegistry.class)
@Component
class WebCmsComponentModelViewElementModelWriter implements ViewElementModelWriter<WebCmsComponentModel>
{
	private Collection<WebCmsComponentModelRenderer> renderers;

	@SuppressWarnings("unchecked")
	@Override
	public void writeModel( WebCmsComponentModel webComponentModel, ThymeleafModelBuilder model ) {
		renderers.stream()
		         .filter( r -> r.supports( webComponentModel ) )
		         .findFirst()
		         .orElseThrow( () -> new UnknownWebCmsComponentModelException( webComponentModel ) )
		         .writeComponent( webComponentModel, model );
	}

	@Autowired
	void setRenderers( @RefreshableCollection(includeModuleInternals = true) Collection<WebCmsComponentModelRenderer> renderers ) {
		this.renderers = renderers;
	}

	@Autowired
	void registerModelWriter( ViewElementModelWriterRegistry modelWriterRegistry ) {
		modelWriterRegistry.registerModelWriter( WebCmsComponentModel.class.getSimpleName(), this );
	}
}
