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
import com.foreach.across.modules.webcms.domain.component.UnknownWebCmsComponentModelException;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
public class WebComponentModelViewElementModelWriter implements ViewElementModelWriter<WebCmsComponentModel>
{
	private Collection<WebComponentModelRenderer> renderers;

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
	void setRenderers( @RefreshableCollection(includeModuleInternals = true) Collection<WebComponentModelRenderer> renderers ) {
		this.renderers = renderers;
	}
}
